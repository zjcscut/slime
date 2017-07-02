package org.throwable.druid.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;
import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/1 16:23
 */
@Slf4j
public class DataSourceHolder extends ResourceHolderSupport {

	private static final ThreadLocal<Object> datasourName = new NamedThreadLocal<>("DataSource name");

	public static Object getResource(){
		return datasourName.get();
	}

	public static void bindResource(Object name){
		datasourName.set(name);
	}

	public static void removeResource(){
		datasourName.remove();
	}

}
