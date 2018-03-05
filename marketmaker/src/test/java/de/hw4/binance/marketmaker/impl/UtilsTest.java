package de.hw4.binance.marketmaker.impl;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;

public class UtilsTest {

	@Test
	public void testGetSymbol() {
		assertEquals("symbol wrong", "NEOETH", Utils.getSymbol("NEOETH"));
		assertEquals("symbol wrong", "IOSTETH", Utils.getSymbol("IOSTETH"));
		assertEquals("symbol wrong", "NEOETH", Utils.getSymbol("NEO_ETH"));
		assertEquals("symbol wrong", "IOSTETH", Utils.getSymbol("IOST_ETH"));
		assertEquals("symbol wrong", "BTCUSDT", Utils.getSymbol("BTC_USDT"));
		assertEquals("symbol wrong", "BTCUSDT", Utils.getSymbol("BTCUSDT"));
	}
	@Test
	public void testGetSymbol1() {
		assertEquals("symbol1 wrong", "NEO", Utils.getSymbol1("NEOETH"));
		assertEquals("symbol1 wrong", "IOST", Utils.getSymbol1("IOSTETH"));
		assertEquals("symbol1 wrong", "NEO", Utils.getSymbol1("NEO_ETH"));
		assertEquals("symbol1 wrong", "IOST", Utils.getSymbol1("IOST_ETH"));
		assertEquals("symbol1 wrong", "BTC", Utils.getSymbol1("BTC_USDT"));
		assertEquals("symbol1 wrong", "BTC", Utils.getSymbol1("BTCUSDT"));
	}
	@Test
	public void testGetSymbol2() {
		assertEquals("symbol2 wrong", "ETH", Utils.getSymbol2("NEOETH"));
		assertEquals("symbol2 wrong", "ETH", Utils.getSymbol2("IOSTETH"));
		assertEquals("symbol2 wrong", "ETH", Utils.getSymbol2("NEO_ETH"));
		assertEquals("symbol2 wrong", "ETH", Utils.getSymbol2("IOST_ETH"));
		assertEquals("symbol2 wrong", "USDT", Utils.getSymbol2("BTC_USDT"));
		assertEquals("symbol2 wrong", "USDT", Utils.getSymbol2("BTCUSDT"));
	}
	
	@Test
	public void testFormatDecimal() {
		assertEquals("0.00012345", Utils.formatDecimal(BigDecimal.valueOf(0.00012345d)));
		assertEquals("10.00012345", Utils.formatDecimal(BigDecimal.valueOf(10.00012345d)));
		assertEquals("0.00012346", Utils.formatDecimal(BigDecimal.valueOf(0.000123459d)));
		assertEquals("10.00000000", Utils.formatDecimal(BigDecimal.valueOf(10)));
	}
	
	@Test
	public void testRoundToTickSize() {
		assertEquals("0.12346", Utils.roundToTickSize(BigDecimal.valueOf(0.123456789d), "0.00001"));
		assertEquals("0.12345", Utils.roundToTickSize(BigDecimal.valueOf(0.12345111d), "0.00001"));
		assertEquals("0.123", Utils.roundToTickSize(BigDecimal.valueOf(0.12345111d), "0.001"));
	}
}
