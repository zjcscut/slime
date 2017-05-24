package org.throwable.mapper.support.assist;

import jodd.util.StringUtil;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.UnknownTypeHandler;
import org.springframework.util.Assert;
import org.throwable.exception.BeanReflectionException;
import org.throwable.mapper.common.annotation.ColumnExtend;
import org.throwable.mapper.common.annotation.NameStyle;
import org.throwable.mapper.common.constant.IdentityDialectEnum;
import org.throwable.mapper.common.constant.NameStyleEnum;
import org.throwable.mapper.common.entity.EntityColumn;
import org.throwable.mapper.common.entity.EntityField;
import org.throwable.mapper.common.entity.EntityTable;
import org.throwable.mapper.configuration.prop.PropertiesConfiguration;
import org.throwable.mapper.support.repository.EntityInfoRepository;
import org.throwable.mapper.support.repository.NameStyleContext;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.throwable.mapper.common.constant.CommonConstants.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/2 12:43
 */
public class EntityTableAssisor extends EntityInfoRepository {

	public static EntityTable getEntityTable(Class<?> entityClass) {
		EntityTable entityTable = entityTableMap.get(entityClass);
		Assert.notNull(entityTable, String.format("无法获取实体->表映射,实体名:%s", entityClass.getCanonicalName()));
		return entityTable;
	}

	public static EntityTable getCondtionEntityTable(Class<?> entityClass) {
		return entityTableMap.get(entityClass);
	}

	public static String getDefaultOrderByClause(Class<?> entityClass) {
		EntityTable table = getEntityTable(entityClass);
		if (null != table.getOrderByClause()) {
			return table.getOrderByClause();
		}
		StringBuilder orderBy = new StringBuilder();
		for (EntityColumn column : table.getEntityClassColumns()) {
			if (null != column.getOrderBy()) {
				if (orderBy.length() != 0) {
					orderBy.append(",");
				}
				orderBy.append(column.getColumn()).append(" ").append(column.getOrderBy());
			}
		}
		table.setOrderByClause(orderBy.toString());
		return table.getOrderByClause();
	}

	public static Set<EntityColumn> getAllColumns(Class<?> entityClass) {
		return getEntityTable(entityClass).getEntityClassColumns();
	}

	public static Set<EntityColumn> getNonePrimaryColumns(Class<?> entityClass) {
		return getAllColumns(entityClass).stream()
				.filter(a -> !a.isIdentity() && a.isInsertable())
				.collect(Collectors.toSet());
	}

	public static Set<EntityColumn> getPrimaryColumns(Class<?> entityClass) {
		return getEntityTable(entityClass).getEntityClassPKColumns();
	}

	public static EntityColumn getPrimaryColumn(Class<?> entityClass) {
		Set<EntityColumn> columns = getPrimaryColumns(entityClass);
		if (columns.size() == 1) {
			return columns.iterator().next();
		} else {
			throw new UnsupportedOperationException(String.format("暂不支持联合主键或多个主键,实体类名:%s", entityClass.getCanonicalName()));
		}
	}

	public static String getDefaultSelectClause(Class<?> entityClass) {
		EntityTable entityTable = getEntityTable(entityClass);
		if (null != entityTable.getBaseSelect()) {
			return entityTable.getBaseSelect();
		}
		Set<EntityColumn> columnList = getAllColumns(entityClass);
		StringBuilder selectBuilder = new StringBuilder();
		boolean skipAlias = Map.class.isAssignableFrom(entityClass);
		for (EntityColumn entityColumn : columnList) {
			selectBuilder.append(entityColumn.getColumn());
			if (!skipAlias && !entityColumn.getColumn().equalsIgnoreCase(entityColumn.getProperty())) {
				//不等的时候分几种情况，例如`DESC`
				if (entityColumn.getColumn().substring(1, entityColumn.getColumn().length() - 1).equalsIgnoreCase(entityColumn.getProperty())) {
					selectBuilder.append(",");
				} else {
					selectBuilder.append(" AS ").append(entityColumn.getProperty()).append(",");
				}
			} else {
				selectBuilder.append(",");
			}
		}
		entityTable.setBaseSelect(selectBuilder.substring(0, selectBuilder.length() - 1));
		return entityTable.getBaseSelect();
	}


	//使用緩存配置進行初始化
	public static synchronized void initEntityTableMap(Class<?> entityClass){
		initEntityTableMap(entityClass,getConfiguration());
	}

	public static synchronized void initEntityTableMap(Class<?> entityClass, PropertiesConfiguration configuration) {
		if (null != entityTableMap.get(entityClass)) {
			return;
		}
		setConfiguration(configuration); //緩存配置
		NameStyleEnum style = configuration.getStyle();
		//style，该注解优先于全局配置
		if (entityClass.isAnnotationPresent(NameStyle.class)) {
			NameStyle nameStyle = entityClass.getAnnotation(NameStyle.class);
			style = nameStyle.value();
		}

		//创建并缓存EntityTable
		EntityTable entityTable = null;
		if (entityClass.isAnnotationPresent(Table.class)) {
			Table table = entityClass.getAnnotation(Table.class);
			if (!table.name().equals("")) {
				entityTable = new EntityTable(entityClass);
				entityTable.setTable(table);
			}
		}
		if (null == entityTable) {
			entityTable = new EntityTable(entityClass);
			//可以通过stye控制
			entityTable.setName(NameStyleContext.convert(style, entityClass.getSimpleName()));
		}
		entityTable.setNameStyle(style);
		entityTable.setEntityClassColumns(new LinkedHashSet<>());
		entityTable.setEntityClassPKColumns(new LinkedHashSet<>());
		//处理所有列
		List<EntityField> fields;
		if (configuration.isEnableMethodAnnotation()) {
			fields = EntityFieldAssistor.getEntityFieldsProperties(entityClass);
		} else {
			fields = EntityFieldAssistor.getEntityFields(entityClass);
		}
		for (EntityField field : fields) {
			processOneFieldColumn(entityTable, style, field);
		}
		//当pk.size=0的时候使用所有列作为主键
		if (entityTable.getEntityClassPKColumns().size() == 0) {
			entityTable.setEntityClassPKColumns(entityTable.getEntityClassColumns());
		}
		entityTable.initPropertyMap();
		entityTableMap.put(entityClass, entityTable);
	}


