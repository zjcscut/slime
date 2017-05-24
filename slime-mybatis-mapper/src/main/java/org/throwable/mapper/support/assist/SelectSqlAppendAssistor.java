package org.throwable.mapper.support.assist;

import org.throwable.mapper.common.constant.CommonConstants;
import org.throwable.mapper.common.entity.EntityColumn;
import org.throwable.mapper.support.plugins.condition.Condition;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/4 2:02
 */
public abstract class SelectSqlAppendAssistor extends ConditionSqlAppendAssistor {


	/**
	 * select指定列，支持distinct查询
	 */
	public static String selectColumnsByCondition(Class<?> entityClass) {
		//不支持指定列的时候查询全部列
		return "SELECT <if test=\"condition neq null and condition.isDistinct\">DISTINCT</if>" +
				"\n" +
				"<if test=\"condition neq null and @org.throwable.mapper.utils.OGNL@hasSelectColumns(condition)\">" +
				"\n" +
				"  <foreach collection=\"condition.selectColumns\" item=\"selectColumn\" separator=\",\">" +
				"\n" +
				"    ${selectColumn}" +
				"\n" +
				"  </foreach>" +
				"\n" +
				"</if>" +
				"\n" +
				"<if test=\"condition eq null or @org.throwable.mapper.utils.OGNL@hasNotSelectColumns(condition)\">" +
				getSelectColumnsClause(entityClass) +
				"</if>" +
				"\n";
	}


	/**
	 * from tableName - 动态表名
	 *
	 * @param entityClass      实体类
	 * @param defaultTableName 默认表名
	 */
	public static String fromTable(Class<?> entityClass, String defaultTableName) {
		return " FROM " + getDynamicTableName(entityClass, defaultTableName) + " \n";
	}

	public static String fromDynamicTable() {
		return " FROM ${dynamicTableName} ";
	}

	public static String selectColumnsByCondition(Class<?> clazz, Condition condition) {
		Set<String> selectColumnSet = condition.getSelectColumns();
		Set<EntityColumn> allColumns = EntityTableAssisor.getAllColumns(clazz);
		StringBuilder sql = new StringBuilder(buildSelectClause());
		if (null != selectColumnSet && !selectColumnSet.isEmpty()) {
			sql.append(buildSelectSetColumnsClause(selectColumnSet, allColumns));
		} else {
			sql.append(buildSelectAllColumnsClause(allColumns));
		}
		return sql.toString();
	}

	private static String buildSelectSetColumnsClause(Set<String> selectColumnSet, Set<EntityColumn> allColumns) {
		Set<EntityColumn> selectColumns =
				allColumns.stream().filter(column -> selectColumnSet.contains(column.getColumn()))
						.collect(Collectors.toSet());
		return buildSelectColumnPair(selectColumns);
	}

	private static String buildSelectAllColumnsClause(Set<EntityColumn> allColumns) {
		return buildSelectColumnPair(allColumns);
	}

	private static String buildSelectColumnPair(Set<EntityColumn> columns) {
		StringBuilder sql = new StringBuilder();
		String template = "\n<trim suffixOverrides=\",\">\n" +
				"%s" +
				"\n</trim>\n";
		columns.forEach(column -> sql.append(column.getColumn()).append(CommonConstants.COMMA));
		return String.format(template, sql.toString());
	}

	private static String buildSelectClause() {
		return "SELECT <if test=\"condition neq null and condition.isDistinct\">DISTINCT</if>";
	}

	public static String selectCountStar() {
		return "SELECT COUNT(*)";
	}

}
