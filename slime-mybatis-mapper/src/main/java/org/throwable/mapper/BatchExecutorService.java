package org.throwable.mapper;


import org.throwable.mapper.support.plugins.condition.Condition;
import org.throwable.mapper.support.plugins.pagination.PageModel;
import org.throwable.mapper.support.plugins.pagination.Pager;

import javax.validation.constraints.Min;
import java.util.List;

import static org.throwable.mapper.support.provider.BatchExecutor.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/10 2:29
 */

public interface BatchExecutorService {

	default <T> int executeBatchInsert(List<T> list) {
		return executeBatchInsert(list, DEFAULT_BATCH_SIZE);
	}

	<T> int executeBatchInsert(List<T> list, int batchSize);

	default <T> int executeBatchUpdate(List<T> list, boolean skipNull) {
		return executeBatchUpdate(list, DEFAULT_BATCH_SIZE, skipNull);
	}

	default <T> int executeBatchUpdate(List<T> list) {
		return executeBatchUpdate(list, DEFAULT_BATCH_SIZE, NONE_SKIP_NULL);
	}

	<T> int executeBatchUpdate(List<T> list, int batchSize, boolean skipNull);

	default <T> int update(T t) {
		return update(t, NONE_SKIP_NULL);
	}

	<T> int update(T t, boolean skipNull);

	default int updateByCondition(Condition condition) {
		return updateByCondition(condition, NONE_SKIP_NULL);
	}

	int updateByCondition(Condition condition, boolean skipNull);

	<T> T selectOneByCondition(Condition condition);

	<T> List<T> selectListByCondition(Condition condition);

	long countByCondition(Condition condition);

	default <T> List<T> selectByConditionLimit(Condition condition, @Min(1) int limit) {
		condition.limit(0, limit);
		return selectListByCondition(condition);
	}

	default <T> PageModel<T> selectListByConditionPage(Condition condition, Pager pager) {
		long total = countByCondition(condition);
		int lastPage = pager.getLastPage(total);
		int offset = pager.getOffset();
		if (offset > lastPage) {
			pager.setPageNumber(offset);
			condition.limit(lastPage, pager.getPageSize());
		} else {
			condition.limit(pager.getOffset(), pager.getPageSize());
		}
		List<T> list = selectListByCondition(condition);
		return new PageModel<>(pager.getPageNumber(), pager.getPageSize(), total, list);
	}
}
