package org.throwable.mapper;

import org.apache.ibatis.annotations.DeleteProvider;
import org.throwable.mapper.support.provider.DeleteMapperProvider;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/3 23:59
 */
public interface DeleteMapper<T> {

	@DeleteProvider(type = DeleteMapperProvider.class, method = "dynamicSQL")
	int deleteByPrimaryKey(Object key);

}
