package org.throwable.mapper.utils;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/8 13:06
 */
public final class ArraysUtils {

	public static <T> List<T> arrayToList(T[] array) {
		List<T> list = Lists.newArrayList();
		Collections.addAll(list, array);
		return list;
	}
}
