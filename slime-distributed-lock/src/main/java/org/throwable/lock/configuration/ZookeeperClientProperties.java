package org.throwable.lock.configuration;

/**
 * @author throwable
 * @version v1.0
 * @function
 * @since 2017/5/18 15:38
 */
public class ZookeeperClientProperties {

    private String connectString;
    private Integer sessionTimeoutMs;
    private Integer connectionTimeoutMs;
    private Integer baseSleepTimeMs;
    private Integer maxRetries;
    private String baseLockPath;

    public ZookeeperClientProperties() {
    }

    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    public Integer getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public void setSessionTimeoutMs(Integer sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public Integer getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(Integer connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public Integer getBaseSleepTimeMs() {
        return baseSleepTimeMs;
    }

    public void setBaseSleepTimeMs(Integer baseSleepTimeMs) {
        this.baseSleepTimeMs = baseSleepTimeMs;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public String getBaseLockPath() {
        return baseLockPath;
    }

    public void setBaseLockPath(String baseLockPath) {
        this.baseLockPath = baseLockPath;
    }
}
