package org.throwable.mapper.support.repository;

import lombok.Getter;
import lombok.Setter;
import org.throwable.mapper.common.entity.EntityTable;
import org.throwable.mapper.configuration.prop.PropertiesConfiguration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/2 13:25
 */
public abstract class EntityInfoRepository {

	@Getter
	@Setter
	protected static PropertiesConfiguration configuration;

	/**
	 * 实体类 => 表对象
	 */
	protected static final ConcurrentMap<Class<?>, EntityTable> entityTableMap = new ConcurrentHashMap<>();

}
