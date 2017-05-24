package org.throwable.mapper.support.provider;

/**
 * @author throwable
 * @version v1.0
 * @description 动态批量sql执行标识接口
 * @since 2017/4/13 22:58
 */
public interface Executors {

	boolean NONE_AUTO_COMMIT = false;

	boolean AUTO_COMMIT = true;

	boolean NONE_SKIP_NULL = false;

	int DEFAULT_BATCH_SIZE = 20;

	String DYNAMIC_BATCHUPDATE = "dynamicBatchUpdate";

	String DYNAMIC_BATCHINSERT = "dynamicBatchInsert";

	String DYNAMIC_UPDATE = "dynamicUpdate";

	String DYNAMIC_UPDATE_CONDITION = "dynamicUpdateByCondition";

	String DYNAMIC_SELECTONE_CONDITION = "dynamicSelectOneByCondition";

	String DYNAMIC_SELECTLIST_CONDITION = "dynamicSelectListByCondition";

	String DYNAMIC_COUNT_CONDITION = "dynamicCountByCondition";

	String DYNAMIC_SELECTPAGE_CONDITION = "dynamicSelectByConditionPage";
}
