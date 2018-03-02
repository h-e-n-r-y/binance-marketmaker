package de.hw4.binance.marketmaker;

import java.util.List;

import com.binance.api.client.BinanceApiRestClient;

import de.hw4.binance.marketmaker.impl.AssetBalanceImpl;
import de.hw4.binance.marketmaker.impl.OrderImpl;
import de.hw4.binance.marketmaker.persistence.SchedulerTask;

public interface Trader {
	
	public Status trade(SchedulerTask pTask);
	
	public String proposeTradingAction(String pSymbol, BinanceApiRestClient binanceClient, 
			List<OrderImpl> displayOrders, List<AssetBalanceImpl> displayBalances, TradingAction action);


}
