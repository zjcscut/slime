package org.throwable.mapper.support.assist;

import jodd.introspector.ClassIntrospector;
import jodd.introspector.FieldDescriptor;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.throwable.exception.UnsupportedElementException;
import org.throwable.mapper.common.entity.EntityColumn;
import org.throwable.mapper.support.plugins.generator.table.DynamicTableName;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.throwable.mapper.common.constant.CommonConstants.PARAM_DEFAULT;
import static org.throwable.mapper.utils.OGNL.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/4 0:07
 */
@Slf4j
public abstract class SqlAppendAssistor extends FieldFilterAssistor {

	public static Optional<EntityColumn> getIdentityColumn(final Class<?> entityClass) {
		Set<EntityColumn> columnList = EntityTableAssisor.getPrimaryColumns(entityClass).stream()
				.filter(EntityColumn::isInsertable)
				.filter(EntityColumn::isIdentity)
				.collect(toSet());
		if (columnList.size() == 0) {
			return Optional.empty();
		}
		if (columnList.size() > 1) {
			throw new UnsupportedElementException("Too many identity columns defined in " + entityClass.getCanonicalName());
		}
		return Optional.of(columnList.iterator().next());
	}

	public static String checkDefaultParamValue() {
		return checkParamValue(PARAM_DEFAULT);
	}

	public static String checkParamValue(String... parameterNames) {
		if (parameterNames.length == 1) {
			String exp = format(CHECK_FOR_NULL, parameterNames[0], "'parameter must not be null'");
			return "<if test=\"" + exp + "\"/>\n";
		}
		return Stream.of(parameterNames)
				.map(parameterName -> {
					String exp = format(CHECK_FOR_NULL, parameterName, "'any parameter must not be null'");
					return "<if test=\"" + exp + "\"/>\n";
				})
				.reduce(String::concat)
				.orElse("");
	}

	public static String removeDelimiter(String column) {
		if (column == null) {
			return null;
		}
		if (column.startsWith("`") && column.endsWith("`")) {
			return column.substring(1, column.length() - 1);
		}
		return column;
	}

	public static String addDelimiter(String column) {
		if (column == null) {
			return null;
		}
		if (column.startsWith("`") && column.endsWith("`")) {
			return column;
		}
		return "`" + column + "`";
	}

	public static String getSingleColumnPair(Class<?> entityClass) {
		Set<EntityColumn> columns = EntityTableAssisor.getAllColumns(entityClass);
		String field = format(TRIM_AND_TO_LOWERCASE, "field");
		String fieldWithoutDelimiter = format(REMOVE_DELIMITER, field);
		String fieldWithDelimiter = format(ADD_DELIMITER, field);

		StringBuilder sql = new StringBuilder();
		sql.append("<if test=\"field eq null or field eq ''\">").append(" = #{value}").append("</if>");
		sql.append("<if test=\"field neq null and field neq ''\">");
		sql.append("<choose>");
		for (EntityColumn entityColumn : columns) {
			String property = entityColumn.getProperty().toLowerCase();
			String column = removeDelimiter(entityColumn.getColumn().toLowerCase());
			sql.append("<when test=\"");
			sql.append(fieldWithoutDelimiter).append(" eq '").append(property).append("'");
			if (!Objects.equals(property, column)) {
				sql.append(" or ").append(fieldWithoutDelimiter).append(" eq '").append(column).append("'");
			}
			sql.append("\">");
			sql.append(getColumnEqualsHolder("value", entityColumn, false));
			sql.append("</when>");
		}
		sql.append("<otherwise>");
		sql.append("${").append(fieldWithDelimiter).append("} = #{value}");
		sql.append("</otherwise>");
		sql.append("</choose>");
		sql.append("</if>");
		return sql.toString();
	}

	/**
	 * 指定列是否定义为not null
	 *
	 * @param column 数据库列
	 */
	public static boolean isNotNull(EntityColumn column) {
		if (column == null) {
			throw new IllegalArgumentException("column must not be null");
		}
		Class<?> entityClass = column.getTable().getEntityClass();
		String property = column.getProperty();
		FieldDescriptor fieldDescriptor = ClassIntrospector.lookup(entityClass).getFieldDescriptor(property, true);
		return fieldDescriptor != null && fieldDescriptor.getField().getAnnotation(NotNull.class) != null;
	}

	/**
	 * <if test="column neq null">content</if>
	 *
	 * @param column  数据库列
	 * @param content 条件满足时的内容
	 */
	public static String getIfNotNull(EntityColumn column, String content) {
		return getIfNotNull(PARAM_DEFAULT, column, content);
	}

	/**
	 * <if test="entity.column neq null">content</if>
	 *
	 * @param column  数据库列
	 * @param content 条件满足时的内容
	 */
	public static String getIfNotNull(String parameterName, EntityColumn column, String content) {
		return getIfNotNull(getEntityPrefix(parameterName) + column.getProperty(), content);
	}

	/**
	 * <if test="testValue neq null">content</if>
	 *
	 * @param testValue 测试值
	 * @param content   条件满足时的内容
	 */
	public static String getIfNotNull(String testValue, String content) {
		return "<if test=\"" + testValue + " neq null\">" + content + "</if>";
	}


	/**
	 * 返回格式如：columnName = #{property,jdbcType=NUMERIC,typeHandler=MyTypeHandler}
	 */
	public static String getColumnEqualsHolder(EntityColumn column) {
		return getColumnEqualsHolder(PARAM_DEFAULT, column);
	}

