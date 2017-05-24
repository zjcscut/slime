package org.throwable.mapper.utils;

import jodd.util.StringUtil;
import org.throwable.mapper.support.assist.SqlAppendAssistor;
import org.throwable.mapper.support.plugins.condition.Condition;
import org.throwable.mapper.support.plugins.generator.table.DynamicTableName;
import org.throwable.utils.Strings;


@SuppressWarnings("unused")
public abstract class OGNL {

	private final static String CLASS_NAME = "@org.throwable.mapper.utils.OGNL";

	public final static String TRIM_AND_TO_LOWERCASE = CLASS_NAME + "@trimAndToLowerCase(%s)";

	public final static String REMOVE_DELIMITER = CLASS_NAME + "@removeDelimiter(%s)";

	public final static String ADD_DELIMITER = CLASS_NAME + "@addDelimiter(%s)";

	public final static String CHECK_FOR_NULL = CLASS_NAME + "@checkForNull(%s,%s)";

	public final static String CHECK_FOR_BLANK = CLASS_NAME + "@checkForBlank(%s,%s)";

	public static String trimAndToLowerCase(String s) {
		return Strings.trimAndToLowerCase(s);
	}

	public static String removeDelimiter(String column) {
		return SqlAppendAssistor.removeDelimiter(column);
	}

	public static String addDelimiter(String column) {
		return SqlAppendAssistor.addDelimiter(column);
	}

	public static boolean checkForNull(Object parameter, String messageIfNull) {
		if (null == parameter) {
			throw new IllegalArgumentException(messageIfNull);
		}
		return true;
	}

	public static boolean checkForBlank(String parameter, String messageIfBlank) {
		if (StringUtil.isBlank(parameter)) {
			throw new IllegalArgumentException(messageIfBlank);
		}
		return true;
	}

	/**
	 * 包含字段过滤器
	 *
	 * @param parameter 入参
	 */
	public static boolean hasFieldFilter(Object parameter) {
		if (parameter != null && parameter instanceof Condition) {
			Condition condition = (Condition) parameter;
			return null != condition.getFieldFilter();
		}
		return false;
	}

	/**
	 * 不包含字段过滤器
	 *
	 * @param parameter 入参
	 */
	public static boolean hasNoFieldFilter(Object parameter) {
		return !hasFieldFilter(parameter);
	}

	/**
	 * 包含自定义查询字段
	 *
	 * @param parameter 入参
	 */
	public static boolean hasSelectColumns(Object parameter) {
		if (parameter != null && parameter instanceof Condition) {
			Condition condition = (Condition) parameter;
			return null != condition.getSelectColumns() && condition.getSelectColumns().size() > 0;
		}
		return false;
	}

	/**
	 * 不包含自定义查询字段
	 *
	 * @param parameter 入参
	 */
	public static boolean hasNotSelectColumns(Object parameter) {
		return !hasSelectColumns(parameter);
	}

	/**
	 * 判断参数是否支持动态表名
	 *
	 * @param parameter 入参
	 * @return true支持，false不支持
	 */
	public static boolean isDynamicParameter(Object parameter) {
		return parameter != null && parameter instanceof DynamicTableName;
	}

	/**
	 * 判断参数是否b支持动态表名
	 *
	 * @param parameter
	 * @return true不支持，false支持
	 */
	public static boolean isNotDynamicParameter(Object parameter) {
		return !isDynamicParameter(parameter);
	}

	/**
	 * 是否含有排序子句
	 *
	 * @param parameter
	 * @return
	 */
	public static boolean hasOrderByClause(Object parameter) {
		if (parameter != null && parameter instanceof Condition) {
			Condition condition = (Condition) parameter;
			return condition.getSort().getOrders().size() > 0;
		}
		return false;
	}
}
