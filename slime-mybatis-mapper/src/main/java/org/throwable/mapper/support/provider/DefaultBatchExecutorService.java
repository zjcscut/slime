package org.throwable.mapper.support.provider;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.throwable.mapper.BatchExecutorService;
import org.throwable.mapper.common.entity.EntityColumn;
import org.throwable.mapper.configuration.prop.MapperProperties;
import org.throwable.mapper.support.handler.DefaultMappedStatementHander;
import org.throwable.mapper.support.plugins.condition.Condition;
import org.throwable.mapper.support.plugins.generator.identity.MultipleJdbc3KeyGenerator;

import java.util.List;
import java.util.Map;

import static org.throwable.mapper.common.constant.CommonConstants.*;
import static org.throwable.mapper.support.repository.AbstractMappedSqlRepository.*;


/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/10 2:30
 */
@Service(value = "batchExecutorService")
@EnableConfigurationProperties(MapperProperties.class)
@Slf4j
public class DefaultBatchExecutorService extends BatchExecutor implements BatchExecutorService {

	@Autowired
	private DefaultMappedStatementHander defaultMappedStatementHander;

	@Autowired
	private MapperProperties mapperProperties;

	@Override
	public <T> int executeBatchUpdate(List<T> list, int batchSize, boolean skipNull) {
		assertListParams(list, batchSize);
		int size = list.size();
		Class<?> clazz = list.iterator().next().getClass();
		//懒初始化classTable
		DefaultMappedClassProcessor processor = new DefaultMappedClassProcessor(clazz,
				mapperProperties.getPropertiesConfiguration(), DYNAMIC_BATCHUPDATE);
		String msId = processor.getMsId();
		SqlSession sqlSession = defaultMappedStatementHander.getSqlSessionFactory().openSession(ExecutorType.BATCH, NONE_AUTO_COMMIT);
		return batchOperation(list, batchSize, msId, processor.getParamsMap(), new BatchProcessor() {

			@Override
			public <T> void beforeExecuteBatch(List<T> list, Map<String, Object> paramsMap) {
				log.debug(String.format("begin executeBatchUpdate,target:List<%s>,executeSize:%d,batchSize:%d",
						clazz.getCanonicalName(), size, batchSize));
				//檢查主鍵是否都存在
				EntityColumn keyColumn = checkExistsPriamryKeyAndReturn(clazz);
				//懒注册MappedStatement
				if (!defaultMappedStatementHander.hasRegisterMappedStatement(msId)) {
					defaultMappedStatementHander.addScriptSqlMappedStatement(
							createDynamicBatchUpdateScriptSql(clazz, msId, skipNull),
							msId,
							SqlCommandType.UPDATE,
							msId.concat(PARAMMAP_DEFAULT),
							Map.class,
							msId.concat(RESULTMAP_DEFAULT),
							Integer.class,
							msId,
							keyColumn.getProperty(),
							keyColumn.getColumn(),
							new NoKeyGenerator());
				}
				paramsMap.put(PARAM_RECORDS, list);
			}

			@Override
			public int executeBatchOperation(String msId, Map<String, Object> paramsMap) {
				int updateCount = sqlSession.update(msId, paramsMap);
				sqlSession.commit();
				sqlSession.clearCache();
				return updateCount;
			}

			@Override
			public void afterExecuteBatch() {
				sqlSession.close();
			}
		});
	}

	@Override
	public <T> int executeBatchInsert(List<T> list, int batchSize) {
		assertListParams(list, batchSize);
		int size = list.size();
		Class<?> clazz = list.iterator().next().getClass();
		//懒初始化classTable
		DefaultMappedClassProcessor processor = new DefaultMappedClassProcessor(clazz,
				mapperProperties.getPropertiesConfiguration(), DYNAMIC_BATCHINSERT);
		String msId = processor.getMsId();
		SqlSession sqlSession = defaultMappedStatementHander.getSqlSessionFactory().openSession(ExecutorType.BATCH, NONE_AUTO_COMMIT);
		return batchOperation(list, batchSize, msId, processor.getParamsMap(), new BatchProcessor() {

			@Override
			public <T> void beforeExecuteBatch(List<T> list, Map<String, Object> paramsMap) {
				log.debug(String.format("begin executeBatchInsert,target:List<%s>,executeSize:%d,batchSize:%d",
						clazz.getCanonicalName(), size, batchSize));
				//檢查主鍵是否都存在
				EntityColumn keyColumn = checkExistsPriamryKeyAndReturn(clazz);
				//如果主键为自增长,设置批量主键自增长主键生成器,否则批量反射调用外部配置的Ognl主键策略写入主键
				KeyGenerator keyGenerator;
				if (keyColumn.isAutoIncrease()) {  //批量自增长
					keyGenerator = new MultipleJdbc3KeyGenerator();
				} else if (keyColumn.isUUID()) {  //非自增长并且是UUID,需要批量写入id,使用配置的OGNL主键策略
					String ognlStrategy = mapperProperties.getPropertiesConfiguration().getOgnlIdentityStrategy();
					autoCreatePrimaryKeyByOgnlStrategy(list, keyColumn, ognlStrategy);
					keyGenerator = new NoKeyGenerator();
				} else {
					keyGenerator = new NoKeyGenerator();  //默认不进行主键回写
				}
				//懒注册MappedStatement
				if (!defaultMappedStatementHander.hasRegisterMappedStatement(msId)) {
					defaultMappedStatementHander.addScriptSqlMappedStatement(
							createDynamicBatchInsertScriptSql(clazz, msId, keyColumn.isUUID()),
							msId,
							SqlCommandType.INSERT,
							msId.concat(PARAMMAP_DEFAULT),
							Map.class,
							msId.concat(RESULTMAP_DEFAULT),
							Integer.class,
							msId,
							keyColumn.getProperty(),
							keyColumn.getColumn(),
							keyGenerator);
				}
				paramsMap.put(PARAM_RECORDS, list);
			}

			@Override
			public int executeBatchOperation(String msId, Map<String, Object> paramsMap) {
				int insertCount = sqlSession.insert(msId, paramsMap);
				sqlSession.commit();
				sqlSession.clearCache();
				return insertCount;
			}

			@Override
			public void afterExecuteBatch() {
				sqlSession.close();
			}
		});
	}

