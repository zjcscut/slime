package org.throwable.mapper.support.handler;

import org.throwable.utils.Strings;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/3/30 23:13
 */
public class CamelCaseToUnderLineLowerNameStyleHandler implements NameStyleHandler {

	@Override
	public String contert(String input) {
		return Strings.camelhumpToUnderline(input).toLowerCase();
	}
}
