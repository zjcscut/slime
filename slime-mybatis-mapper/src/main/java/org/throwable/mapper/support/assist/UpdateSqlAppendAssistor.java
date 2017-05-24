package org.throwable.mapper.support.assist;

import org.throwable.mapper.common.entity.EntityColumn;

import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.throwable.mapper.common.constant.CommonConstants.DOT;
import static org.throwable.mapper.common.constant.CommonConstants.PARAM_RECORD;
import static org.throwable.mapper.support.assist.EntityTableAssisor.*;
import static org.throwable.mapper.utils.OGNL.CHECK_FOR_NULL;

/**
 * @author throwable
 * @version v1.0
 * @function
 * @since 2017/4/7 18:26
 */
public abstract class UpdateSqlAppendAssistor extends ConditionSqlAppendAssistor {

	public static String checkPrimaryKeyValue(Class<?> entityClass, String parameterName) {
		return getPrimaryColumns(entityClass).stream()
				.map(column -> {
					String ognlParameter = getEntityPrefix(parameterName) + column.getProperty();
					String exp = format(CHECK_FOR_NULL, ognlParameter, "'id must not be null for update'");
					return "<if test=\"" + exp + "\"/>\n";
				})
				.reduce(String::concat)
				.orElse("");
	}

	public static String updateSetColumns(Class<?> entityClass) {
		return "<set>\n" +
				getColumnSetPairs(entityClass) +
				"</set>\n";
	}

	private static String getColumnSetPairs(Class<?> entityClass) {
		return getAllColumns(entityClass).stream()
				.filter(column -> !column.isIdentity() && column.isUpdatable() && !column.isUUID())
				.map(UpdateSqlAppendAssistor::getColumnPairHolder)
				.reduce(String::concat)
				.orElse("");
	}

	private static String getColumnPairHolder(EntityColumn column) {
		String content = column.getColumnEqualsHolder(PARAM_RECORD).concat(", ");
		if (isNotNull(column)) {
			return getIfNotNull(PARAM_RECORD, column, content);
		} else {
			return getIfAllowUpdateToNull(column, content);
		}
	}

	public static String updateDynamicTable() {
		return "UPDATE ${dynamicTableName} ";
	}

	public static String updateTable(Class<?> entityClass, String defaultTableName, String parameterName) {
		return "UPDATE " + getDynamicTableName(entityClass, defaultTableName, parameterName) + " ";
	}

	private static String getIfAllowUpdateToNull(EntityColumn column, String content) {
		String template = "<choose>\n" +
				"<when test='allowUpdateToNull'>\n" +
				"%s" +
				"\n</when>\n" +
				"<otherwise>\n" +
				"<if test='%s != null'>\n" +
				"%s" +
				"\n</if>\n" +
				"</otherwise>\n" +
				"</choose>\n";
		return format(template, content, PARAM_RECORD.concat(".").concat(column.getProperty()), content);
	}


	public static String batchUpdateSetColumns(Class<?> entityClass, boolean skipNull) {
		EntityColumn key = getPrimaryColumn(entityClass);
		Set<EntityColumn> noneKeyColumns = getNonePrimaryColumns(entityClass);
		return "\n<trim prefix=\"set\" suffixOverrides=\",\">\n" +
				getBatchUpdateColumnSetPairs(noneKeyColumns, key, skipNull) +
				"</trim>\n" +
				buildBatchUpdateByPrimaryKeyWhereClause(key);
	}

	private static String getBatchUpdateColumnSetPairs(Set<EntityColumn> noneKeyColumns, EntityColumn key, boolean skipNull) {
		StringBuilder sql = new StringBuilder();
		noneKeyColumns.forEach(column -> sql.append(buildBatchUpdateColumnSetPair(key, column, skipNull)));
		return sql.toString();
	}

	private static String buildBatchUpdateColumnSetPair(EntityColumn key, EntityColumn target, boolean skipNull) {
		return skipNull ? batchUpdateColumnSetSkipNullPair(key, target) : batchUpdateColumnSetNoneSkipNullPair(key, target);
	}

