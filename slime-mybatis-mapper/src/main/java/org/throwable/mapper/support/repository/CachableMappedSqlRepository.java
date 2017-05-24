package org.throwable.mapper.support.repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/15 13:35
 *
 */
@Slf4j
public abstract class CachableMappedSqlRepository {

    protected static Cache<String, String> sqlCache = CacheBuilder
            .newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .removalListener((RemovalListener<String, String>) removalNotification -> {
                log.debug(String.format("remove key:%s,value:%s from sqlCache,cause:%s", removalNotification.getKey(),
                        removalNotification.getValue(), removalNotification.getCause()));
            }).build();

}
