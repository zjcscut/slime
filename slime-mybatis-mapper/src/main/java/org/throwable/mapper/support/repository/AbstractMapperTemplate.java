package org.throwable.mapper.support.repository;

import com.google.common.collect.Lists;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;
import org.throwable.exception.BeanRegisterHandleException;
import org.throwable.mapper.common.entity.EntityColumn;
import org.throwable.mapper.common.entity.EntityTable;
import org.throwable.mapper.support.assist.EntityTableAssisor;
import org.throwable.mapper.support.assist.MapperTemplateAssistor;
import org.throwable.mapper.support.assist.SqlAppendAssistor;
import org.throwable.mapper.support.plugins.generator.identity.MultipleJdbc3KeyGenerator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static jodd.util.StringUtil.isBlank;
import static org.apache.ibatis.executor.keygen.SelectKeyGenerator.SELECT_KEY_SUFFIX;
import static org.apache.ibatis.mapping.SqlCommandType.SELECT;
import static org.throwable.mapper.common.constant.CommonConstants.GENERATED_JDBC;

/**
 * @author throwable
 * @version v1.0
 * @description 抽象mapper模板
 * @since 2017/4/2 16:39
 */
@Slf4j
public abstract class AbstractMapperTemplate {

	private static final XMLLanguageDriver languageDriver = new XMLLanguageDriver();
	private Map<String, Method> methodMap = new HashMap<>();
	private Map<String, Class<?>> entityClassMap = new HashMap<>();
	private Class<?> mapperClass;
	private MapperTemplateAssistor mapperTemplateAssistor;

	public AbstractMapperTemplate(Class<?> mapperClass, MapperTemplateAssistor mapperTemplateAssistor) {
		this.mapperClass = mapperClass;
		this.mapperTemplateAssistor = mapperTemplateAssistor;
	}

	public static Class<?> getMapperClass(String msId) {
		if (!msId.contains(".")) {
			throw new UnsupportedOperationException(String.format("当前MappedStatement的id=%s,不符合MappedStatement的规则!", msId));
		}
		String mapperClassStr = msId.substring(0, msId.lastIndexOf("."));
		try {
			return Class.forName(mapperClassStr);
		} catch (ClassNotFoundException e) {
			throw new UnsupportedOperationException(String.format("实例化mapper类失败,className:%s", mapperClassStr));
		}
	}

	public static String getMethodName(MappedStatement ms) {
		return getMethodName(ms.getId());
	}

	public static String getMethodName(String msId) {
		return msId.substring(msId.lastIndexOf(".") + 1);
	}

	public String dynamicSQL(Object record) {
		return "dynamicSQL";
	}

	public void addMethodMap(String methodName, Method method) {
		methodMap.put(methodName, method);
	}

	public String getOgnlIdentityStrategy() {
		return mapperTemplateAssistor.getConfig().getOgnlIdentityStrategy();
	}

	public String getIdentityDialect() {
		return mapperTemplateAssistor.getConfig().getIdentityDialect();
	}

	public boolean isSelectKeyExecuteBefore() {
		return mapperTemplateAssistor.getConfig().isSelectKeyExecuteBefore();
	}

	public boolean supportMethod(String msId) {
		Class<?> mapperClass = getMapperClass(msId);
		if (mapperClass != null && this.mapperClass.isAssignableFrom(mapperClass)) {
			String methodName = getMethodName(msId);
			return methodMap.get(methodName) != null;
		}
		return false;
	}

	/**
	 * 重写ms的sqlSource
	 */
	protected void rewriteSqlSource(MappedStatement ms, SqlSource sqlSource) {
		MetaObject msObject = SystemMetaObject.forObject(ms);
		msObject.setValue("sqlSource", sqlSource);
		//如果是Jdbc3KeyGenerator，就设置为MultipleJdbc3KeyGenerator
		KeyGenerator keyGenerator = ms.getKeyGenerator();
		if (keyGenerator instanceof Jdbc3KeyGenerator) {
			msObject.setValue("keyGenerator", new MultipleJdbc3KeyGenerator());
		}
	}

	/**
	 * 检查缓存
	 */
	private void checkCache(MappedStatement ms) throws Exception {
		if (ms.getCache() == null) {
			String nameSpace = ms.getId().substring(0, ms.getId().lastIndexOf("."));
			Cache cache;
			try {
				//不存在的时候会抛出异常,详情看ScriptMap里面,如果null会抛出异常
				cache = ms.getConfiguration().getCache(nameSpace);
			} catch (IllegalArgumentException e) {
				//ignore
				return;
			}
			if (cache != null) {
				MetaObject metaObject = SystemMetaObject.forObject(ms);
				metaObject.setValue("cache", cache);
			}
		}
	}

