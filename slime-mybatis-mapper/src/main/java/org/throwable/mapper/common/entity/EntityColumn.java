package org.throwable.mapper.common.entity;

import jodd.util.StringUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.throwable.mapper.common.constant.CommonConstants;

/**
 * @author throwable
 * @version v1.0
 * @since 2017/3/29 0:47
 * @description 实体行-对应数据库的一个列
 * @copyfrom common-mapper
 */

@Getter
@Setter
@NoArgsConstructor
public class EntityColumn {

	private EntityTable table;
	private String property;
	private String column;
	private Class<?> javaType;
	private JdbcType jdbcType;
	private Class<? extends TypeHandler<?>> typeHandler;
	private String sequenceName;
	private boolean UUID = false;
	private boolean autoIncrease = false;
	private boolean identity = false;
	private String generator;
	private String orderBy;
	private boolean insertable = true;
	private boolean updatable = true;

	public EntityColumn(EntityTable table) {
		this.table = table;
	}

	/**
	 * 返回格式如:colum = #{age,jdbcType=NUMERIC,typeHandler=MyTypeHandler}
	 *
	 * @return
	 */
	public String getColumnEqualsHolder() {
		return getColumnEqualsHolder(null);
	}

	/**
	 * 返回格式如:colum = #{age,jdbcType=NUMERIC,typeHandler=MyTypeHandler}
	 *
	 * @param entityName
	 * @return
	 */
	public String getColumnEqualsHolder(String entityName) {
		return this.column + " = " + getColumnHolder(entityName);
	}

	/**
	 * 返回格式如:#{age,jdbcType=NUMERIC,typeHandler=MyTypeHandler}
	 *
	 * @return
	 */
	public String getColumnHolder() {
		return getColumnHolder(null);
	}

	/**
	 * 返回格式如:#{entityName.age,jdbcType=NUMERIC,typeHandler=MyTypeHandler}
	 *
	 * @param entityName
	 * @return
	 */
	public String getColumnHolder(String entityName) {
		return getColumnHolder(entityName, null);
	}

	/**
	 * 返回格式如:#{entityName.age+suffix,jdbcType=NUMERIC,typeHandler=MyTypeHandler}
	 *
	 * @param entityName
	 * @param suffix
	 * @return
	 */
	public String getColumnHolder(String entityName, String suffix) {
		return getColumnHolder(entityName, null, null);
	}

	/**
	 * 返回格式如:#{entityName.age+suffix,jdbcType=NUMERIC,typeHandler=MyTypeHandler},
	 *
	 * @param entityName
	 * @param suffix
	 * @return
	 */
	public String getColumnHolderWithComma(String entityName, String suffix) {
		return getColumnHolder(entityName, suffix, CommonConstants.COMMA);
	}

	/**
	 * 返回格式如:#{entityName.age+suffix,jdbcType=NUMERIC,typeHandler=MyTypeHandler}+separator
	 *
	 * @param entityName
	 * @param suffix
	 * @param separator
	 * @return
	 */
	public String getColumnHolder(String entityName, String suffix, String separator) {
		StringBuilder sb = new StringBuilder("#{");
		if (StringUtil.isNotEmpty(entityName)) {
			sb.append(entityName);
			sb.append(".");
		}
		sb.append(this.property);
		if (StringUtil.isNotEmpty(suffix)) {
			sb.append(suffix);
		}
		if (this.jdbcType != null) {
			sb.append(",jdbcType=");
			sb.append(this.jdbcType.toString());
		} else if (this.typeHandler != null) {
			sb.append(",typeHandler=");
			sb.append(this.typeHandler.getCanonicalName());
		} else if (!this.javaType.isArray()) {
			sb.append(",javaType=");
			sb.append(javaType.getCanonicalName());
		}
		sb.append("}");
		if (StringUtil.isNotEmpty(separator)) {
			sb.append(separator);
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EntityColumn that = (EntityColumn) o;
		if (UUID != that.UUID) return false;
		if (identity != that.identity) return false;
		if (table != null ? !table.equals(that.table) : that.table != null) return false;
		if (property != null ? !property.equals(that.property) : that.property != null) return false;
		if (column != null ? !column.equals(that.column) : that.column != null) return false;
		if (javaType != null ? !javaType.equals(that.javaType) : that.javaType != null) return false;
		if (jdbcType != that.jdbcType) return false;
		if (typeHandler != null ? !typeHandler.equals(that.typeHandler) : that.typeHandler != null) return false;
		if (sequenceName != null ? !sequenceName.equals(that.sequenceName) : that.sequenceName != null) return false;
		if (generator != null ? !generator.equals(that.generator) : that.generator != null) return false;
        return true;
	}

	@Override
	public int hashCode() {
		int result = table != null ? table.hashCode() : 0;
		result = 31 * result + (property != null ? property.hashCode() : 0);
		result = 31 * result + (column != null ? column.hashCode() : 0);
		result = 31 * result + (javaType != null ? javaType.hashCode() : 0);
		result = 31 * result + (jdbcType != null ? jdbcType.hashCode() : 0);
		result = 31 * result + (typeHandler != null ? typeHandler.hashCode() : 0);
		result = 31 * result + (sequenceName != null ? sequenceName.hashCode() : 0);
		result = 31 * result + (UUID ? 1 : 0);
		result = 31 * result + (identity ? 1 : 0);
		result = 31 * result + (generator != null ? generator.hashCode() : 0);
		return result;
	}


}
