package org.throwable.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/29 12:59
 */
public final class ArrayUtils {

	public static <E> List<E> getDuplicatedElements(List<E> list) {
		return list.stream()                           // list 对应的 Stream
				.collect(Collectors.toMap(e -> e, e -> 1, (a, b) -> a + b)) // 获得元素出现频率的 Map，键为元素，值为元素出现的次数
				.entrySet().stream()                   // 所有 entry 对应的 Stream
				.filter(entry -> entry.getValue() > 1) // 过滤出元素出现次数大于 1 的 entry
				.map(Map.Entry::getKey)          // 获得 entry 的键（重复元素）对应的 Stream
				.collect(Collectors.toList());         // 转化为 List
	}
}