	@Override
	public <T> int update(T t, boolean skipNull) {
		Assert.notNull(t, "update target must not be null");
		Class<?> clazz = t.getClass();
		DefaultMappedClassProcessor processor = new DefaultMappedClassProcessor(clazz,
				mapperProperties.getPropertiesConfiguration(), DYNAMIC_UPDATE);
		String msId = processor.getMsId();
		SqlSession sqlSession = defaultMappedStatementHander.getSqlSessionFactory().openSession(ExecutorType.SIMPLE, NONE_AUTO_COMMIT);
		return singleOperation(t, msId, processor.getParamsMap(), new SingleProcessor() {

			@Override
			public <T> void beforeExecuteSingle(T t, Map<String, Object> paramsMap) {
				log.debug(String.format("begin update target:<%s>", clazz.getCanonicalName()));
				EntityColumn keyColumn = checkExistsPriamryKeyAndReturn(clazz);
				//懒注册MappedStatement
				if (!defaultMappedStatementHander.hasRegisterMappedStatement(msId)) {
					defaultMappedStatementHander.addScriptSqlMappedStatement(
							createDynamicUpdateScriptSql(clazz, msId, skipNull),
							msId,
							SqlCommandType.UPDATE,
							msId.concat(PARAMMAP_DEFAULT),
							Map.class,
							msId.concat(RESULTMAP_DEFAULT),
							Integer.class,
							msId,
							keyColumn.getProperty(),
							keyColumn.getColumn(),
							new NoKeyGenerator());
				}
				paramsMap.put(PARAM_RECORD, t);
			}

			@Override
			public int executeSingleOperation(String msId, Map<String, Object> paramsMap) {
				int updateCount = sqlSession.update(msId, paramsMap);
				sqlSession.commit();
				sqlSession.clearCache();
				return updateCount;
			}

			@Override
			public void afterSingleExecute() {
				sqlSession.close();
			}
		});
	}

	@Override
	public int updateByCondition(@NonNull Condition condition, boolean skipNull) {
		Class<?> clazz = condition.getEntity();
		DefaultMappedClassProcessor processor = new DefaultMappedClassProcessor(clazz,
				mapperProperties.getPropertiesConfiguration(), DYNAMIC_UPDATE_CONDITION);
		String msId = processor.getMsId().concat("$" + condition.hashCode());
		SqlSession sqlSession = defaultMappedStatementHander.getSqlSessionFactory().openSession(ExecutorType.SIMPLE, NONE_AUTO_COMMIT);
		return singleOperation(null, msId, processor.getParamsMap(), new SingleProcessor() {

			@Override
			public <T> void beforeExecuteSingle(T t, Map<String, Object> paramsMap) {
				log.debug(String.format("begin updateByCondition target:<%s>", clazz.getCanonicalName()));
				EntityColumn keyColumn = checkExistsPriamryKeyAndReturn(clazz);
				//懒注册MappedStatement
				if (!defaultMappedStatementHander.hasRegisterMappedStatement(msId)) {
					defaultMappedStatementHander.addScriptSqlMappedStatement(
							createDynamicUpdateByConditionScriptSql(clazz, msId, skipNull, condition),
							msId,
							SqlCommandType.UPDATE,
							msId.concat(PARAMMAP_DEFAULT),
							Map.class,
							msId.concat(RESULTMAP_DEFAULT),
							Integer.class,
							msId,
							keyColumn.getProperty(),
							keyColumn.getColumn(),
							new NoKeyGenerator());
				}
				paramsMap.put(PARAM_RECORD, condition.getUpdateFieldMap());
				paramsMap.put(PARAM_CONDITION, condition);
			}

			@Override
			public int executeSingleOperation(String msId, Map<String, Object> paramsMap) {
				int updateCount = sqlSession.update(msId, paramsMap);
				sqlSession.commit();
				sqlSession.clearCache();
				return updateCount;
			}

			@Override
			public void afterSingleExecute() {
				sqlSession.close();
			}
		});
	}