	private static void processOneFieldColumn(EntityTable entityTable, NameStyleEnum style, EntityField field) {
		//exclude Transient field
		if (field.isAnnotationPresent(Transient.class)) {
			return;
		}
		//Id
		EntityColumn entityColumn = new EntityColumn(entityTable);
		if (field.isAnnotationPresent(Id.class)) {
			entityColumn.setIdentity(true);
		}
		//Column
		String columnName = null;
		if (field.isAnnotationPresent(Column.class)) {
			Column column = field.getAnnotation(Column.class);
			columnName = column.name();
			entityColumn.setUpdatable(column.updatable());
			entityColumn.setInsertable(column.insertable());
		}
		//ColumnType
		if (field.isAnnotationPresent(ColumnExtend.class)) {
			ColumnExtend columnType = field.getAnnotation(ColumnExtend.class);
			//Column --> alias
			if (StringUtil.isEmpty(columnName) && StringUtil.isNotEmpty(columnType.column())) {
				columnName = columnType.column();
			}
			if (columnType.jdbcType() != JdbcType.UNDEFINED) {
				entityColumn.setJdbcType(columnType.jdbcType());
			}
			if (columnType.typeHandler() != UnknownTypeHandler.class) {
				entityColumn.setTypeHandler(columnType.typeHandler());
			}
		}
		//ColumnName
		if (StringUtil.isEmpty(columnName)) {
			columnName = NameStyleContext.convert(style, field.getName());
		}
		entityColumn.setProperty(field.getName());
		entityColumn.setColumn(columnName);
		entityColumn.setJavaType(field.getJavaType());
		//OrderBy
		if (field.isAnnotationPresent(OrderBy.class)) {
			OrderBy orderBy = field.getAnnotation(OrderBy.class);
			if (orderBy.value().equals("")) {
				entityColumn.setOrderBy("ASC");
			} else {
				entityColumn.setOrderBy(orderBy.value());
			}
		}
		if (field.isAnnotationPresent(SequenceGenerator.class)) {
			SequenceGenerator sequenceGenerator = field.getAnnotation(SequenceGenerator.class);
			if (sequenceGenerator.sequenceName().equals("")) {
				throw new BeanReflectionException(entityTable.getEntityClass() + "字段" + field.getName() + "的注解@SequenceGenerator未指定sequenceName!");
			}
			entityColumn.setSequenceName(sequenceGenerator.sequenceName());
		} else if (field.isAnnotationPresent(GeneratedValue.class)) {
			GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
			if (generatedValue.generator().equalsIgnoreCase(GENERATED_UUID)) {
				entityColumn.setUUID(true);
			} else if (generatedValue.generator().equalsIgnoreCase(GENERATED_JDBC)) {
				entityColumn.setIdentity(true);
				entityColumn.setAutoIncrease(true);
				entityColumn.setGenerator("JDBC");
				entityTable.setKeyProperties(entityColumn.getProperty());
				entityTable.setKeyColumns(entityColumn.getColumn());
			} else {
				//config sql to fetch database last id,such as: mysql=CALL IDENTITY(),hsqldb=SELECT SCOPE_IDENTITY()
				//允许通过拦截器参数设置公共的generator
				if (generatedValue.strategy() == GenerationType.IDENTITY && StringUtil.isNotBlank(generatedValue.generator())) {
					//mysql的自动增长
					entityColumn.setIdentity(true);
					entityColumn.setAutoIncrease(true);
					String generator;
					IdentityDialectEnum identityDialect = IdentityDialectEnum.getDatabaseIdentityDialect(generatedValue.generator());
					if (identityDialect != null) {
						generator = identityDialect.getIdentityRetrievalStatement();
					} else {
						generator = generatedValue.generator();
					}
					entityColumn.setGenerator(generator);
				} else {
					throw new BeanReflectionException(field.getName()
							+ " - 该字段@GeneratedValue配置只允许以下几种形式:" +
							"\n1.全部数据库通用的@GeneratedValue(generator=\"UUID\")" +
							"\n2.useGeneratedKeys的@GeneratedValue(generator=\\\"JDBC\\\")  " +
							"\n3.类似mysql数据库的@GeneratedValue(strategy=GenerationType.IDENTITY,generator=\"Mysql\")");
				}
			}
		}
		entityTable.getEntityClassColumns().add(entityColumn);
		if (entityColumn.isIdentity()) {
			entityTable.getEntityClassPKColumns().add(entityColumn);
		}
	}
}
