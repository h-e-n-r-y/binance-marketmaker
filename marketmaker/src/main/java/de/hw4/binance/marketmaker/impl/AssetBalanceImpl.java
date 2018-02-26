package de.hw4.binance.marketmaker.impl;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.market.TickerPrice;

public class AssetBalanceImpl {
	
	AssetBalance balance;
	
	BigDecimal free;
	BigDecimal locked;
	BigDecimal value;
	String valueSymbol;
	
	
	static Logger logger = LoggerFactory.getLogger(AssetBalanceImpl.class);
	
	public AssetBalanceImpl(AssetBalance pBalance, TickerPrice pPrice) {
		this.balance = pBalance;
		String assetSymbol = pBalance.getAsset();
		this.free = Utils.parseDecimal(pBalance.getFree());
		BigDecimal tickerPrice = Utils.parseDecimal(pPrice.getPrice());
		this.locked = Utils.parseDecimal(pBalance.getLocked());
		String tickerSymbol = pPrice.getSymbol();
		String asset1 = Utils.getSymbol1(tickerSymbol);
		String asset2 = Utils.getSymbol2(tickerSymbol);
		if (asset1.equals(assetSymbol)) {
			value = free.multiply(tickerPrice);
			valueSymbol = asset2;
		} else {
			value = free.divide(tickerPrice, BigDecimal.ROUND_HALF_UP);
			valueSymbol = asset1;
		}
	}
	
	public BigDecimal getValue() {
		return value;
	}

	public String getValueSymbol() {
		return valueSymbol;
	}

	public String getAsset() {
		return balance.getAsset();
	}
	
	public BigDecimal getFree() {
		return free;
	}

	public BigDecimal getLocked() {
		return locked;
	}
}
