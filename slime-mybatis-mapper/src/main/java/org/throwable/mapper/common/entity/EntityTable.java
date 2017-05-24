package org.throwable.mapper.common.entity;

import jodd.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.throwable.mapper.common.constant.NameStyleEnum;
import org.throwable.mapper.support.repository.NameStyleContext;

import javax.persistence.Table;
import java.util.*;

/**
 * @author throwable
 * @version v1.0
 * @description 实体表-对应一个表的所有列
 * @since 2017/3/30 12:48
 */

@Getter
@Setter
public class EntityTable {

	private String name;
	private String catalog;
	private String schema;
	private String orderByClause;  //默认排序字符串
	private String baseSelect;  //基础查询字符串
	//实体类 => 全部列属性
	private Set<EntityColumn> entityClassColumns;
	//实体类 => 主键信息
	private Set<EntityColumn> entityClassPKColumns;
	//useGenerator包含多列的时候需要用到
	private List<String> keyProperties;
	private List<String> keyColumns;
	//resultMap对象
	private ResultMap resultMap;
	//属性和列对应
	protected Map<String, EntityColumn> propertyMap;
	//类
	private Class<?> entityClass;
	//命名规范
	private NameStyleEnum nameStyle;

	public EntityTable(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public void setTable(Table table) {
		this.name = table.name();
		this.catalog = table.catalog();
		this.schema = table.schema();
	}

	public String getPrefix() {
		if (StringUtil.isNotEmpty(catalog)) {
			return catalog;
		} else if (StringUtil.isNotEmpty(schema)) {
			return schema;
		} else {
			return "";
		}
	}

	public String[] getKeyProperties() {
		if (keyProperties != null && keyProperties.size() > 0) {
			return keyProperties.toArray(new String[keyProperties.size()]);
		}
		return new String[]{};
	}

	public void setKeyProperties(String keyProperty) {
		if (this.keyProperties == null) {
			this.keyProperties = new ArrayList<>();
			this.keyProperties.add(keyProperty);
		} else {
			this.keyProperties.add(keyProperty);
		}
	}

	public String[] getKeyColumns() {
		if (keyColumns != null && keyColumns.size() > 0) {
			return keyColumns.toArray(new String[keyColumns.size()]);
		}
		return new String[]{};
	}

	public void setKeyColumns(String keyColumn) {
		if (this.keyColumns == null) {
			this.keyColumns = new ArrayList<>();
			this.keyColumns.add(keyColumn);
		} else {
			this.keyColumns.add(keyColumn);
		}
	}

	/**
	 * 生成当前实体的resultMap对象
	 *
	 */
	public ResultMap getResultMap(Configuration configuration) {
		if (this.resultMap != null) {
			return this.resultMap;
		}
		if (entityClassColumns == null || entityClassColumns.size() == 0) {
			return null;
		}
		List<ResultMapping> resultMappings = new ArrayList<>();
		for (EntityColumn entityColumn : entityClassColumns) {
			ResultMapping.Builder builder = new ResultMapping.Builder(configuration, entityColumn.getProperty(), entityColumn.getColumn(), entityColumn.getJavaType());
			if (entityColumn.getJdbcType() != null) {
				builder.jdbcType(entityColumn.getJdbcType());
			}
			if (entityColumn.getTypeHandler() != null) {
				try {
					builder.typeHandler(entityColumn.getTypeHandler().newInstance());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			List<ResultFlag> flags = new ArrayList<>();
			if (entityColumn.isIdentity()) {
				flags.add(ResultFlag.ID);
			}
			builder.flags(flags);
			resultMappings.add(builder.build());
		}
		ResultMap.Builder builder = new ResultMap.Builder(configuration, "BaseMapperResultMap", this.entityClass, resultMappings, true);
		this.resultMap = builder.build();
		return this.resultMap;
	}

	public void initPropertyMap() {
		propertyMap = new HashMap<>(getEntityClassColumns().size());
		for (EntityColumn column : getEntityClassColumns()) {
			propertyMap.put(column.getProperty(), column);
		}
	}

	public Map<String, EntityColumn> getPropertyMap() {
		return propertyMap;
	}

	public String columnNameStyleConvert(String key) {
		return NameStyleContext.convert(nameStyle, key);
	}


}
