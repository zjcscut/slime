package org.throwable.mapper.support.provider;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.apache.ibatis.ognl.Ognl;
import org.apache.ibatis.ognl.OgnlException;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.util.Assert;
import org.throwable.mapper.common.entity.EntityColumn;
import org.throwable.mapper.configuration.prop.PropertiesConfiguration;
import org.throwable.mapper.support.assist.EntityTableAssisor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.throwable.mapper.common.constant.CommonConstants.*;


/**
 * @author throwable
 * @version v1.0
 * @function
 * @since 2017/4/10 10:57
 */
public abstract class BatchExecutor extends SingleExecutor implements Executors {

	protected void assertListParams(List list, int batchSize) {
		assertNotEmpty(list, "executeBatch list must not be empey!");
		assertBatchSize(batchSize, "batchSize must be greater than 0!");
	}

	protected void assertNotEmpty(List list, String message) {
		Assert.notEmpty(list, message);
	}

	protected void assertBatchSize(int batchSize, String message) {
		if (batchSize <= 0) {
			throw new IllegalArgumentException(message);
		}
	}

	interface MappedClassProcessor {

		void initMappedClassTable();

		void initMappedParamsMap();

		void createMappedStatementId();
	}

	interface BatchProcessor {

		<T> void beforeExecuteBatch(List<T> list, Map<String, Object> paramsMap);

		int executeBatchOperation(String msId, Map<String, Object> paramsMap);

		void afterExecuteBatch();
	}

	interface SelectListProcessor {

		void beforeExecute(Map<String, Object> paramsMap);

		<T> List<T> executeOperation(String msId, Map<String, Object> paramsMap);

		void afterExecute();
	}

	protected <T> List<T> selectListOperation(String msId, Map<String, Object> paramsMap, SelectListProcessor selectListProcessor) {
		selectListProcessor.beforeExecute(paramsMap);
		List<T> list = selectListProcessor.executeOperation(msId, paramsMap);
		selectListProcessor.afterExecute();
		return list;
	}

	protected Map<String, Object> buildParamsMap(String tableName) {
		Map<String, Object> params = Maps.newHashMap();
		params.put(DYNAMICT_TABLENAME, tableName);
		return params;
	}

	protected void checkExistsPriamryKey(Class<?> clazz) {
		Assert.notNull(EntityTableAssisor.getPrimaryColumn(clazz), "Primary key column must be existed");
	}

	protected EntityColumn checkExistsPriamryKeyAndReturn(Class<?> clazz) {
		EntityColumn primaryKeyColumn = EntityTableAssisor.getPrimaryColumn(clazz);
		Assert.notNull(primaryKeyColumn, "Primary key column must be existed");
		return primaryKeyColumn;
	}


	protected <T> int batchOperation(List<T> list, int batchSize, String msId, Map<String, Object> paramsMap, BatchProcessor batchProcessor) {
		batchProcessor.beforeExecuteBatch(list, paramsMap);
		int commitCount = (int) Math.ceil(list.size() / (double) batchSize);
		List<T> tempList = new ArrayList<>(batchSize);
		int start, stop;
		int batchCount = 0;
		for (int batchIndex = 0; batchIndex < commitCount; batchIndex++) {
			tempList.clear();
			start = batchIndex * batchSize;
			stop = Math.min(batchIndex * batchSize + batchSize - 1, list.size() - 1);
			for (int executeIndex = start; executeIndex <= stop; executeIndex++) {
				tempList.add(list.get(executeIndex));
				paramsMap.put(PARAM_RECORDS, tempList);
			}
			batchCount += batchProcessor.executeBatchOperation(msId, paramsMap);
		}
		batchProcessor.afterExecuteBatch();
		return batchCount;
	}

	protected <T> void autoCreatePrimaryKeyByOgnlStrategy(List<T> list, EntityColumn keyColumn, String strategy) {
		list.forEach(column -> {
			MetaObject target = SystemMetaObject.forObject(column);
			try {
				target.setValue(keyColumn.getProperty(), Ognl.getValue(strategy, null));
			} catch (OgnlException e) {
				throw new UnsupportedOperationException(e);
			}
		});
	}

	protected class DefaultMappedClassProcessor implements MappedClassProcessor {
		@Getter
		private Class<?> clazz;
		@Getter
		private String tableName;
		@Getter
		private PropertiesConfiguration configuration;
		@Getter
		private String msId;
		@Getter
		private String msIdPrefix;
		@Getter
		private Map<String, Object> paramsMap;

		public DefaultMappedClassProcessor(Class<?> clazz, PropertiesConfiguration configuration, String msIdPrefix) {
			this.clazz = clazz;
			this.configuration = configuration;
			this.msIdPrefix = msIdPrefix;
			init();
		}

		public void init() {
			initMappedClassTable();
			initMappedParamsMap();
			createMappedStatementId();
		}

		@Override
		public void initMappedClassTable() {
			EntityTableAssisor.initEntityTableMap(this.clazz, this.configuration);
			this.tableName = EntityTableAssisor.getEntityTable(this.clazz).getName();
		}

		@Override
		public void initMappedParamsMap() {
			this.paramsMap = buildParamsMap(this.tableName);
		}

		@Override
		public void createMappedStatementId() {
			this.msId = this.msIdPrefix.concat(UNDER_LINE).concat(this.tableName);
		}
	}


}
