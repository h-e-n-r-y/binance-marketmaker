package de.hw4.binance.marketmaker.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;

public class ServerTimeOffsetTest {
	
	
	@Test
	public void testTimeOffset() {
	
		BinanceApiRestClient client = BinanceApiClientFactory.newInstance(null, null).newRestClient();

		long offset = System.currentTimeMillis() - client.getExchangeInfo().getServerTime();
		assertTrue("offset to big: " + offset, Math.abs(offset) < 100);
	}
}
