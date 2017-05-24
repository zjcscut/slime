package org.throwable.mapper.support.assist;

import com.google.common.collect.Sets;
import org.throwable.mapper.common.constant.NameStyleEnum;
import org.throwable.mapper.common.entity.EntityColumn;
import org.throwable.mapper.support.filter.FieldFilter;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author throwable
 * @version v1.0
 * @function
 * @since 2017/4/6 0:38
 */
public abstract class FieldFilterAssistor {

	public static Set<EntityColumn> filter(Set<EntityColumn> entityColumns, FieldFilter fieldFilter) {
		if (null == entityColumns || entityColumns.size() == 0) {
			return Sets.newHashSet();
		}
		Set<String> filterFields = fieldFilter.accept();
		if (fieldFilter.isInculdeFilter()) {
			return entityColumns.stream()
					.filter(a -> filterFields.contains(a.getProperty()))
					.collect(Collectors.toSet());
		} else {
			return entityColumns.stream()
					.filter(a -> !filterFields.contains(a.getProperty()))
					.collect(Collectors.toSet());
		}
	}

	public static Set<EntityColumn> getFilterColumns(Class<?> entityClass, FieldFilter fieldFilter, boolean skipPrimaryKey) {
		Set<EntityColumn> columnList = skipPrimaryKey ? EntityTableAssisor.getNonePrimaryColumns(entityClass)
				: EntityTableAssisor.getAllColumns(entityClass);
		if (null != fieldFilter) {
			columnList = filter(columnList, fieldFilter);
		}
		return columnList;
	}
}
