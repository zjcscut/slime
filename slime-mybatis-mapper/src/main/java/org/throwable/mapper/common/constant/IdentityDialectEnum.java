package org.throwable.mapper.common.constant;

import java.util.Locale;

/**
 * @author throwable
 * @version v1.0
 * @description 数据库方言 - 获取最新的主键方言
 * @since 2017/4/2 12:16
 */
public enum IdentityDialectEnum {

	DB2("VALUES IDENTITY_VAL_LOCAL()"),
	MYSQL("SELECT LAST_INSERT_ID()"),
	SQLSERVER("SELECT SCOPE_IDENTITY()"),
	CLOUDSCAPE("VALUES IDENTITY_VAL_LOCAL()"),
	DERBY("VALUES IDENTITY_VAL_LOCAL()"),
	HSQLDB("CALL IDENTITY()"),
	SYBASE("SELECT @@IDENTITY"),
	DB2_MF("SELECT IDENTITY_VAL_LOCAL() FROM SYSIBM.SYSDUMMY1"),
	INFORMIX("select dbinfo('sqlca.sqlerrd1') from systables where tabid=1");

	private String identityRetrievalStatement;

	IdentityDialectEnum(String identityRetrievalStatement) {
		this.identityRetrievalStatement = identityRetrievalStatement;
	}

	public static IdentityDialectEnum getDatabaseIdentityDialect(String database) {
		IdentityDialectEnum dialect;
		switch (database.toUpperCase(Locale.US)) {
			case "DB2":
				dialect = DB2;
				break;
			case "MYSQL":
				dialect = MYSQL;
				break;
			case "SQLSERVER":
				dialect = SQLSERVER;
				break;
			case "CLOUDSCAPE":
				dialect = CLOUDSCAPE;
				break;
			case "DERBY":
				dialect = DERBY;
				break;
			case "HSQLDB":
				dialect = HSQLDB;
				break;
			case "SYBASE":
				dialect = SYBASE;
				break;
			case "DB2_MF":
				dialect = DB2_MF;
				break;
			case "INFORMIX":
				dialect = INFORMIX;
				break;
			default: {
				dialect = MYSQL;
			}
		}
		return dialect;
	}

	public String getIdentityRetrievalStatement() {
		return identityRetrievalStatement;
	}
}
