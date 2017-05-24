package org.throwable.mapper;

import lombok.NonNull;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.UpdateProvider;
import org.throwable.mapper.support.plugins.condition.Condition;
import org.throwable.mapper.support.provider.UpdateMapperProvider;

import java.util.List;

import static org.throwable.mapper.common.constant.CommonConstants.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/3 23:58
 */
public interface UpdateMapper<T> {

	default int updateByPrimaryKey(T t) {
		return updateByPrimaryKey(t, false);
	}

	@UpdateProvider(type = UpdateMapperProvider.class, method = "dynamicSQL")
	int updateByPrimaryKey(@NonNull @Param(PARAM_RECORD) T t,
                           @Param(PARAM_ALLOW_UPDATE_TO_NULL) boolean allowUpdateToNull);


}
