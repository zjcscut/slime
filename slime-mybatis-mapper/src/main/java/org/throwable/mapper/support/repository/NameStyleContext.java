package org.throwable.mapper.support.repository;

import org.throwable.mapper.common.constant.NameStyleEnum;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/1 0:03
 */
public final class NameStyleContext {


	public static String convert(NameStyleEnum styleEnum, String input) {
		return NameStyleHandlerFactory
				.getInstance()
				.create(styleEnum)
				.contert(input);
	}
	
}