	@Override
	public <T> T selectOneByCondition(@NonNull Condition condition) {
		Class<?> clazz = condition.getEntity();
		DefaultMappedClassProcessor processor = new DefaultMappedClassProcessor(clazz,
				mapperProperties.getPropertiesConfiguration(), DYNAMIC_SELECTONE_CONDITION);
		String msId = processor.getMsId().concat("$" + condition.hashCode());
		SqlSession sqlSession = defaultMappedStatementHander.getSqlSessionFactory().openSession(ExecutorType.SIMPLE, AUTO_COMMIT);
		return selectOneOperation(msId, processor.getParamsMap(), new SelectOneProcessor() {
			@Override
			public void beforeExecuteSingle(Map<String, Object> paramsMap) {
				log.debug(String.format("begin selectOneByCondition target:<%s>", clazz.getCanonicalName()));

				//懒注册MappedStatement
				if (!defaultMappedStatementHander.hasRegisterMappedStatement(msId)) {
					defaultMappedStatementHander.addScriptSqlMappedStatement(
							createDynamicSelectOneByConditionScriptSql(clazz, msId, condition),
							msId,
							SqlCommandType.SELECT,
							msId.concat(PARAMMAP_DEFAULT),
							Map.class,
							msId.concat(RESULTMAP_DEFAULT),
							clazz,
							msId,
							null,
							null,
							new NoKeyGenerator());
				}
				paramsMap.put(PARAM_CONDITION, condition);
			}

			@Override
			public <T> T executeSingleOperation(String msId, Map<String, Object> paramsMap) {
				return sqlSession.selectOne(msId, paramsMap);
			}

			@Override
			public void afterSingleExecute() {
				sqlSession.close();
			}
		});
	}

	@Override
	public <T> List<T> selectListByCondition(@NonNull Condition condition) {
		Class<?> clazz = condition.getEntity();
		DefaultMappedClassProcessor processor = new DefaultMappedClassProcessor(clazz,
				mapperProperties.getPropertiesConfiguration(), DYNAMIC_SELECTLIST_CONDITION);
		String msId = processor.getMsId().concat("$" + condition.hashCode());
		SqlSession sqlSession = defaultMappedStatementHander.getSqlSessionFactory().openSession(ExecutorType.SIMPLE, AUTO_COMMIT);
		return selectListOperation(msId, processor.getParamsMap(), new SelectListProcessor() {
			@Override
			public void beforeExecute(Map<String, Object> paramsMap) {
				log.debug(String.format("begin selectListByCondtion target:<%s>", clazz.getCanonicalName()));
				//懒注册MappedStatement
				if (!defaultMappedStatementHander.hasRegisterMappedStatement(msId)) {
					defaultMappedStatementHander.addScriptSqlMappedStatement(
							createDynamicSelectOneByConditionScriptSql(clazz, msId, condition),
							msId,
							SqlCommandType.SELECT,
							msId.concat(PARAMMAP_DEFAULT),
							Map.class,
							msId.concat(RESULTMAP_DEFAULT),
							clazz,
							msId,
							null,
							null,
							new NoKeyGenerator());
				}
				paramsMap.put(PARAM_CONDITION, condition);
			}

			@Override
			public <T> List<T> executeOperation(String msId, Map<String, Object> paramsMap) {
				return sqlSession.selectList(msId, paramsMap);
			}

			@Override
			public void afterExecute() {
				sqlSession.close();
			}
		});
	}

	@Override
	public long countByCondition(@NonNull Condition condition) {
		Class<?> clazz = condition.getEntity();
		DefaultMappedClassProcessor processor = new DefaultMappedClassProcessor(clazz,
				mapperProperties.getPropertiesConfiguration(), DYNAMIC_COUNT_CONDITION);
		String msId = processor.getMsId().concat("$" + condition.hashCode());
		SqlSession sqlSession = defaultMappedStatementHander.getSqlSessionFactory().openSession(ExecutorType.SIMPLE, AUTO_COMMIT);
		return countOperation(msId, processor.getParamsMap(), new CountProcessor() {
			@Override
			public void beforeExecute(Map<String, Object> paramsMap) {
				log.debug(String.format("begin countByCondition target:<%s>", clazz.getCanonicalName()));
				//懒注册MappedStatement
				if (!defaultMappedStatementHander.hasRegisterMappedStatement(msId)) {
					defaultMappedStatementHander.addScriptSqlMappedStatement(
							createDynamicCountByConditionScriptSql(msId),
							msId,
							SqlCommandType.SELECT,
							msId.concat(PARAMMAP_DEFAULT),
							Map.class,
							msId.concat(RESULTMAP_DEFAULT),
							long.class,
							msId,
							null,
							null,
							new NoKeyGenerator());
				}
				paramsMap.put(PARAM_CONDITION, condition);
			}

			@Override
			public long executeOperation(String msId, Map<String, Object> paramsMap) {
				return sqlSession.selectOne(msId, paramsMap);
			}

			@Override
			public void afterExecute() {
				sqlSession.close();
			}
		});
	}
}
