package org.throwable.mapper.support.provider;

import lombok.val;
import org.apache.ibatis.mapping.MappedStatement;
import org.throwable.mapper.support.assist.MapperTemplateAssistor;
import org.throwable.mapper.support.repository.AbstractMapperTemplate;

import static org.throwable.mapper.support.assist.InsertSqlAppendAssistor.*;
import static org.throwable.mapper.support.assist.SqlAppendAssistor.checkDefaultParamValue;
import static org.throwable.mapper.support.assist.SqlAppendAssistor.getIdentityColumn;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/4 0:02
 */
public class InsertMapperProvider extends AbstractMapperTemplate {

    public InsertMapperProvider(Class<?> mapperClass, MapperTemplateAssistor mapperTemplateAssistor) {
        super(mapperClass, mapperTemplateAssistor);
    }

    public String insert(MappedStatement ms) {
        val entityClass = getEntityClass(ms);
        //主键回写
        getIdentityColumn(entityClass).ifPresent(column -> newSelectKeyMappedStatement(ms, column));
        //拼接动态SQL
        val builder = new StringBuilder(checkDefaultParamValue());
        builder.append(insertIntoTable(entityClass, tableName(entityClass)));
        builder.append(insertColumns(entityClass, null, true, false));
        builder.append(insertValues(entityClass, null, true, false));
        return builder.toString();
    }


    public String insertNoneSkipPrimaryKey(MappedStatement ms) {
        val entityClass = getEntityClass(ms);
        //主键回写
        getIdentityColumn(entityClass).ifPresent(column -> newSelectKeyMappedStatement(ms, column));
        //拼接动态SQL
        val builder = new StringBuilder(checkDefaultParamValue());
        builder.append(insertIntoTable(entityClass, tableName(entityClass)));
        builder.append(insertColumns(entityClass, null, false, false));
        builder.append(insertValues(entityClass, null, false, false));
        return builder.toString();
    }

    public String insertIngore(MappedStatement ms) {
        val entityClass = getEntityClass(ms);
        //主键回写
        getIdentityColumn(entityClass).ifPresent(column -> newSelectKeyMappedStatement(ms, column));
        //拼接动态SQL
        val builder = new StringBuilder(checkDefaultParamValue());
        builder.append(insertIgnoreIntoTable(entityClass, tableName(entityClass)));
        builder.append(insertColumns(entityClass, null, false, false));
        builder.append(insertValues(entityClass, null, false, false));
        return builder.toString();
    }

    public String insertSkipNull(MappedStatement ms) {
        val entityClass = getEntityClass(ms);
        //主键回写
        getIdentityColumn(entityClass).ifPresent(column -> newSelectKeyMappedStatement(ms, column));
        //拼接动态SQL
        val builder = new StringBuilder(checkDefaultParamValue());
        builder.append(insertIntoTable(entityClass, tableName(entityClass)));
        builder.append(insertColumns(entityClass, null, true, true));
        builder.append(insertValues(entityClass, null, true, true));
        return builder.toString();
    }

    public String insertNoneSkipPrimaryKeyAndSkipNull(MappedStatement ms) {
        val entityClass = getEntityClass(ms);
        //主键回写
        getIdentityColumn(entityClass).ifPresent(column -> newSelectKeyMappedStatement(ms, column));
        //拼接动态SQL
        val builder = new StringBuilder(checkDefaultParamValue());
        builder.append(insertIntoTable(entityClass, tableName(entityClass)));
        builder.append(insertColumns(entityClass, null, false, true));
        builder.append(insertValues(entityClass, null, false, true));
        return builder.toString();
    }

}
