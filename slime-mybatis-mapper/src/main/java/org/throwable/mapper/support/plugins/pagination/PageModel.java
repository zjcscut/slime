package org.throwable.mapper.support.plugins.pagination;

import lombok.Getter;

import java.util.List;

/**
 * @author throwable
 * @version v1.0
 * @description 分页model
 * @since 2017/4/8 15:28
 */
@Getter
public class PageModel<E>{

	private long pageNumber;
	private long pageSize;
	private long total;
	private List<E> result;

	public PageModel(long pageNumber, long pageSize, long total, List<E> result) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		this.total = total;
		this.result = result;
	}
}
