package org.throwable.mapper.support.filter;

import java.util.Set;

/**
 * @author throwable
 * @version v1.0
 * @description 域过滤器
 * @since 2017/3/31 0:01
 */
public interface FieldFilter {

	boolean isInculdeFilter();

	Set<String> accept();
}
