package org.throwable.mapper.configuration;

import com.google.common.collect.Lists;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;
import org.throwable.mapper.configuration.prop.MapperProperties;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/4 22:20
 */
@Slf4j
public class AutoConfiguredMapperScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar,
        EnvironmentAware, ResourceLoaderAware {

    @Setter
    private BeanFactory beanFactory;
    @Setter
    private ResourceLoader resourceLoader;

    private MapperProperties properties;

	//为了获取mybatis一些基本的配置属性
    @Override
    public void setEnvironment(Environment environment) {
        properties = new MapperProperties(environment);
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        log.debug("Scanning mapper interfaces and register them into ioc container");
		ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
        try {
            if (null != this.resourceLoader) {
                scanner.setResourceLoader(this.resourceLoader);
            }
            scanner.setAddToConfig(true);
            scanner.setSqlSessionFactoryBeanName("sqlSessionFactory");
            List<String> basePackages = Lists.newArrayList();
            for (String pkg : properties.getBasePackages()) {
                if (StringUtils.hasText(pkg)) {
                    basePackages.add(pkg);
                }
            }
            //如果需要扫描的包为空,则全局扫描
            if (basePackages.isEmpty()) {
                basePackages.addAll(AutoConfigurationPackages.get(this.beanFactory));
            }
            if (log.isDebugEnabled()) {
                for (String pkg : basePackages) {
                    log.debug("Using auto-configuration base package '{}'", pkg);
                }
            }
            if (null != properties.getAnnotationClass()) {
                scanner.setAnnotationClass(properties.getAnnotationClass());
            } else {
                scanner.setAnnotationClass(Mapper.class);
            }
            Class<? extends MapperFactoryBean> mapperFactoryBeanClass = properties.getFactoryBean();
            if (null != mapperFactoryBeanClass && !MapperFactoryBean.class.equals(mapperFactoryBeanClass)) {
                scanner.setMapperFactoryBean(BeanUtils.instantiateClass(mapperFactoryBeanClass));
            }
            scanner.registerFilters();
            scanner.doScan(StringUtils.toStringArray(basePackages));
        } catch (IllegalStateException ex) {
            log.debug("Could not determine auto-configuration package, automatic mapper scanning disabled.", ex);
        }
    }


    /**
     * {@link org.mybatis.spring.annotation.MapperScan} ultimately ends up
     * creating instances of {@link MapperFactoryBean}. If
     * {@link org.mybatis.spring.annotation.MapperScan} is used then this
     * auto-configuration is not needed. If it is _not_ used, however, then this
     * will bring in a bean registrar and automatically register components based
     * on the same component-scanning path as Spring Boot itself.
     */
    @Configuration
    @ConditionalOnMissingBean(MapperFactoryBean.class)
    public static class MapperScannerRegistrarNotFoundConfiguration {

        @PostConstruct
        public void afterPropertiesSet() {
            log.debug("No {} found.", MapperFactoryBean.class.getName());
        }
    }


}
