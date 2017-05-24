package org.throwable.mapper;

import org.apache.ibatis.annotations.InsertProvider;
import org.throwable.mapper.support.provider.InsertMapperProvider;

import java.util.List;


/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/3 23:57
 */
public interface InsertMapper<T> {

	@InsertProvider(type = InsertMapperProvider.class, method = "dynamicSQL")
	int insert(T t);

	@InsertProvider(type = InsertMapperProvider.class, method = "dynamicSQL")
	int insertNoneSkipPrimaryKey(T t);

	@InsertProvider(type = InsertMapperProvider.class, method = "dynamicSQL")
	int insertIngore(T t);

}
