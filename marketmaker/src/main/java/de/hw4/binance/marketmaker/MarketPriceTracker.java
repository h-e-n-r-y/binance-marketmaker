package de.hw4.binance.marketmaker;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;

public class MarketPriceTracker {

	private BinanceApiRestClient binanceClient;
	
	private String marketSymbol;
	
	MarketPriceTracker(String pSymbol) {
		marketSymbol = pSymbol;
		

	}
}
