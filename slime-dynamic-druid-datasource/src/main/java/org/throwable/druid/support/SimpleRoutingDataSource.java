package org.throwable.druid.support;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/1 16:22
 */
//必须添加@Primary,否则因为多接口实现无法注入,这个bean的实例作为主数据源
@Primary
public class SimpleRoutingDataSource extends AbstractRoutingDataSource {

	@Override
	protected Object determineCurrentLookupKey() {
		return DataSourceHolder.getResource();
	}
}
