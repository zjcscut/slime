package org.throwable.druid.configuration;


/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/1 20:02
 */
public class DruidInstance {

	private volatile Boolean defaultAutoCommit;
	private volatile Boolean defaultReadOnly;
	private volatile Integer defaultTransactionIsolation;
	private volatile String defaultCatalog;
	
	private volatile String username;
	private volatile String password;
	private volatile String url;
	private volatile String driverClassName;


	private volatile Integer initialSize;
	private volatile Integer maxActive;
	private volatile Integer minIdle;
	private volatile Integer maxIdle;
	private volatile Long maxWait;
	private Integer notFullTimeoutRetryCount;

	private volatile String validationQuery;
	private volatile Integer validationQueryTimeout;
	private volatile Boolean testOnBorrow;
	private volatile Boolean testOnReturn;
	private volatile Boolean testWhileIdle;
	private volatile Boolean poolPreparedStatements;
	private volatile Boolean sharePreparedStatements;
	private volatile Integer maxPoolPreparedStatementPerConnectionSize;


	private volatile Integer queryTimeout;
	private volatile Integer transactionQueryTimeout;


	private Boolean clearFiltersEnable;
	private volatile Integer maxWaitThreadCount;

	private volatile Boolean accessToUnderlyingConnectionAllowed;

	private volatile Long timeBetweenEvictionRunsMillis;

	private volatile Integer numTestsPerEvictionRun;

	private volatile Long minEvictableIdleTimeMillis;
	private volatile Long maxEvictableIdleTimeMillis;

	private volatile Long phyTimeoutMillis;

	private volatile Boolean removeAbandoned;

	private volatile Long removeAbandonedTimeoutMillis;

	private volatile Boolean logAbandoned;

	private volatile Integer maxOpenPreparedStatements;

	private volatile String dbType;

	private volatile Long timeBetweenConnectErrorMillis;

	private Integer connectionErrorRetryAttempts;

	private Boolean breakAfterAcquireFailure;

	private Long transactionThresholdMillis;

	private Boolean failFast;
	private Integer maxCreateTaskCount;
	private Boolean asyncCloseConnectionEnable;
	private Boolean initVariants;
	private Boolean initGlobalVariants;

	private Boolean useUnfairLock;

	private Boolean useLocalSessionState;

	private Long timeBetweenLogStatsMillis;
	private String exceptionSorter;
	
	//filter配置
	private String filters;
	private String signature;  //唯一标识,同时作为name属性
	private Boolean primary; //是否主数据源,只有一个

	public DruidInstance() {
	}

	public Boolean getDefaultAutoCommit() {
		return defaultAutoCommit;
	}

	public void setDefaultAutoCommit(Boolean defaultAutoCommit) {
		this.defaultAutoCommit = defaultAutoCommit;
	}

	public Boolean getDefaultReadOnly() {
		return defaultReadOnly;
	}

	public void setDefaultReadOnly(Boolean defaultReadOnly) {
		this.defaultReadOnly = defaultReadOnly;
	}

	public Integer getDefaultTransactionIsolation() {
		return defaultTransactionIsolation;
	}

	public void setDefaultTransactionIsolation(Integer defaultTransactionIsolation) {
		this.defaultTransactionIsolation = defaultTransactionIsolation;
	}

	public String getDefaultCatalog() {
		return defaultCatalog;
	}

	public void setDefaultCatalog(String defaultCatalog) {
		this.defaultCatalog = defaultCatalog;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public Integer getInitialSize() {
		return initialSize;
	}

	public void setInitialSize(Integer initialSize) {
		this.initialSize = initialSize;
	}

	public Integer getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(Integer maxActive) {
		this.maxActive = maxActive;
	}

	public Integer getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(Integer minIdle) {
		this.minIdle = minIdle;
	}

	public Integer getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(Integer maxIdle) {
		this.maxIdle = maxIdle;
	}

	public Long getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(Long maxWait) {
		this.maxWait = maxWait;
	}

	public Integer getNotFullTimeoutRetryCount() {
		return notFullTimeoutRetryCount;
	}

	public void setNotFullTimeoutRetryCount(Integer notFullTimeoutRetryCount) {
		this.notFullTimeoutRetryCount = notFullTimeoutRetryCount;
	}

	public String getValidationQuery() {
		return validationQuery;
	}

	public void setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
	}

