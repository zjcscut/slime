package org.throwable.mapper.support.handler;

import com.google.common.collect.Maps;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author throwable
 * @version v1.0
 * @function
 * @since 2017/4/13 15:48
 */
public abstract class AbstractMappedSqlSourceBuilder {

	private final static ConcurrentMap<String, Boolean> dynamicMappedStatements = Maps.newConcurrentMap();

	protected void addDynamicMappedStatement(String msId) {
		dynamicMappedStatements.putIfAbsent(msId, Boolean.TRUE);
	}

	protected boolean existMappedStatement(String msId){
		return dynamicMappedStatements.containsKey(msId);
	}

	protected abstract SqlSource createScriptSqlSource(Configuration configuration, String msId, String scriptSql, Class<?> parameterType);

	public abstract boolean hasRegisterMappedStatement(String msId);

}
