package org.throwable.mapper.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.throwable.mapper.configuration.prop.MapperProperties;
import org.throwable.mapper.configuration.prop.PropertiesConfiguration;
import org.throwable.mapper.support.assist.MapperTemplateAssistor;
import org.throwable.mapper.support.repository.EntityInfoRepository;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.stream.Stream;

/**
 * @author throwable
 * @version v1.0
 * @description 动态注册mapper
 * @since 2017/4/4 0:14
 */
@Configuration
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
@ConditionalOnBean({DataSource.class, SqlSessionFactory.class})
@EnableConfigurationProperties({MapperProperties.class})
@Slf4j
public class MapperAutoConfiguration implements InitializingBean {

	private final SqlSessionFactory sqlSessionFactory;

	private final MapperProperties properties;

	public MapperAutoConfiguration(SqlSessionFactory sqlSessionFactory, MapperProperties properties) {
		this.sqlSessionFactory = sqlSessionFactory;
		this.properties = properties;
	}

	/**
	 * 关键步骤
	 * 0:缓存全局配置
	 * 1:注册SmartMapper
	 * 2:动态注册所有自定义的MappedStatement
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		cacheSmartMapperGlobalConfiguration(properties.createConfiguration());
		registryMappers();
		dynamicRegistryMappedStatements();
	}

	private void registryMappers() {
		PropertiesConfiguration configuration = properties.getPropertiesConfiguration();
		MapperTemplateAssistor assistor = new MapperTemplateAssistor(configuration);
		Stream.of(configuration.getRegisterMappers()).forEach(assistor::registerMapper);
		assistor.processConfiguration(sqlSessionFactory.getConfiguration());
	}

	private void dynamicRegistryMappedStatements() {

	}

	/**
	 * 缓存全局配置
	 *
	 * @param configuration configuration
	 */
	private void cacheSmartMapperGlobalConfiguration(PropertiesConfiguration configuration) {
		try {
			Field target = EntityInfoRepository.class.getDeclaredField("configuration");
			target.setAccessible(true);
			target.set("configuration", configuration);
			target.setAccessible(false);
		} catch (Exception e) {
			log.error("cache SmartMapper global configuration failed!", e);
		}
	}

}
