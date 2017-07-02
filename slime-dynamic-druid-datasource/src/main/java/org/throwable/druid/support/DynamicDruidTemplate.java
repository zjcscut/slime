package org.throwable.druid.support;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/2 0:56
 */
public class DynamicDruidTemplate {

	public <T> T execute(String signature, DynamicDruidCallback<T> callback) {
		try {
			DataSourceHolder.bindResource(signature);
			return callback.doInDruid();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			DataSourceHolder.removeResource();
		}
	}
}
