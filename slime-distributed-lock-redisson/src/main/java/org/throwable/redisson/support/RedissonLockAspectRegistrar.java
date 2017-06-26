package org.throwable.redisson.support;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.throwable.distributed.exception.LockException;
import org.throwable.distributed.exception.UnMatchedLockKeyException;
import org.throwable.distributed.support.DistributedLockTargetKeyMatcher;
import org.throwable.redisson.annotation.RedissonDistributedLock;
import org.throwable.redisson.annotation.RedissonDistributedLocks;
import org.throwable.utils.ClazzUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/25 1:47
 */
@Slf4j
public class RedissonLockAspectRegistrar implements ImportBeanDefinitionRegistrar {

	private final DistributedLockTargetKeyMatcher keyMatcher = new DistributedLockTargetKeyMatcher();

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
										BeanDefinitionRegistry registry) {
		final DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) registry;
		Advice advice = (MethodInterceptor) invocation -> {
			Method invocationMethod = invocation.getMethod();
			ProxyMethodInvocation pmi = (ProxyMethodInvocation) invocation;
			ProceedingJoinPoint pjp = new MethodInvocationProceedingJoinPoint(pmi);
			Signature signature = pjp.getSignature();
			MethodSignature methodSignature = (MethodSignature) signature;
			return processRedissonLock(invocation, invocationMethod, methodSignature, beanFactory);
		};
		registerRedissonLockAspect(advice, beanFactory);
	}

	private Object processRedissonLock(MethodInvocation methodInvocation, Method invocationMethod,
									   MethodSignature methodSignature,
									   DefaultListableBeanFactory beanFactory) {
		List<LockInvocation> lockInvocations = buildLockInvocations(invocationMethod, methodSignature, beanFactory, methodInvocation);
		boolean acquireSuccess = false;
		try {
			acquireSuccess = processLockChainAcquire(lockInvocations);
			if (acquireSuccess) {
				return methodInvocation.proceed();
			}
		} catch (Throwable e) {
			log.error("RedissonLockAspect processRedissonLock execute failed!!", e);
			throw new RuntimeException(e);  //unknown exception
		} finally {
			if (acquireSuccess) {
				processLockChainRelease(lockInvocations);
			}
		}
		throw new LockException(String.format("RedissonLockAspect processRedissonLock execute failed for trying to get lock timeout!Target method : %s",
				invocationMethod.getDeclaringClass().getCanonicalName() + "." + invocationMethod.getName()));
	}

	private Set<RedissonDistributedLock> findRedissonLockAnnotations(Method invocationMethod) {
		Set<RedissonDistributedLock> lockAnnos = new HashSet<>();
		RedissonDistributedLock anno = AnnotationUtils.findAnnotation(invocationMethod, RedissonDistributedLock.class);
		if (null != anno) {
			lockAnnos.add(anno);
		}
		RedissonDistributedLocks annos = AnnotationUtils.findAnnotation(invocationMethod, RedissonDistributedLocks.class);
		if (null != annos) {
			Collections.addAll(lockAnnos, annos.value());
		}
		return lockAnnos;
	}

	private List<LockInvocation> buildLockInvocations(Method invocationMethod, MethodSignature methodSignature,
													  DefaultListableBeanFactory beanFactory, MethodInvocation methodInvocation) {
		RedissonLockFactory lockFactory = beanFactory.getBean(RedissonLockFactory.class);
		List<LockInvocation> lockInvocationsToUse = new LinkedList<>();
		Set<RedissonDistributedLock> lockAnnosToUse = findRedissonLockAnnotations(invocationMethod);
		for (RedissonDistributedLock anno : lockAnnosToUse) {
			lockInvocationsToUse.add(buildLockInvocation(anno, methodSignature, lockFactory, methodInvocation));
		}
		lockInvocationsToUse.sort(Comparator.comparingInt(LockInvocation::getOrder)); //sort by order
		return lockInvocationsToUse;
	}

	private LockInvocation buildLockInvocation(RedissonDistributedLock anno,
											   MethodSignature methodSignature,
											   RedissonLockFactory lockFactory, MethodInvocation methodInvocation) {
		String lockPathToUse = resolveRealLockPathFromKeyArray(anno, methodSignature, methodInvocation);
		return new LockInvocation(lockPathToUse, anno.waitTime(), anno.leaseTime(), anno.unit(),
				anno.isFair(), anno.order(), lockFactory);
	}

	private String resolveRealLockPathFromKeyArray(RedissonDistributedLock anno, MethodSignature methodSignature,
												   MethodInvocation methodInvocation) {
		String[] parameterNames = methodSignature.getParameterNames();
		if (null == parameterNames || parameterNames.length == 0) {
			throw new UnMatchedLockKeyException("@RedissonDistributedLock must be matched to any parameter key!!!!Method parameters array's length is zero");
		}
		String[] keyNameArray = anno.keyNames();
		if (keyNameArray.length == 0) {
			throw new UnMatchedLockKeyException("@RedissonDistributedLock must be matched to any parameter key!!!!Annotation keyName array's length is zero");
		}
		//key - parameter name, value - parameter value
		Map<String, Object> args = buildMethodParameterMap(parameterNames, methodInvocation.getArguments());
		StringBuilder targetLockPath = new StringBuilder(anno.lockPathPrefix());
		for (String key : keyNameArray) {
			for (Map.Entry<String, Object> entry : args.entrySet()) {
				String name = entry.getKey();
				Object value = entry.getValue();
				if (name.equals(key)) {
					if (ClazzUtils.isPrimitive(value.getClass())) {
						targetLockPath.append(String.valueOf(value)).append(anno.keySeparator());
					} else {
						throw new UnMatchedLockKeyException(String.format("Key name <%s> is matched to parameter name,but it is not primitive!", key));
					}
				} else {
					targetLockPath.append(keyMatcher.matchAndReturnLockKeyByTargetObjectAndKeyName(value, key)).append(anno.keySeparator());
				}
			}
		}
		return targetLockPath.substring(0, targetLockPath.lastIndexOf(anno.keySeparator()));
	}

	private Map<String, Object> buildMethodParameterMap(String[] parameterNames, Object[] args) {
		Map<String, Object> result = new HashMap<>(parameterNames.length);
		int index = 0;
		for (String parameterName : parameterNames) {
			result.put(parameterName, args[index]);
			index++;
		}
		return result;
	}

	private boolean processLockChainAcquire(List<LockInvocation> lockInvocations) {
		return lockInvocations.stream().allMatch(distributedLockInvocation ->
				!distributedLockInvocation.isHeldByCurrentThread() && distributedLockInvocation.tryLock());
	}

	private void processLockChainRelease(List<LockInvocation> lockInvocations) {
		Collections.reverse(lockInvocations);
		lockInvocations.forEach(LockInvocation::release);
	}

	private void registerRedissonLockAspect(Advice advice, DefaultListableBeanFactory beanFactory) {
		AspectJExpressionPointcutAdvisor redissonLockAspect = new AspectJExpressionPointcutAdvisor();
		redissonLockAspect.setLocation("$$redissonLockAspect##");
		redissonLockAspect.setExpression("@annotation(org.throwable.redisson.annotation.RedissonDistributedLock) || @annotation(org.throwable.redisson.annotation.RedissonDistributedLocks)");
		redissonLockAspect.setAdvice(advice);
		redissonLockAspect.setBeanFactory(beanFactory);
		beanFactory.registerSingleton("redissonLockAspect", redissonLockAspect);
	}

	@NoArgsConstructor
	private static class LockInvocation {

		@Getter
		private String lockPathToUse;
		@Getter
		private long waitTime;
		@Getter
		private long leaseTime;
		@Getter
		private TimeUnit unit;
		@Getter
		private boolean isFair;
		@Getter
		private int order;

		private RLock rLock;

		public LockInvocation(String lockPathToUse, long waitTime, long leaseTime, TimeUnit unit,
							  boolean isFair, int order, RedissonLockFactory lockFactory) {
			this.lockPathToUse = lockPathToUse;
			this.waitTime = waitTime;
			this.leaseTime = leaseTime;
			this.unit = unit;
			this.isFair = isFair;
			this.order = order;
			this.rLock = lockFactory.createLockInstance(lockPathToUse, this.isFair);
		}

		public boolean tryLock() {
			try {
				return rLock.tryLock(waitTime, leaseTime, unit);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return false;
		}

		public void release() {
			try {
				rLock.unlock();
			} catch (IllegalMonitorStateException e) {
				if (log.isWarnEnabled()) {
					log.warn("Release lock error!", e);
				}
			}
		}

		public boolean isLocked() {
			return rLock.isLocked();
		}

		public boolean isHeldByCurrentThread() {
			return rLock.isHeldByCurrentThread();
		}

		public void forceUnlock() {
			rLock.forceUnlock();
		}
	}
}
