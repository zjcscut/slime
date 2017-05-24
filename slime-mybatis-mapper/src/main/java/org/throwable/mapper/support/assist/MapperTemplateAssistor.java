package org.throwable.mapper.support.assist;

import lombok.Getter;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.throwable.exception.BeanRegisterHandleException;
import org.throwable.mapper.configuration.prop.PropertiesConfiguration;
import org.throwable.mapper.support.provider.EmptyProvider;
import org.throwable.mapper.support.repository.AbstractMapperTemplate;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/4 0:26
 */
@Getter
public class MapperTemplateAssistor {

	/**
	 * 缓存skip结果
	 */
	private final Map<String, Boolean> msIdSkip = new HashMap<>();

	/**
	 * 注册的接口
	 */
	private List<Class<?>> registerClass = new ArrayList<>();

	/**
	 * 注册的通用Mapper接口
	 */
	private Map<Class<?>, AbstractMapperTemplate> registerMapper = new ConcurrentHashMap<>();

	/**
	 * 缓存msid和MapperTemplate
	 */
	private Map<String, AbstractMapperTemplate> msIdCache = new HashMap<>();

	/**
	 * Mapper配置
	 */
	private final PropertiesConfiguration config;

	public MapperTemplateAssistor(PropertiesConfiguration config) {
		this.config = config;
	}

	/**
	 * 核心方法,注册所有provider中的方法
	 */
	private AbstractMapperTemplate registerMapperClassMethod(Class<?> mapperClass) {
		Method[] methods = mapperClass.getDeclaredMethods();
		Class<?> templateClass = null;
		Class<?> tempClass = null;
		Set<String> methodSet = new HashSet<>();
		for (Method method : methods) {
			if (method.isAnnotationPresent(SelectProvider.class)) {
				SelectProvider provider = method.getAnnotation(SelectProvider.class);
				tempClass = provider.type();
				methodSet.add(method.getName());
			} else if (method.isAnnotationPresent(InsertProvider.class)) {
				InsertProvider provider = method.getAnnotation(InsertProvider.class);
				tempClass = provider.type();
				methodSet.add(method.getName());
			} else if (method.isAnnotationPresent(DeleteProvider.class)) {
				DeleteProvider provider = method.getAnnotation(DeleteProvider.class);
				tempClass = provider.type();
				methodSet.add(method.getName());
			} else if (method.isAnnotationPresent(UpdateProvider.class)) {
				UpdateProvider provider = method.getAnnotation(UpdateProvider.class);
				tempClass = provider.type();
				methodSet.add(method.getName());
			}
			if (templateClass == null) {
				templateClass = tempClass;
			} else if (templateClass != tempClass) {
				throw new BeanRegisterHandleException("一个Smart-Mapper中只允许存在一个AbstractMapperTemplate子类!");
			}
		}
		if (templateClass == null || !AbstractMapperTemplate.class.isAssignableFrom(templateClass)) {
			templateClass = EmptyProvider.class;
		}
		AbstractMapperTemplate mapperTemplate;
		try {
			mapperTemplate = (AbstractMapperTemplate) templateClass.getConstructor(Class.class, MapperTemplateAssistor.class).newInstance(mapperClass, this);
		} catch (Exception e) {
			throw new BeanRegisterHandleException(String.format("实例化AbstractMapperTemplate对象失败:%s", e));
		}
		//注册方法
		for (String methodName : methodSet) {
			try {
				mapperTemplate.addMethodMap(methodName, templateClass.getMethod(methodName, MappedStatement.class));
			} catch (NoSuchMethodException e) {
				throw new BeanRegisterHandleException(String.format("%s中缺少%s方法!", templateClass.getCanonicalName(), methodName));
			}
		}
		return mapperTemplate;
	}


	public void registerMapper(Class<?> mapperClass) {
		if (!registerMapper.containsKey(mapperClass)) {
			registerClass.add(mapperClass);
			registerMapper.put(mapperClass, registerMapperClassMethod(mapperClass));
		}
		//自动注册继承的接口
		Class<?>[] interfaces = mapperClass.getInterfaces();
		if (null != interfaces && interfaces.length > 0) {
			for (Class<?> anInterface : interfaces) {
				registerMapper(anInterface);
			}
		}
	}


	public boolean isMapperMethod(String msId) {
		if (msIdSkip.get(msId) != null) {
			return msIdSkip.get(msId);
		}
		for (Map.Entry<Class<?>, AbstractMapperTemplate> entry : registerMapper.entrySet()) {
			if (entry.getValue().supportMethod(msId)) {
				msIdSkip.put(msId, true);
				msIdCache.put(msId, entry.getValue());
				return true;
			}
		}
		msIdSkip.put(msId, false);
		return false;
	}


	public boolean isExtendCommonMapper(Class<?> mapperInterface) {
		for (Class<?> mapperClass : registerClass) {
			if (mapperClass.isAssignableFrom(mapperInterface)) {
				return true;
			}
		}
		return false;
	}


	public void rewriteSqlSource(MappedStatement ms) {
		AbstractMapperTemplate mapperTemplate = msIdCache.get(ms.getId());
		try {
			if (mapperTemplate != null) {
				mapperTemplate.rewriteSqlSource(ms);
			}
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}


	public void processConfiguration(Configuration configuration) {
		processConfiguration(configuration, null);
	}

	public void processConfiguration(Configuration configuration, Class<?> mapperInterface) {
		String prefix;
		if (mapperInterface != null) {
			prefix = mapperInterface.getCanonicalName();
		} else {
			prefix = "";
		}
		//重新设置所有的MappedStatements的SqlSource -> 关键步骤
		List<Object> msList = new ArrayList<>(configuration.getMappedStatements());
		msList.forEach(o -> {
			if (o instanceof MappedStatement) {
				MappedStatement ms = (MappedStatement) o;
				if (ms.getId().startsWith(prefix) && isMapperMethod(ms.getId())) {
					if (ms.getSqlSource() instanceof ProviderSqlSource) {
						rewriteSqlSource(ms);
					}
				}
			}
		});
	}
}
