package de.hw4.binance.marketmaker.impl;

import org.springframework.stereotype.Component;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;

import de.hw4.binance.marketmaker.BinanceClientComponent;

@Component
public class BinanceClientComponentImpl implements BinanceClientComponent {

	private BinanceApiRestClient binanceClient;
	
	public BinanceClientComponentImpl() {
		// TODO do not hard code! 
		binanceClient = BinanceApiClientFactory.newInstance("p5kSWssbN7WahHLwxFZlBYhjvWx2Wc7dZ1jXpVr2eAPKBcZO6ndZH2nWhAna2CJh", 
				"jPL2v3TCrW4K93dqvY1HtE8NwdPfliYO1CJRUBfg8Ji6owyifPkfY5VSemeUuIKs").newRestClient();	
		}

	public BinanceApiRestClient getClient() {
		return binanceClient;
	}
}
