package de.hw4.binance.marketmaker;

import com.binance.api.client.BinanceApiRestClient;

public interface BinanceClientComponent {
	
	/**
	 * Return an ApiRestClient for the given pUsername.
	 * Reads apikey and secret from api-keys/pUsername.properties
	 * 
	 * @param pUsername the Name of the User whose key shall be used.
	 * 
	 * @return the Client.
	 */
	BinanceApiRestClient getClient(String pUsername);

}