	public Integer getValidationQueryTimeout() {
		return validationQueryTimeout;
	}

	public void setValidationQueryTimeout(Integer validationQueryTimeout) {
		this.validationQueryTimeout = validationQueryTimeout;
	}

	public Boolean getTestOnBorrow() {
		return testOnBorrow;
	}

	public void setTestOnBorrow(Boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}

	public Boolean getTestOnReturn() {
		return testOnReturn;
	}

	public void setTestOnReturn(Boolean testOnReturn) {
		this.testOnReturn = testOnReturn;
	}

	public Boolean getTestWhileIdle() {
		return testWhileIdle;
	}

	public void setTestWhileIdle(Boolean testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
	}

	public Boolean getPoolPreparedStatements() {
		return poolPreparedStatements;
	}

	public void setPoolPreparedStatements(Boolean poolPreparedStatements) {
		this.poolPreparedStatements = poolPreparedStatements;
	}

	public Boolean getSharePreparedStatements() {
		return sharePreparedStatements;
	}

	public void setSharePreparedStatements(Boolean sharePreparedStatements) {
		this.sharePreparedStatements = sharePreparedStatements;
	}

	public Integer getMaxPoolPreparedStatementPerConnectionSize() {
		return maxPoolPreparedStatementPerConnectionSize;
	}

	public void setMaxPoolPreparedStatementPerConnectionSize(Integer maxPoolPreparedStatementPerConnectionSize) {
		this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
	}

	public Integer getQueryTimeout() {
		return queryTimeout;
	}

	public void setQueryTimeout(Integer queryTimeout) {
		this.queryTimeout = queryTimeout;
	}

	public Integer getTransactionQueryTimeout() {
		return transactionQueryTimeout;
	}

	public void setTransactionQueryTimeout(Integer transactionQueryTimeout) {
		this.transactionQueryTimeout = transactionQueryTimeout;
	}

	public Boolean getClearFiltersEnable() {
		return clearFiltersEnable;
	}

	public void setClearFiltersEnable(Boolean clearFiltersEnable) {
		this.clearFiltersEnable = clearFiltersEnable;
	}

	public Integer getMaxWaitThreadCount() {
		return maxWaitThreadCount;
	}

	public void setMaxWaitThreadCount(Integer maxWaitThreadCount) {
		this.maxWaitThreadCount = maxWaitThreadCount;
	}

	public Boolean getAccessToUnderlyingConnectionAllowed() {
		return accessToUnderlyingConnectionAllowed;
	}

	public void setAccessToUnderlyingConnectionAllowed(Boolean accessToUnderlyingConnectionAllowed) {
		this.accessToUnderlyingConnectionAllowed = accessToUnderlyingConnectionAllowed;
	}

	public Long getTimeBetweenEvictionRunsMillis() {
		return timeBetweenEvictionRunsMillis;
	}

	public void setTimeBetweenEvictionRunsMillis(Long timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}

	public Integer getNumTestsPerEvictionRun() {
		return numTestsPerEvictionRun;
	}

	public void setNumTestsPerEvictionRun(Integer numTestsPerEvictionRun) {
		this.numTestsPerEvictionRun = numTestsPerEvictionRun;
	}

