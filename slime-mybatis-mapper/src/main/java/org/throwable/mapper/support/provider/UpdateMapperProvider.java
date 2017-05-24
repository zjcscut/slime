package org.throwable.mapper.support.provider;

import lombok.val;
import org.apache.ibatis.mapping.MappedStatement;
import org.throwable.mapper.support.assist.MapperTemplateAssistor;
import org.throwable.mapper.support.repository.AbstractMapperTemplate;

import static org.throwable.mapper.common.constant.CommonConstants.PARAM_ALLOW_UPDATE_TO_NULL;
import static org.throwable.mapper.common.constant.CommonConstants.PARAM_RECORD;
import static org.throwable.mapper.support.assist.UpdateSqlAppendAssistor.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/4 0:02
 */
public class UpdateMapperProvider extends AbstractMapperTemplate {

	public UpdateMapperProvider(Class<?> mapperClass, MapperTemplateAssistor mapperTemplateAssistor) {
		super(mapperClass, mapperTemplateAssistor);
	}

	public String updateByPrimaryKey(MappedStatement ms) {
		val entityClass = getEntityClass(ms);
		StringBuilder builder = new StringBuilder();
		builder.append(checkParamValue(PARAM_RECORD, PARAM_ALLOW_UPDATE_TO_NULL));
		builder.append(updateTable(entityClass, tableName(entityClass), PARAM_RECORD));
		builder.append(updateSetColumns(entityClass));
		builder.append(primaryKeyWhereClause(entityClass, PARAM_RECORD));
		return builder.toString();
	}

}
