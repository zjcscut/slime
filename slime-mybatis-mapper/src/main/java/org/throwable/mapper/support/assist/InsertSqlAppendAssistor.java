package org.throwable.mapper.support.assist;

import org.throwable.mapper.common.entity.EntityColumn;
import org.throwable.mapper.exception.UnsupportedElementException;
import org.throwable.mapper.support.filter.FieldFilter;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.throwable.mapper.common.constant.CommonConstants.PARAM_DEFAULT;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/4 2:02
 */
public abstract class InsertSqlAppendAssistor extends SqlAppendAssistor {


    public static String insertDynamicTable() {
        return "INSERT INTO ${dynamicTableName} ";
    }

    /**
     * insert into tableName - 动态表名
     *
     * @param entityClass      实体类
     * @param defaultTableName 默认表名
     */
    public static String insertIntoTable(Class<?> entityClass, String defaultTableName) {
        return "INSERT INTO " + getDynamicTableName(entityClass, defaultTableName) + " ";
    }

    /**
     * insert into tableName
     *
     * @param tableName 表名
     */
    public static String insertBatchIntoTable(String tableName) {
        return "INSERT INTO " + tableName + " ";
    }

    /**
     * insert ignore into tableName - 动态表名
     *
     * @param entityClass      实体类
     * @param defaultTableName 默认表名
     */
    public static String insertIgnoreIntoTable(Class<?> entityClass, String defaultTableName) {
        return "INSERT IGNORE INTO " + getDynamicTableName(entityClass, defaultTableName) + " ";
    }

    /**
     * insert ignore into tableName
     *
     * @param tableName 表名
     */
    public static String insertBatchIgnoreIntoTable(String tableName) {
        return "INSERT IGNORE INTO " + tableName + " ";
    }

    /**
     * insert指定列
     *
     * @param entityClass 实体类
     */
    public static String insertColumns(Class<?> entityClass, FieldFilter fieldFilter, boolean skipPrimaryKey) {
        Set<EntityColumn> columnList = getFilterColumns(entityClass, fieldFilter, skipPrimaryKey);
        StringBuilder sql = new StringBuilder();
        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        for (EntityColumn column : columnList) {
            if (!column.isInsertable()) {
                continue;
            }
            //当该列在数据库中明确定义为not null，则加上!=null的判断
            if (isNotNull(column)) {
                sql.append(getIfNotNull(column, column.getColumn() + ","));
            } else {
                sql.append(column.getColumn()).append(",");
            }
        }
        sql.append("</trim>");
        return sql.toString();
    }

    public static String insertValues(Class<?> entityClass, FieldFilter fieldFilter, boolean skipPrimaryKey) {
        Set<EntityColumn> columnList = getFilterColumns(entityClass, fieldFilter, skipPrimaryKey);
        StringBuilder sql = new StringBuilder();
        sql.append("<trim prefix=\"VALUES(\" suffix=\")\" suffixOverrides=\",\">");
        for (EntityColumn column : columnList) {
            if (!column.isInsertable()) {
                continue;
            }
            //当该列在数据库中明确定义为not null，必须加上!=null的判断
            String content = getColumnHolderWithComma(column);
            if (isNotNull(column)) {
                sql.append(getIfNotNull(column, content));
            } else {
                sql.append(content);
            }
        }
        sql.append("</trim>");
        return sql.toString();
    }

    public static String insertBatchColumns(Class<?> entityClass, boolean skipPrimaryKey) {
        Set<EntityColumn> columnList = skipPrimaryKey ? EntityTableAssisor.getNonePrimaryColumns(entityClass) :
                EntityTableAssisor.getAllColumns(entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append("\n<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
        columnList.forEach(column -> sql.append(column.getColumn()).append(","));
        sql.append("\n</trim>\n");
        return sql.toString();
    }

    public static String insertBatchValues(Class<?> entityClass, boolean skipPrimaryKey) {
        Set<EntityColumn> columnList = skipPrimaryKey ? EntityTableAssisor.getNonePrimaryColumns(entityClass) :
                EntityTableAssisor.getAllColumns(entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append(" VALUES ");
        sql.append("\n<foreach collection=\"records\" item=\"record\" separator=\",\" >\n");
        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
        columnList.forEach(column -> sql.append(column.getColumnHolder("record")).append(","));
        sql.append("\n</trim>\n");
        sql.append("</foreach>\n");
        return sql.toString();
    }

}
