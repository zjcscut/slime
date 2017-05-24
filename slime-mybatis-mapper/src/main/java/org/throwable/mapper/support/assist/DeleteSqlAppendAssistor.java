package org.throwable.mapper.support.assist;

import static org.throwable.mapper.support.assist.EntityTableAssisor.getPrimaryColumns;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/4 2:02
 */
public abstract class DeleteSqlAppendAssistor extends ConditionSqlAppendAssistor {

	public static String deleteFromTable(Class<?> entityClass, String defaultTableName) {
		return "DELETE FROM " + getDynamicTableName(entityClass, defaultTableName) + " ";
	}

	public static String deletePrimaryKeyWhereClause(Class<?> entityClass) {
		final StringBuilder builder = new StringBuilder();
		builder.append("<where>\n");
		getPrimaryColumns(entityClass).forEach(column -> builder.append(" AND ").append(getColumnEqualsHolder(column)));
		builder.append("\n</where>\n");
		return builder.toString();
	}

}
