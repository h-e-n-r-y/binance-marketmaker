package de.hw4.binance.marketmaker.impl;

import org.junit.Test;
import static org.junit.Assert.*;

public class UtilsTest {

	@Test
	public void testGetSymbol() {
		assertEquals("symbol wrong", "NEOETH", Utils.getSymbol("NEOETH"));
		assertEquals("symbol wrong", "IOSTETH", Utils.getSymbol("IOSTETH"));
		assertEquals("symbol wrong", "NEOETH", Utils.getSymbol("NEO_ETH"));
		assertEquals("symbol wrong", "IOSTETH", Utils.getSymbol("IOST_ETH"));
	}
	@Test
	public void testGetSymbol1() {
		assertEquals("symbol1 wrong", "NEO", Utils.getSymbol1("NEOETH"));
		assertEquals("symbol1 wrong", "IOST", Utils.getSymbol1("IOSTETH"));
		assertEquals("symbol1 wrong", "NEO", Utils.getSymbol1("NEO_ETH"));
		assertEquals("symbol1 wrong", "IOST", Utils.getSymbol1("IOST_ETH"));
	}
	@Test
	public void testGetSymbol2() {
		assertEquals("symbol2 wrong", "ETH", Utils.getSymbol2("NEOETH"));
		assertEquals("symbol2 wrong", "ETH", Utils.getSymbol2("IOSTETH"));
		assertEquals("symbol2 wrong", "ETH", Utils.getSymbol2("NEO_ETH"));
		assertEquals("symbol2 wrong", "ETH", Utils.getSymbol2("IOST_ETH"));
	}
}
