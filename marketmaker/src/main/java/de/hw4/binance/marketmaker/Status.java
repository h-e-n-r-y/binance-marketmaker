package de.hw4.binance.marketmaker;

/**
 * Status for a trading pair.
 * 
 * Example NEOETH
 * asset1: NEO
 * asset2: ETH
 * 
 * @author henry
 *
 */
public enum Status {

	UNKNOWN,
	WAITING, // for order to be filled
	PROPOSE_BUY, // Buy asset1 (Sell asset2)
	PROPOSE_SELL; // Sell asset1 (Buy asset2)
	
	public String getName() {
		return name();
	}
}

