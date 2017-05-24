package org.throwable.mapper.support.filter.impl;

import com.google.common.collect.Sets;
import org.throwable.mapper.common.constant.CommonConstants;
import org.throwable.mapper.support.filter.FieldFilter;

import java.util.Set;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/3/31 0:17
 */
public class ExcludeFieldFilter implements FieldFilter {

	private final String fields;

	public ExcludeFieldFilter(String fields) {
		this.fields = fields;
	}

	@Override
	public boolean isInculdeFilter() {
		return false;
	}

	@Override
	public Set<String> accept() {
		return Sets.newHashSet(fields.split(CommonConstants.COMMA));
	}
}
