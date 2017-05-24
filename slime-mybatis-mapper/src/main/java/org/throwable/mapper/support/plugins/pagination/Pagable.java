package org.throwable.mapper.support.plugins.pagination;

import org.throwable.mapper.utils.PageUtils;

/**
 * @author throwable
 * @version v1.0
 * @description 分页接口
 * @since 2017/4/7 3:27
 */
public interface Pagable {

	// 默認頁碼
	int DEFAULT_PAGE_NUMBER = 1;

	// 默认每页显示数量
	int DEFAULT_PAGE_SIZE = 20;

	boolean USED_DEFAULT_PAGE_NUMBER = true;

	boolean fixEdge();

	boolean isPagable();

	int getPageNumber();

	int getPageSize();

	default int getOffset() {
		return PageUtils.getOffset(PageUtils.getFirstPageNumber(), getPageNumber(), getPageSize());
	}

	default int getLastPage(long total){
		return PageUtils.getLastPageNumber(total,getPageSize());
	}
}
