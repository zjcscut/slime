package org.throwable.mapper.support.plugins.sort;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.Iterator;
import java.util.List;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/7 12:41
 */
@Getter
public class Sort implements Iterable<Order> {

	private final List<Order> orders;

	public Sort() {
		orders = Lists.newArrayList();
	}

	public Sort(List<Order> orders) {
		if (null == orders) {
			this.orders = Lists.newArrayList();
		} else {
			this.orders = orders;
		}
	}

	public void addSort(Order order) {
		this.orders.add(order);
	}

	@Override
	public Iterator<Order> iterator() {
		return orders.iterator();
	}

	public String getSortClause() {
		if (orders.isEmpty()) {
			return " ";
		}
		final StringBuilder builder = new StringBuilder(" ORDER BY ");
		this.orders.forEach(a -> builder.append(a.getOrderClause()).append(","));
		return builder.substring(0, builder.lastIndexOf(","));
	}
}
