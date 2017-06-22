package org.throwable.mapper.configuration.prop;

import com.google.common.collect.Lists;
import jodd.util.StringUtil;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.throwable.mapper.common.constant.NameStyleEnum;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/3 23:25
 */
@Data
@Slf4j
@NoArgsConstructor
@ConfigurationProperties(prefix = MapperProperties.MYBATIS_PREFIX)
public class MapperProperties {

    public static final String MYBATIS_PREFIX = "slime.mybatis.mapper";

    /**
     * Config file path.
     */
    private String configLocation;

    /**
     * Location of mybatis mapper files.
     */
    private String[] mapperLocations;

    /**
     * Package to scan domain objects.
     */
    private String typeAliasesPackage;

    /**
     * Package to scan handlers.
     */
    private String typeHandlersPackage;

    /**
     * Check the config file exists.
     */
    private boolean checkConfigLocation = false;

    /**
     * Execution mode for {@link org.mybatis.spring.SqlSessionTemplate}.
     */
    private ExecutorType executorType;

    /**
     * Externalized properties for configuration.
     */
    private Properties configurationProperties;


    /**
     * 需要扫描的mapper所在的包
     */
    private String[] basePackages = {};

    /**
     * mapper识别注解
     */
    private Class<? extends Annotation> annotationClass = Mapper.class;

    /**
     * MapperFactoryBean的子类,提供重写的入口
     */
    private Class<? extends MapperFactoryBean> factoryBean = MapperFactoryBean.class;

    /**
     * OGNL主键策略 - 当@GeneratedValue的generator = “UUID”,生成主键会使用此策略,此策略使用Mybatis的OGNL表达式,支持外部提供的主键生成方法
     * 目前暂时不支持多主键或者联合主键,只支持单主键
     */
    private String ognlIdentityStrategy;

    /**
     * selectKey顺序策略,若自增需要设置为false,如果需要外部写入主键(使用OGNL主键策略)必须设置为true,默认为false
     */
    private Boolean selectKeyExecuteBefore;
    /**
     * catalog
     */
    private String catalog;

    /**
     * schema
     */
    private String schema;

    /**
     * 命名转换规范
     */
    private String nameStyle;

    /**
     * Environment对象无法获取Yaml文件的列表类型数据,因此需要使用Properties下标获取
     */
    @SuppressWarnings("unchecked")
    public MapperProperties(Environment env) {
        String packagesKey = "slime.mybatis.mapper.base-packages";
        int index = 0;
        List<String> packages = Lists.newArrayList();
        while (null != env.getProperty(packagesKey + "[" + index + "]")) {
            packages.add(env.getProperty(packagesKey + "[" + index + "]"));
            index++;
        }
        if (packages.size() > 0) {
            basePackages = packages.toArray(new String[packages.size()]);
        }
        String clazz = env.getProperty("slime.mybatis.mapper.annotation-class", String.class);
        if (StringUtil.isNotBlank(clazz)) {
            try {
                Class<?> annotationClazz = Class.forName(clazz);
                if (Annotation.class.isAssignableFrom(annotationClazz)) {
                    this.annotationClass = (Class<? extends Annotation>) annotationClazz;
                }
            } catch (ClassNotFoundException e) {
                log.error("load annotationClass fail", e);
            }
        }
        String bean = env.getProperty("slime.mybatis.mapper.factory-bean", String.class);
        if (StringUtil.isNotBlank(bean)) {
            try {
                Class<?> beanClazz = Class.forName(bean);
                if (MapperFactoryBean.class.isAssignableFrom(beanClazz)) {
                    this.factoryBean = (Class<? extends MapperFactoryBean>) beanClazz;
                }
            } catch (ClassNotFoundException e) {
                log.error("load MapperFactoryBean fail", e);
            }
        }
    }

    /**
     * A Configuration object for customize default settings. If {@link #configLocation}
     * is specified, this property is not used.
     */
    @NestedConfigurationProperty
    private Configuration configuration;

    public Resource[] resolveMapperLocations() {
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        List<Resource> resources = new ArrayList<>();
        if (null != this.mapperLocations) {
            for (String mapperLocation : this.mapperLocations) {
                try {
                    Resource[] mappers = resourceResolver.getResources(mapperLocation);
                    resources.addAll(Arrays.asList(mappers));
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return resources.toArray(new Resource[resources.size()]);
    }

    /**
     * 封装smart-mapper内部配置
     */
    public PropertiesConfiguration createConfiguration() {
        if (this.propertiesConfiguration != null) {
            return this.propertiesConfiguration;
        }
        PropertiesConfiguration configuration = new PropertiesConfiguration();
        if (StringUtil.isNotBlank(this.ognlIdentityStrategy)) {
            configuration.setOgnlIdentityStrategy(this.ognlIdentityStrategy.replace("\\", "").trim());
        }
        if (StringUtil.isNotBlank(this.catalog)) {
            configuration.setCatalog(this.catalog);
        }
        if (StringUtil.isNotBlank(this.schema)) {
            configuration.setSchema(this.schema);
        }
        if (null != this.selectKeyExecuteBefore) {
            configuration.setSelectKeyExecuteBefore(this.selectKeyExecuteBefore);
        }
        if (StringUtil.isNotBlank(this.nameStyle)) {
            configuration.setStyle(NameStyleEnum.search(this.nameStyle));
        }
        this.propertiesConfiguration = configuration;
        return configuration;
    }

    @Getter
    private PropertiesConfiguration propertiesConfiguration;
}
