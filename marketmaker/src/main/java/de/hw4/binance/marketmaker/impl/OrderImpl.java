package de.hw4.binance.marketmaker.impl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.account.Order;

public class OrderImpl {

	Order order;

	public OrderImpl(Order pOrder) {
		this.order = pOrder;
	}

	public OrderStatus getStatus() {
		return order.getStatus();
	}

	public OrderSide getSide() {
		return order.getSide();
	}

	public BigDecimal getPrice() {
		return Utils.parseDecimal(order.getPrice());
	}

	public long getOrderId() {
		return order.getOrderId();
	}

	public Date getDate() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(order.getTime());
		return cal.getTime();
	}

	public BigDecimal getOrigQty() {
		return Utils.parseDecimal(order.getOrigQty());
	}
	
	public BigDecimal getExecutedQty() {
		return Utils.parseDecimal(order.getExecutedQty());
	}
	
	/**
	 * @return Quantity of other asset.
	 */
	public BigDecimal getQty2() {
		return getOrigQty().multiply(getPrice());
	}
	
	public boolean getCanCancel() {
		return order.getStatus() == OrderStatus.NEW;
	}

}
