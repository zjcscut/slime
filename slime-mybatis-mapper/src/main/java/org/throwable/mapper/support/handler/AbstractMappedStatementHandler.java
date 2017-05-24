package org.throwable.mapper.support.handler;

import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.session.Configuration;

import java.util.List;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/11 11:37
 */
public abstract class AbstractMappedStatementHandler extends AbstractMappedSqlSourceBuilder {

	protected abstract SqlSource createCustomSqlSource(Configuration configuration);

	protected abstract SqlSource createDynamicSqlSource(Configuration configuration, SqlNode sqlNode);

	protected abstract SqlSource createRawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType);

	protected abstract SqlSource createRawSqlSource(Configuration configuration, String sql, Class<?> parameterType);

	protected abstract SqlSource createStaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings);

	protected abstract MappedStatement.Builder createMappedStatementBuilder(Configuration configuration, String msId, SqlSource sqlSource, SqlCommandType sqlCommandType);

	protected abstract ParameterMap createParameterMap(Configuration configuration, String pmId, Class<?> type, List<ParameterMapping> parameterMappings);

	protected abstract ResultMap createResultMap(Configuration configuration, String id, Class<?> type, List<ResultMapping> resultMappings, Boolean autoMapping);
}
