package org.throwable.druid.support;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/1 16:22
 */
public class SimpleRoutingDataSource extends AbstractRoutingDataSource {

	@Override
	protected Object determineCurrentLookupKey() {
		return DataSourceHolder.getResource();
	}
}
