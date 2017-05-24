package org.throwable.mapper;

import lombok.NonNull;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.throwable.mapper.support.plugins.condition.Condition;
import org.throwable.mapper.support.plugins.pagination.PageModel;
import org.throwable.mapper.support.plugins.pagination.Pager;
import org.throwable.mapper.support.provider.SelectMapperProvider;

import javax.validation.constraints.Min;
import java.util.List;

import static org.throwable.mapper.common.constant.CommonConstants.PARAM_CONDITION;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/3 23:59
 */
public interface SelectMapper<T> {

	default List<T> selectByConditionLimit(Condition condition, @Min(1) int limit) {
		condition.limit(0, limit);
		return selectByCondition(condition);
	}

	default PageModel<T> selectByConditionPage(Condition condition, int pageNumber, int pageSize) {
		return selectByConditionPage(condition, new Pager(pageNumber, pageSize));
	}

	default PageModel<T> selectByConditionPage(Condition condition, Pager pager) {
		long count = countByCondition(condition);
		int lastPage = pager.getLastPage(count);
		int offset = pager.getOffset();
		if (offset > lastPage) {
			pager.setPageNumber(offset);
			condition.limit(lastPage, pager.getPageSize());
		} else {
			condition.limit(pager.getOffset(), pager.getPageSize());
		}
		List<T> list = selectByCondition(condition);
		return new PageModel<>(pager.getPageNumber(), pager.getPageSize(), count, list);
	}

	@SelectProvider(type = SelectMapperProvider.class, method = "dynamicSQL")
	long countByCondition(@NonNull @Param(PARAM_CONDITION) Condition condition);

	@SelectProvider(type = SelectMapperProvider.class, method = "dynamicSQL")
	List<T> selectByCondition(@NonNull @Param(PARAM_CONDITION) Condition condition);

}
