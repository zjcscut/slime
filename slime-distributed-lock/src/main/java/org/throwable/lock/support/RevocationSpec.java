package org.throwable.lock.support;

import java.util.concurrent.Executor;

/**
 * @author throwable
 * @version v1.0
 * @description copy from {@link org.apache.curator.framework.recipes.locks.RevocationSpec}
 * @since 2017/5/17 22:35
 */
public class RevocationSpec {

	private final Runnable runnable;
	private final Executor executor;

	RevocationSpec(Executor executor, Runnable runnable) {
		this.runnable = runnable;
		this.executor = executor;
	}

	Runnable getRunnable() {
		return runnable;
	}

	Executor getExecutor() {
		return executor;
	}
}
