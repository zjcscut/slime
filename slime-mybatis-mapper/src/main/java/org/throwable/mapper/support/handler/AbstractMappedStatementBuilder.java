package org.throwable.mapper.support.handler;

import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.session.Configuration;
import org.throwable.mapper.common.constant.SqlSourceEnum;

import java.util.List;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/11 11:38
 */
public abstract class AbstractMappedStatementBuilder extends AbstractMappedStatementHandler {


	protected SqlSource createSqlSource(SqlSourceEnum sqlSourceEnum, Configuration configuration,
										SqlNode sqlNode, String sql,
										Class<?> parameterType, List<ParameterMapping> parameterMappings) {
		switch (sqlSourceEnum) {
			case DYNAMIC_SQLSOURCE:
				return createDynamicSqlSource(configuration, sqlNode);
			case RAW_SQLSOURCE:
				return createRawSqlSource(configuration, sql, parameterType);
			case RAW_SQLSOURCE_WITHNODE:
				return createRawSqlSource(configuration, sqlNode, parameterType);
			case STATIC_SQLSOURCE:
				return createStaticSqlSource(configuration, sql, parameterMappings);
			case CUSTOM_SQLSOURCE:
				return createCustomSqlSource(configuration);
			default: {
				return createStaticSqlSource(configuration, sql, parameterMappings);
			}
		}
	}


}
