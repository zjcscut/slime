package org.throwable.mapper.support.plugins.sort;


import org.throwable.exception.UnsupportedElementException;

import java.util.Locale;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/7 12:33
 */
public enum Direction {

	ASC, DESC;

	public static Direction fromString(String value) {
		try {
			return Direction.valueOf(value.toUpperCase(Locale.US));
		} catch (Exception e) {
			throw new UnsupportedElementException(String.format(
					"Invalid value '%s' for orders given! Has to be either 'desc' or 'asc' (case insensitive).", value), e);
		}
	}

}
