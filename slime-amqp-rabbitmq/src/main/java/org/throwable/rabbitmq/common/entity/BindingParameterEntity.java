package org.throwable.rabbitmq.common.entity;

import org.throwable.rabbitmq.configuration.ConsumerBindingParameter;

import java.util.Date;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/24 14:49
 */
public class BindingParameterEntity extends ConsumerBindingParameter {

	private String  bindingType;
	private String instanceSignature;

	private Integer isEnabled;
	private Date createTime;
	private Date updateTime;

	public String getBindingType() {
		return bindingType;
	}

	public void setBindingType(String bindingType) {
		this.bindingType = bindingType;
	}

	public String getInstanceSignature() {
		return instanceSignature;
	}

	public void setInstanceSignature(String instanceSignature) {
		this.instanceSignature = instanceSignature;
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
