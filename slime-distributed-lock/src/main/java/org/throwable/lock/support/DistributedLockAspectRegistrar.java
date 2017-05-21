package org.throwable.lock.support;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.throwable.lock.annotation.DistributedLock;
import org.throwable.lock.annotation.DistributedLocks;
import org.throwable.lock.common.LockPolicyEnum;
import org.throwable.lock.exception.LockException;
import org.throwable.lock.exception.UnMatchedLockKeyException;
import org.throwable.utils.ClazzUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author throwable
 * @version v1.0
 * @function
 * @since 2017/5/17 12:09
 */
@Slf4j
public class DistributedLockAspectRegistrar implements ImportBeanDefinitionRegistrar {

    private final DistributedLockTargetKeyMatcher keyMatcher = new DistributedLockTargetKeyMatcher();

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {
        Advice advice = (MethodInterceptor) invocation -> {
            Method invocationMethod = invocation.getMethod();
            ProxyMethodInvocation pmi = (ProxyMethodInvocation) invocation;
            ProceedingJoinPoint pjp = new MethodInvocationProceedingJoinPoint(pmi);
            Signature signature = pjp.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;
            if (invocationMethod.isAnnotationPresent(DistributedLocks.class)) {
                DistributedLocks distributedLocks = invocationMethod.getAnnotation(DistributedLocks.class);
                return processDistributedLockMethodInterceptor(distributedLocks.locks(), invocation, methodSignature, registry);
            } else if (invocationMethod.isAnnotationPresent(DistributedLock.class)) {
                DistributedLock distributedLock = invocationMethod.getAnnotation(DistributedLock.class);
                return processDistributedLockMethodInterceptor(new DistributedLock[]{distributedLock}, invocation, methodSignature, registry);
            } else {
                return invocation.proceed();
            }
        };
        BeanDefinition aspectJBean = BeanDefinitionBuilder.genericBeanDefinition(AspectJExpressionPointcutAdvisor.class)
                .addPropertyValue("location", "$$distributedLockAspect##")
                .addPropertyValue("expression", "@annotation(org.throwable.lock.annotation.DistributedLock) || @annotation(org.throwable.lock.annotation.DistributedLocks)")
                .addPropertyValue("advice", advice)
                .getBeanDefinition();
        registry.registerBeanDefinition("distributedLockAspect", aspectJBean);
    }

    private Object processDistributedLockMethodInterceptor(DistributedLock[] lockAnnotations,
                                                           MethodInvocation methodInvocation,
                                                           MethodSignature methodSignature,
                                                           BeanDefinitionRegistry registry) {
        String[] parameterNames = methodSignature.getParameterNames();
        if (null == parameterNames || parameterNames.length == 0) {
            throw new UnMatchedLockKeyException("@DistributedLock must be matched to parameter key!!!!Method parameters array's length is zero");
        }
        DistributedLockContext context = wrapBeanDefinitionRegistry(registry).getBean(DistributedLockContext.class);
        List<DistributedLockInvocation> distributedLockInvocations = new ArrayList<>();
        for (DistributedLock lockAnnotation : lockAnnotations) {
            Class<?> target = lockAnnotation.target();
            String keyName = lockAnnotation.keyName();
            LockPolicyEnum policy = lockAnnotation.policy();
            long waitSeconds = lockAnnotation.waitSeconds();
            if (ClazzUtils.isPrimitive(target)) {
                processMatchKeyDistributedLockInvocation(parameterNames, keyName, methodInvocation, policy, distributedLockInvocations, waitSeconds, context);
            } else {
                distributedLockInvocations.add(buildDistributedLockInvocationWithClassTarget(target, policy, keyName, waitSeconds, methodInvocation, context));
            }
        }

        try {
            if (processDistributedLockChainAquire(distributedLockInvocations)) {
                return methodInvocation.proceed();
            }
        } catch (Throwable e) {
            log.error("processDistributedLockChainAquire execute failed for timeout:", e);
            throw new LockException(String.format("distributedLock execute #tryLock failed for timeout,message:%s", e.getMessage()));
        } finally {
            processDistributedLockChainRelease(distributedLockInvocations);
        }
        throw new LockException("distributedLock chain acquire lock failed for timeout");
    }