	public void rewriteSqlSource(MappedStatement ms) throws Exception {
		if (this.mapperClass == getMapperClass(ms.getId())) {
			throw new UnsupportedOperationException(String.format("请不要配置或扫描Smart-Mapper接口类:%s", this.mapperClass));
		}
		Method method = methodMap.get(getMethodName(ms));
		try {
			//第一种，直接操作ms，不需要返回值
			if (method.getReturnType() == Void.TYPE) {
				method.invoke(this, ms);
			}
			//第二种，返回SqlNode
			else if (SqlNode.class.isAssignableFrom(method.getReturnType())) {
				SqlNode sqlNode = (SqlNode) method.invoke(this, ms);
				DynamicSqlSource dynamicSqlSource = new DynamicSqlSource(ms.getConfiguration(), sqlNode);
				rewriteSqlSource(ms, dynamicSqlSource);
			}
			//第三种，返回xml形式的sql字符串
			else if (String.class.equals(method.getReturnType())) {
				String xmlSql = (String) method.invoke(this, ms);
				SqlSource sqlSource = createXmlSqlSource(ms, xmlSql);
				//替换原有的SqlSource
				rewriteSqlSource(ms, sqlSource);
			} else {
				throw new BeanRegisterHandleException("自定义Mapper方法返回类型错误,可选的返回类型为void,SqlNode,String三种!");
			}
			//cache
			checkCache(ms);
		} catch (IllegalAccessException e) {
			throw new BeanRegisterHandleException(e);
		} catch (InvocationTargetException e) {
			throw new BeanRegisterHandleException(e.getTargetException() != null ? e.getTargetException() : e);
		}
	}

	/**
	 * 通过xmlSql创建sqlSource
	 */
	public SqlSource createXmlSqlSource(MappedStatement ms, String xmlSql) {
		return languageDriver.createSqlSource(ms.getConfiguration(), "<script>\n\t" + xmlSql + "</script>", null);
	}


	/**
	 * 获取序列下个值的表达式
	 */
	protected String getSeqNextVal(EntityColumn column) {
		return MessageFormat.format(mapperTemplateAssistor.getConfig().getSeqFormat(), column.getSequenceName(),
				column.getColumn(), column.getProperty(), column.getTable().getName());
	}

	/**
	 * 获取实体类的表名
	 */
	protected String tableName(Class<?> entityClass) {
		EntityTable entityTable = EntityTableAssisor.getEntityTable(entityClass);
		String prefix = entityTable.getPrefix();
		if (StringUtil.isEmpty(prefix)) {
			//使用全局配置
			prefix = mapperTemplateAssistor.getConfig().getPrefix();
		}
		if (StringUtil.isNotEmpty(prefix)) {
			return prefix + "." + entityTable.getName();
		}
		return entityTable.getName();
	}

	/**
	 * 新建写回主键策略
	 */
	protected void newSelectKeyMappedStatement(MappedStatement ms, EntityColumn column) {
		Configuration config = ms.getConfiguration();
		String keyId = ms.getId() + SELECT_KEY_SUFFIX;
		if (config.hasKeyGenerator(keyId)) {
			return;
		}
		Class<?> entityClass = getEntityClass(ms);
		KeyGenerator keyGenerator;
		String generator = isBlank(column.getGenerator()) ? getIdentityDialect() : column.getGenerator();
		if (GENERATED_JDBC.equalsIgnoreCase(generator)) {
			keyGenerator = new MultipleJdbc3KeyGenerator();
		} else {   //新建一个selectKey标签
			//新建SqlSource
			SqlSource sqlSource = new DynamicSqlSource(config, getSelectKeySql(column, generator, getOgnlIdentityStrategy()));
			//新建MappedStatement
			MappedStatement.Builder statementBuilder = new MappedStatement.Builder(config, keyId, sqlSource, SELECT);
			statementBuilder.resource(ms.getResource());
			statementBuilder.keyGenerator(new NoKeyGenerator());
			statementBuilder.keyProperty(column.getProperty());
			statementBuilder.keyColumn(null);
			statementBuilder.fetchSize(null);
			statementBuilder.databaseId(null);
			statementBuilder.lang(config.getDefaultScriptingLanguageInstance());
			statementBuilder.resultOrdered(false);
			statementBuilder.resultSets(null);
			statementBuilder.timeout(config.getDefaultStatementTimeout());
			//新建ParameterMap
			ParameterMap inlineParameterMap = new ParameterMap.Builder(config, keyId + "-Inline", entityClass,
					newArrayList()).build();
			statementBuilder.parameterMap(inlineParameterMap);
			//新建ResultMap
			ResultMap inlineResultMap = new ResultMap.Builder(config, keyId + "-Inline", column.getJavaType(),
					newArrayList(), null).build();
			statementBuilder.resultMaps(Lists.newArrayList(inlineResultMap));
			statementBuilder.resultSetType(null);
			statementBuilder.flushCacheRequired(false);
			statementBuilder.useCache(false);
			statementBuilder.cache(null);

			try {
				//configuration添加MappedStatement
				config.addMappedStatement(statementBuilder.build());
			} catch (Exception e) {
				//ignore
				log.error("addMappedStatement error", e);
			}
			//新建keyGenerator
			keyGenerator = new SelectKeyGenerator(config.getMappedStatement(keyId, false), isSelectKeyExecuteBefore());
			try {
				config.addKeyGenerator(keyId, keyGenerator);
			} catch (Exception e) {
				//ignore
				log.error("addKeyGenerator error", e);
			}
		}

		//MappedStatement中设置keyGenerator
		setKeyGenerator(ms, column, keyGenerator);
	}

