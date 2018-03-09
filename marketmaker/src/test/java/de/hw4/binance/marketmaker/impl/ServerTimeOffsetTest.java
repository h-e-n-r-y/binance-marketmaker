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
		assertTrue("Timeoffset to big. Consider adjusting the system clock! Offset: " + offset, Math.abs(offset) < 2000);
	}
}
