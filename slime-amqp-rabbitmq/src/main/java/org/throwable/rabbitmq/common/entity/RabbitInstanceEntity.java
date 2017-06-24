package org.throwable.rabbitmq.common.entity;

import org.throwable.rabbitmq.configuration.RabbitmqProducerInstanceProperties;

import java.util.Date;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/24 14:48
 */
public class RabbitInstanceEntity extends RabbitmqProducerInstanceProperties {

	private String instanceType;

	private Integer isEnabled;
	private Date createTime;
	private Date updateTime;

	public String getInstanceType() {
		return instanceType;
	}

	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}

	public Integer getIsEnabled() {
		return isEnabled;
	}

	public void setIsEnabled(Integer isEnabled) {
		this.isEnabled = isEnabled;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
}
