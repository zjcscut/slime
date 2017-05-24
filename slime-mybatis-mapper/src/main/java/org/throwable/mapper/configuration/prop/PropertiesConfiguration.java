package org.throwable.mapper.configuration.prop;

import jodd.util.StringUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.throwable.mapper.SmartMapper;
import org.throwable.mapper.common.constant.IdentityDialectEnum;
import org.throwable.mapper.common.constant.NameStyleEnum;

import static org.throwable.mapper.common.constant.IdentityDialectEnum.MYSQL;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/4 0:01
 */

@NoArgsConstructor
@Data
public class PropertiesConfiguration {

	//默认数据库主键回写方言 - Mysql
	private static final IdentityDialectEnum DEFAULT_IDENTITY_DIALECT = MYSQL;

	//默认主键回写策略提交order  - 对应selectKey标签的order里面的before和end
	private static final boolean DEFAULT_SELECTKEY_EXECUTE_BEFORE = true;

	//默认命名转换规范 - normal
	private static final NameStyleEnum DEFAULT_NAMESTYLE_ENUM = NameStyleEnum.NORMAL;

	//默认注册SmartMapper
	private static final Class<?>[] DEFAULT_REGISGER_MAPPERS = new Class[]{SmartMapper.class};

	//OGNL主键策略 - 使用外部方法生成主键
	private static final String DEFAULT_OGNLIDENTITY_STRATEGY = "@java.util.UUID@randomUUID().toString().replace(\"-\", \"\")";

	//是否允许使用方法上的注解
	private static final boolean DEFAULT_ENABLE_METHODANNOTATION = false;

	//注册的mapper
	private Class<?>[] registerMappers = DEFAULT_REGISGER_MAPPERS;

	//OGNL主键策略
	private String ognlIdentityStrategy = DEFAULT_OGNLIDENTITY_STRATEGY;

	//数据库回写主键方言
	private String identityDialect = DEFAULT_IDENTITY_DIALECT.getIdentityRetrievalStatement();

	//selectKey顺序策略
	private boolean selectKeyExecuteBefore = DEFAULT_SELECTKEY_EXECUTE_BEFORE;

	//是否允许方法是的注解
	private boolean enableMethodAnnotation = DEFAULT_ENABLE_METHODANNOTATION;

	//命名规范
	private NameStyleEnum style = DEFAULT_NAMESTYLE_ENUM;

	//序列格式
	private String seqFormat;

	//catalog
	private String catalog;

	//schema
	private String schema;

	//拼接查询前缀
	public String getPrefix() {
		if (StringUtil.isNotEmpty(this.catalog)) {
			return this.catalog;
		}
		if (StringUtil.isNotEmpty(this.schema)) {
			return this.schema;
		}
		return "";
	}


}
