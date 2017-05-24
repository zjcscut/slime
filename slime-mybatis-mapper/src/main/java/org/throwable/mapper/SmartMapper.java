package org.throwable.mapper;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/3 23:57
 */
public interface SmartMapper<T> extends Mapper, InsertMapper<T>, UpdateMapper<T>, SelectMapper<T>, DeleteMapper<T> {
}