	private static String batchUpdateColumnSetNoneSkipNullPair(EntityColumn key, EntityColumn target) {
		String template = "<trim prefix=\"%s = CASE\" suffix=\"END,\">\n" +
				"<foreach collection=\"records\" item=\"record\">\n" +
				"WHEN %s = #{record.%s} THEN #{record.%s}\n" +
				"</foreach>\n" +
				"</trim>\n";
		return format(template, target.getColumn(), target.getProperty(), key.getColumn(), key.getProperty(), target.getProperty());
	}

	private static String batchUpdateColumnSetSkipNullPair(EntityColumn key, EntityColumn target) {
		String template = "<trim prefix=\"%s = CASE\" suffix=\"END,\">\n" +
				"<foreach collection=\"records\" item=\"record\">\n" +
				"<if test=\"record.%s != null\">\n" +
				"WHEN %s = #{record.%s} THEN #{record.%s}\n" +
				"</if>\n" +
				"</foreach>\n" +
				"</trim>\n";
		return format(template, target.getColumn(), target.getProperty(), key.getColumn(), key.getProperty(), target.getProperty());
	}

	private static String buildBatchUpdateByPrimaryKeyWhereClause(EntityColumn key) {
		String template = "<where> %s IN " +
				"<foreach collection=\"records\" separator=\",\" item=\"record\" open=\"(\" close=\")\">\n" +
				"<if test=\"record.%s != null\">\n" +
				"#{record.%s}\n" +
				"</if>\n" +
				"</foreach>\n" +
				"</where>\n";
		return format(template, key.getColumn(), key.getProperty(), key.getProperty());
	}

	public static String dynamicUpdateSetColumns(Class<?> entityClass, boolean skipNull) {
		Set<EntityColumn> noneKeyColumns = getNonePrimaryColumns(entityClass);
		return "\n<trim prefix=\"set\" suffixOverrides=\",\">\n" +
				buildDynamicUpdateSetPairs(noneKeyColumns, skipNull) +
				"</trim>\n";

	}

	private static String buildDynamicUpdateSetPairs(Set<EntityColumn> noneKeyColumns, boolean skipNull) {
		StringBuilder sql = new StringBuilder();
		noneKeyColumns.forEach(column -> sql.append(buildDynamicUpdateSetPair(column, skipNull)));
		return sql.toString();
	}

	private static String buildDynamicUpdateSetPair(EntityColumn target, boolean skipNull) {
		return skipNull ? buildDynamicUpdateSkipNullSetPair(target) : buildDynamicUpdateNoneSkipNullSetPair(target);
	}

	private static String buildDynamicUpdateNoneSkipNullSetPair(EntityColumn target) {
		String template = "%s = %s\n";
		return format(template, target.getColumn(), getColumnHolderWithComma(PARAM_RECORD.concat(DOT), target));
	}

	private static String buildDynamicUpdateSkipNullSetPair(EntityColumn target) {
		String template = "<if test=\"record.%s neq null\">\n" +
				"%s = %s\n" +
				"</if>\n";
		return format(template, target.getProperty(), target.getColumn(), getColumnHolderWithComma(PARAM_RECORD.concat(DOT), target));
	}

	public static String buildDynamicUpdateWhereClause(Class<?> entityClass) {
		EntityColumn key = getPrimaryColumn(entityClass);
		String template = "WHERE %s = %s";
		return format(template, key.getColumn(), getColumnHolder(PARAM_RECORD.concat(DOT), key));
	}

	public static String dynamicUpdateByCondtionSetColumns(Class<?> entityClass, Set<String> updateColumnSet, boolean skipNull) {
		Set<EntityColumn> allColumns = getAllColumns(entityClass);
		Set<EntityColumn> updateColumns = allColumns.stream()
				.filter(column -> updateColumnSet.contains(column.getColumn()))
				.collect(Collectors.toSet());
		return "\n<trim prefix=\"set\" suffixOverrides=\",\">\n" +
				buildDynamicUpdateSetPairs(updateColumns, skipNull) +
				"</trim>\n";

	}
}