	public Long getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}

	public void setMinEvictableIdleTimeMillis(Long minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	public Long getMaxEvictableIdleTimeMillis() {
		return maxEvictableIdleTimeMillis;
	}

	public void setMaxEvictableIdleTimeMillis(Long maxEvictableIdleTimeMillis) {
		this.maxEvictableIdleTimeMillis = maxEvictableIdleTimeMillis;
	}

	public Long getPhyTimeoutMillis() {
		return phyTimeoutMillis;
	}

	public void setPhyTimeoutMillis(Long phyTimeoutMillis) {
		this.phyTimeoutMillis = phyTimeoutMillis;
	}

	public Boolean getRemoveAbandoned() {
		return removeAbandoned;
	}

	public void setRemoveAbandoned(Boolean removeAbandoned) {
		this.removeAbandoned = removeAbandoned;
	}

	public Long getRemoveAbandonedTimeoutMillis() {
		return removeAbandonedTimeoutMillis;
	}

	public void setRemoveAbandonedTimeoutMillis(Long removeAbandonedTimeoutMillis) {
		this.removeAbandonedTimeoutMillis = removeAbandonedTimeoutMillis;
	}

	public Boolean getLogAbandoned() {
		return logAbandoned;
	}

	public void setLogAbandoned(Boolean logAbandoned) {
		this.logAbandoned = logAbandoned;
	}

	public Integer getMaxOpenPreparedStatements() {
		return maxOpenPreparedStatements;
	}

	public void setMaxOpenPreparedStatements(Integer maxOpenPreparedStatements) {
		this.maxOpenPreparedStatements = maxOpenPreparedStatements;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public Long getTimeBetweenConnectErrorMillis() {
		return timeBetweenConnectErrorMillis;
	}

	public void setTimeBetweenConnectErrorMillis(Long timeBetweenConnectErrorMillis) {
		this.timeBetweenConnectErrorMillis = timeBetweenConnectErrorMillis;
	}

	public Integer getConnectionErrorRetryAttempts() {
		return connectionErrorRetryAttempts;
	}

	public void setConnectionErrorRetryAttempts(Integer connectionErrorRetryAttempts) {
		this.connectionErrorRetryAttempts = connectionErrorRetryAttempts;
	}

	public Boolean getBreakAfterAcquireFailure() {
		return breakAfterAcquireFailure;
	}

	public void setBreakAfterAcquireFailure(Boolean breakAfterAcquireFailure) {
		this.breakAfterAcquireFailure = breakAfterAcquireFailure;
	}

	public Long getTransactionThresholdMillis() {
		return transactionThresholdMillis;
	}

	public void setTransactionThresholdMillis(Long transactionThresholdMillis) {
		this.transactionThresholdMillis = transactionThresholdMillis;
	}

	public Boolean getFailFast() {
		return failFast;
	}

	public void setFailFast(Boolean failFast) {
		this.failFast = failFast;
	}

	public Integer getMaxCreateTaskCount() {
		return maxCreateTaskCount;
	}

	public void setMaxCreateTaskCount(Integer maxCreateTaskCount) {
		this.maxCreateTaskCount = maxCreateTaskCount;
	}

	public Boolean getAsyncCloseConnectionEnable() {
		return asyncCloseConnectionEnable;
	}

	public void setAsyncCloseConnectionEnable(Boolean asyncCloseConnectionEnable) {
		this.asyncCloseConnectionEnable = asyncCloseConnectionEnable;
	}

	public Boolean getInitVariants() {
		return initVariants;
	}

	public void setInitVariants(Boolean initVariants) {
		this.initVariants = initVariants;
	}

	public Boolean getInitGlobalVariants() {
		return initGlobalVariants;
	}

	public void setInitGlobalVariants(Boolean initGlobalVariants) {
		this.initGlobalVariants = initGlobalVariants;
	}

	public Boolean getUseUnfairLock() {
		return useUnfairLock;
	}

	public void setUseUnfairLock(Boolean useUnfairLock) {
		this.useUnfairLock = useUnfairLock;
	}

	public Boolean getUseLocalSessionState() {
		return useLocalSessionState;
	}

	public void setUseLocalSessionState(Boolean useLocalSessionState) {
		this.useLocalSessionState = useLocalSessionState;
	}

	public Long getTimeBetweenLogStatsMillis() {
		return timeBetweenLogStatsMillis;
	}

	public void setTimeBetweenLogStatsMillis(Long timeBetweenLogStatsMillis) {
		this.timeBetweenLogStatsMillis = timeBetweenLogStatsMillis;
	}

	public String getExceptionSorter() {
		return exceptionSorter;
	}

	public void setExceptionSorter(String exceptionSorter) {
		this.exceptionSorter = exceptionSorter;
	}

	public String getFilters() {
		return filters;
	}

	public void setFilters(String filters) {
		this.filters = filters;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public Boolean getPrimary() {
		return primary;
	}

	public void setPrimary(Boolean primary) {
		this.primary = primary;
	}
}
