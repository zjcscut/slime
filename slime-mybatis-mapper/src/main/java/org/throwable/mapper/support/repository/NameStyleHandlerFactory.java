package org.throwable.mapper.support.repository;

import com.google.common.collect.Maps;
import org.throwable.mapper.common.constant.NameStyleEnum;
import org.throwable.mapper.support.handler.*;

import java.util.Collections;
import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description 命名转换处理器工厂
 * @since 2017/3/30 13:12
 */
public final class NameStyleHandlerFactory {

	private static final Map<NameStyleEnum, NameStyleHandler> handlers = Maps.newEnumMap(NameStyleEnum.class);

	private static final NameStyleHandlerFactory factory = new NameStyleHandlerFactory();

	static {
		handlers.put(NameStyleEnum.NORMAL, new NormalNameStyleHandler());
		handlers.put(NameStyleEnum.CAMELCASE_TO_UNDERLINE, new CamelCaseToUnderLineNameStyleHandler());
		handlers.put(NameStyleEnum.CAMELCASE_TO_UNDERLINE_LOWERCASE, new CamelCaseToUnderLineLowerNameStyleHandler());
		handlers.put(NameStyleEnum.CAMELCASE_TO_UNDERLINE_UPPERCASE, new CamelCaseToUnderLineUpperNameStyleHandler());
		handlers.put(NameStyleEnum.UPPER, new UpperCaseNameStyleHandler());
		handlers.put(NameStyleEnum.LOWER, new LowerCaseNameStyleHandler());
	}

	private NameStyleHandlerFactory() {
	}

	public static NameStyleHandlerFactory getInstance() {
		return factory;
	}


	public NameStyleHandler create(NameStyleEnum styleEnum) {
		return handlers.get(styleEnum);
	}

	public Map<NameStyleEnum, NameStyleHandler> getHandlers() {
		return Collections.unmodifiableMap(handlers);
	}
}