    private void processMatchKeyDistributedLockInvocation(String[] parameterNames, String keyName,
                                                          MethodInvocation methodInvocation, LockPolicyEnum policy,
                                                          List<DistributedLockInvocation> distributedLockInvocations,
                                                          long waitSeconds, DistributedLockContext context) {
        boolean match = false;
        for (int i = 0; i < parameterNames.length; i++) {
            String parameterName = parameterNames[i];
            if (parameterName.contains(keyName)) {
                match = true;
                String targetValue = String.valueOf(methodInvocation.getArguments()[i]);
                distributedLockInvocations.add(buildDistributedLockInvocation(policy, targetValue, waitSeconds, context));
                break;
            }
        }
        if (!match) {
            throw new UnMatchedLockKeyException("@DistributedLock must be matched to parameter key!!!!Lock keyName must be match to a parameter name,keyName: " + keyName);
        }
    }

    private DistributedLockInvocation buildDistributedLockInvocation(LockPolicyEnum policy,
                                                                     String lockPath,
                                                                     long waitSeconds,
                                                                     DistributedLockContext context) {
        return new DistributedLockInvocation(policy, lockPath, waitSeconds, context);
    }

    private DistributedLockInvocation buildDistributedLockInvocationWithClassTarget(Class<?> target,
                                                                                    LockPolicyEnum policy,
                                                                                    String lockPath,
                                                                                    long waitSeconds,
                                                                                    MethodInvocation methodInvocation,
                                                                                    DistributedLockContext context) {
        Object[] parameters = methodInvocation.getArguments();
        if (null == parameters || 0 == parameters.length) {
            throw new UnMatchedLockKeyException("@DistributedLock must be matched to parameter key!!!!Method parameters array's length is zero");
        }
        Object targetParam = null;
        for (Object parameter : parameters) {
            if (parameter.getClass().isAssignableFrom(target)) {
                targetParam = parameter;
                break;
            }
        }
        if (null == targetParam) {
            throw new UnMatchedLockKeyException("@DistributedLock must be matched to parameter key!!!!Target type must be matched to parameter type!!");
        }
        String targetKey = keyMatcher.matchAndReturnLockKeyByTargetObjectAndKeyName(targetParam, lockPath);
        return buildDistributedLockInvocation(policy, targetKey, waitSeconds, context);
    }

    /**
     * 分布式锁链加锁
     */
    private boolean processDistributedLockChainAquire(List<DistributedLockInvocation> distributedLockInvocations) {
        return distributedLockInvocations.stream().allMatch(distributedLockInvocation ->
                !distributedLockInvocation.isHeldByCurrentThread() && distributedLockInvocation.tryLock());
    }

    /**
     * 分布式锁链释放,注意解锁必须反序
     */
    private void processDistributedLockChainRelease(List<DistributedLockInvocation> distributedLockInvocations) {
        Collections.reverse(distributedLockInvocations);
        distributedLockInvocations.forEach(DistributedLockInvocation::release);
    }

    private DefaultListableBeanFactory wrapBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        return (DefaultListableBeanFactory) registry;
    }

    @NoArgsConstructor
    private static class DistributedLockInvocation {

        @Getter
        private LockPolicyEnum lockPolicy;
        @Getter
        private String lockPath;
        @Getter
        private long waitSeconds;

        private DistributedLockContext context;

        @Getter
        private org.throwable.lock.support.DistributedLock distributedLock;

        public DistributedLockInvocation(LockPolicyEnum lockPolicy, String lockPath,
                                         long waitSeconds, DistributedLockContext context) {
            this.lockPolicy = lockPolicy;
            this.lockPath = lockPath;
            this.waitSeconds = waitSeconds;
            this.context = context;
            this.distributedLock = this.context.getLockByPolicyAndPath(lockPolicy, lockPath);
        }

        public boolean tryLock() {
            try {
                return distributedLock.tryLock(waitSeconds, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error(String.format("execute distributedLock invocation #tryLock failed,lockPath:%s", lockPath), e);
                throw new LockException("execute distributedLock invocation #tryLock failed,lockPath:" + lockPath);
            }
        }

        public void release() {
            try {
                this.distributedLock.release();
            } catch (Exception e) {
                log.warn(String.format("execute distributedLock invocation #release failed,lockPath:%s", lockPath), e);
            }
        }

        public boolean isHeldByCurrentThread() {
            return distributedLock.isHeldByCurrentThread();
        }
    }


}
