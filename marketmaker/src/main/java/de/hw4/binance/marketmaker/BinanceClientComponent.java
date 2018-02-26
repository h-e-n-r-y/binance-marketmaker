package de.hw4.binance.marketmaker;

import com.binance.api.client.BinanceApiRestClient;

public interface BinanceClientComponent {
	
	BinanceApiRestClient getClient();

}
