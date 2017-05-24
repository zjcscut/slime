package org.throwable.mapper.support.plugins.pagination;

import lombok.Setter;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/8 16:10
 */
public class Pager implements Pagable {

	@Setter
	private int pageNumber;
	@Setter
	private int pageSize;

	public Pager(int pageNumber, int pageSize) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		if (this.pageNumber <= 0 && fixEdge()) {
			this.pageNumber = DEFAULT_PAGE_NUMBER;
		}
		if (this.pageSize <= 0 && fixEdge()) {
			this.pageSize = DEFAULT_PAGE_SIZE;
		}
	}

	@Override
	public boolean isPagable() {
		return true;
	}

	@Override
	public boolean fixEdge() {
		return true;
	}

	@Override
	public int getPageNumber() {
		if (this.pageNumber <= 0 && fixEdge()) {
			this.pageNumber = DEFAULT_PAGE_NUMBER;
		}
		return pageNumber;
	}

	@Override
	public int getPageSize() {
		if (this.pageSize <= 0 && fixEdge()) {
			this.pageSize = DEFAULT_PAGE_SIZE;
		}
		return pageSize;
	}
}
