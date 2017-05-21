package org.throwable.lock.configuration;

/**
 * @author throwable
 * @version v1.0
 * @function
 * @since 2017/5/18 17:47
 */
public class ZookeeperClientConfiguration {

    private ZookeeperClientProperties zookeeperClientProperties;

    public ZookeeperClientConfiguration() {
    }

    public ZookeeperClientProperties getZookeeperClientProperties() {
        return zookeeperClientProperties;
    }

    public void setZookeeperClientProperties(ZookeeperClientProperties zookeeperClientProperties) {
        this.zookeeperClientProperties = zookeeperClientProperties;
    }
}