	/**
	 * 设置resultMap
	 */
	protected void setResultType(MappedStatement ms, Class<?> entityClass) {
		ResultMap resultMap = _getResultMap(ms.getConfiguration(), entityClass);
		List<ResultMap> resultMaps = Collections.unmodifiableList(newArrayList(resultMap));
		SystemMetaObject.forObject(ms).setValue("resultMaps", resultMaps);
	}

	private ResultMap _getResultMap(Configuration config, Class<?> entityClass) {
		EntityTable entityTable = EntityTableAssisor.getEntityTable(entityClass);
		ResultMap resultMap = entityTable.getResultMap(config);
		if (resultMap == null) {
			return null;
		}
		List<ResultMapping> resultMappings = resultMap.getResultMappings().stream()
				.filter(resultMapping -> resultMapping.getColumn() != null)
				.map(resultMapping -> {
					String column = resultMapping.getColumn().trim();
					// 去除首尾的分隔符，如果有的话
					column = SqlAppendAssistor.removeDelimiter(column);
					ResultMapping.Builder builder = new ResultMapping.Builder(config, resultMapping.getProperty(),
							column, resultMapping.getJavaType());
					builder.jdbcType(resultMapping.getJdbcType());
					builder.typeHandler(resultMapping.getTypeHandler());
					builder.flags(resultMapping.getFlags());
					return builder.build();
				})
				.collect(toList());
		return new ResultMap.Builder(config, "BaseMapperResultMap", entityClass, resultMappings, true).build();
	}

	public Class<?> getEntityClass(MappedStatement ms) {
		String msId = ms.getId();
		if (entityClassMap.containsKey(msId)) {
			return entityClassMap.get(msId);
		} else {
			return Stream.of(getMapperClass(msId).getGenericInterfaces())
					.filter(type -> type instanceof ParameterizedType)
					.map(type -> (ParameterizedType) type)
					.filter(t -> t.getRawType() == this.mapperClass || this.mapperClass.isAssignableFrom((Class<?>) t.getRawType()))
					.findFirst()
					.map(t -> {
						Class<?> returnType = (Class<?>) t.getActualTypeArguments()[0];
						//获取该类型后，第一次对该类型进行初始化
						EntityTableAssisor.initEntityTableMap(returnType, mapperTemplateAssistor.getConfig());
						entityClassMap.put(msId, returnType);
						return returnType;
					})
					.orElseThrow(() -> new UnsupportedOperationException(String.format("无法获取SmartMapper<T>泛型类型:%s", msId)));
		}
	}

	private void setKeyGenerator(MappedStatement ms, EntityColumn column, KeyGenerator keyGenerator) {
		try {
			MetaObject msObject = SystemMetaObject.forObject(ms);
			msObject.setValue("keyGenerator", keyGenerator);
			msObject.setValue("keyProperties", column.getTable().getKeyProperties());
			msObject.setValue("keyColumns", column.getTable().getKeyColumns());
		} catch (Exception e) {
			//ignore
			log.error("set keyGenerator error", e);
		}
	}

	/**
	 * 根据msId获取接口类名称
	 */
	public static String getMapperName(String msId) {
		if (!msId.contains(".")) {
			throw new UnsupportedOperationException(String.format("当前MappedStatement的id=%s,不符合MappedStatement的规则!)", msId));
		}
		return msId.substring(0, msId.lastIndexOf("."));
	}

	/**
	 * 用于insert生成主键的动态SQL，将放入SelectKey标签,区分一般的数据库主键方言和OGNL生成的UUID
	 *
	 * @param generator 获取主键的SQL
	 */
	public static SqlNode getSelectKeySql(EntityColumn column, String generator, String ognlIdentityStrategy) {
		SqlNode ifSqlNode;
		SqlNode defaultSqlNode;
		if (column.isUUID()) {
			ifSqlNode = new IfSqlNode(new TextSqlNode("SELECT '${" + ognlIdentityStrategy + "}'"), ognlIdentityStrategy.concat(" neq null"));
			defaultSqlNode = new TextSqlNode("SELECT uuid()");
		} else {
			ifSqlNode = new IfSqlNode(new TextSqlNode(generator), column.getProperty().concat(" neq null"));
			defaultSqlNode = new TextSqlNode("SELECT " + SqlAppendAssistor.getColumnHolder(column));
		}
		return new ChooseSqlNode(newArrayList(ifSqlNode), defaultSqlNode);
	}
}
