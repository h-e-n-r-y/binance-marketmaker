package de.hw4.binance.marketmaker;

import java.math.BigDecimal;

import com.binance.api.client.domain.market.TickerPrice;

public class TradingAction {
	
	private Status status;
	
	private BigDecimal tradePrice;
	private BigDecimal quantity;
	TickerPrice tickerPrice;
	
	public TradingAction(TickerPrice pTickerPrice) {
		tickerPrice = pTickerPrice;
		status = Status.UNKNOWN;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public BigDecimal getTradePrice() {
		return tradePrice;
	}
	
	public void setTradePrice(BigDecimal tradePrice) {
		this.tradePrice = tradePrice;
	}
	
	public BigDecimal getQuantity() {
		return quantity;
	}
	
	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public TickerPrice getTickerPrice() {
		return tickerPrice;
	}

}
