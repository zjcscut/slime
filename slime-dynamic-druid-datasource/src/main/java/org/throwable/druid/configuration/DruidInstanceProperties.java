package org.throwable.druid.configuration;

import java.util.List;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/30 0:41
 */
public class DruidInstanceProperties {

	private List<DruidInstance> druids;

	public DruidInstanceProperties() {
	}

	public List<DruidInstance> getDruids() {
		return druids;
	}

	public void setDruids(List<DruidInstance> druids) {
		this.druids = druids;
	}
}