	/**
	 * 返回格式：columnName = #{[parameterName.]property,jdbcType=NUMERIC,typeHandler=MyTypeHandler}
	 */
	public static String getColumnEqualsHolder(String parameterName, EntityColumn column) {
		return getColumnEqualsHolder(parameterName, column, true);
	}

	/**
	 * 返回格式：columnName = #{parameterName/[parameterName.]property,jdbcType=NUMERIC,typeHandler=MyTypeHandler}
	 */
	public static String getColumnEqualsHolder(String parameterName, EntityColumn column, boolean addProperty) {
		return column.getColumn() + " = " + getColumnHolder(parameterName, column, addProperty, "");
	}

	/**
	 * 返回格式：#{property,jdbcType=NUMERIC,typeHandler=MyTypeHandler},
	 */
	public static String getColumnHolderWithComma(EntityColumn column) {
		return getColumnHolderWithComma(PARAM_DEFAULT, column);
	}

	/**
	 * 返回格式：#{[parameterName.]property,jdbcType=NUMERIC,typeHandler=MyTypeHandler},
	 *
	 * @param parameterName @Param里的参数，如果方法只有一个参数，则mybatis会解析成_parameter
	 * @param column        数据库列
	 */
	public static String getColumnHolderWithComma(String parameterName, EntityColumn column) {
		return getColumnHolder(parameterName, column, ",");
	}

	/**
	 * 返回格式：#{property,jdbcType=NUMERIC,typeHandler=MyTypeHandler}
	 *
	 * @param column 数据库列
	 */
	public static String getColumnHolder(EntityColumn column) {
		return getColumnHolder(PARAM_DEFAULT, column);
	}

	/**
	 * 返回格式：#{[parameterName.]property,jdbcType=NUMERIC,typeHandler=MyTypeHandler}
	 *
	 * @param parameterName @Param里的参数，如果方法只有一个参数，则mybatis会解析成_parameter
	 * @param column        数据库列
	 */
	static String getColumnHolder(String parameterName, EntityColumn column) {
		return getColumnHolder(parameterName, column, "");
	}

	/**
	 * 返回格式如：#{[parameterName.]property,jdbcType=NUMERIC,typeHandler=MyTypeHandler}+separator
	 */
	private static String getColumnHolder(String parameterName, EntityColumn column, String separator) {
		return getColumnHolder(parameterName, column, true, separator);
	}

	/**
	 * 返回格式如：#{parameterName/[parameterName].property,jdbcType=NUMERIC,typeHandler=MyTypeHandler}+separator
	 *
	 * @param parameterName @Param里的参数，如果方法只有一个参数，则mybatis会解析成_parameter
	 * @param column        数据库列
	 * @param addProperty   是否加 .property
	 * @param separator     末尾分隔符
	 */
	private static String getColumnHolder(String parameterName, EntityColumn column, boolean addProperty, String separator) {
		StringBuilder sb = new StringBuilder();
		sb.append("#{");
		if (addProperty) {
			sb.append(getEntityPrefix(parameterName)).append(column.getProperty());
		} else {
			if (StringUtil.isNotEmpty(parameterName)) {
				sb.append(parameterName);
			} else {
				throw new UnsupportedOperationException("参数名和属性名必须保留一个，请考虑设置具体的parameterName或设置addProperty=true");
			}
		}
		if (column.getJdbcType() != null) {
			sb.append(",jdbcType=").append(column.getJdbcType());
		} else if (column.getTypeHandler() != null) {
			sb.append(",typeHandler=").append(column.getTypeHandler().getCanonicalName());
		} else if (!column.getJavaType().isArray()) {
			sb.append(",javaType=").append(column.getJavaType().getCanonicalName());
		} else if (column.getJavaType().isArray()) {
			//当类型为数组时，不用设置javaType，mybatis会自动解析数组
			log.debug("column javaType is array");
		}
		sb.append("}");
		if (StringUtil.isNotEmpty(separator)) {
			sb.append(separator);
		}
		return sb.toString();
	}

	/**
	 * 获取表名 - 支持动态表名
	 *
	 * @param entityClass      实体类
	 * @param defaultTableName 默认表名
	 */
	static String getDynamicTableName(Class<?> entityClass, String defaultTableName) {
		return getDynamicTableName(entityClass, defaultTableName, PARAM_DEFAULT);
	}

	/**
	 * 获取表名 - 支持动态表名
	 *
	 * @param entityClass      实体类
	 * @param defaultTableName 默认表名
	 * @param parameterName    实体参数名
	 */
	static String getDynamicTableName(Class<?> entityClass, String defaultTableName, String parameterName) {
		if (DynamicTableName.class.isAssignableFrom(entityClass)) {
			String entityPrefix = getEntityPrefix(parameterName);
			return "<if test=\"@org.throwable.mapper.utils.OGNL@isDynamicParameter(" + parameterName + ") and @jodd.util.StringUtil@isNotBlank(" + entityPrefix + "dynamicTableName)\">\n" +
					"${" + entityPrefix + "dynamicTableName}\n" +
					"</if>\n" +
					"<if test=\"@org.throwable.mapper.utils.OGNL@isNotDynamicParameter(" + parameterName + ") or @jodd.util.StringUtil@isBlank(" + entityPrefix + "dynamicTableName)\">\n" +
					defaultTableName + "\n" +
					"</if>";
		} else {
			return defaultTableName;
		}
	}

	/**
	 * 获取访问实体属性的前缀
	 *
	 * @param parameterName @Param注解里的值，如果只有一个参数，则mybatis会解析为_parameter
	 */
	static String getEntityPrefix(String parameterName) {
		return Objects.equals(parameterName, PARAM_DEFAULT) ? "" : parameterName;
	}


}
