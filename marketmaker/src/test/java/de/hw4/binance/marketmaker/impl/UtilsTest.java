package de.hw4.binance.marketmaker.impl;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.FilterType;
import com.binance.api.client.domain.general.SymbolFilter;
import com.binance.api.client.domain.general.SymbolInfo;

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
		assertEquals("0.12345", Utils.roundToTickSize(BigDecimal.valueOf(0.123456789d), "0.00001"));
		assertEquals("0.12345", Utils.roundToTickSize(BigDecimal.valueOf(0.12345111d), "0.00001"));
		assertEquals("0.123", Utils.roundToTickSize(BigDecimal.valueOf(0.12345111d), "0.001"));
	}
	
	@Test
	public void testFormatQuantity() {
		ExchangeInfo ex = new ExchangeInfo();
		List<SymbolInfo> symbols = new ArrayList<>();
		SymbolInfo si = new SymbolInfo();
		si.setSymbol("NEOETH");
		symbols.add(si);
		ex.setSymbols(symbols);
		
		
		SymbolFilter lotFilter = new SymbolFilter();
		lotFilter.setFilterType(FilterType.LOT_SIZE);
		lotFilter.setStepSize("0.001");
		
		List<SymbolFilter> filters = new ArrayList<>();
		filters.add(lotFilter);
		si.setFilters(filters);
		
				
		assertEquals("Quantity falsch", "1.023", Utils.formatQuantity(BigDecimal.valueOf(1.02345), "NEOETH", ex));
		assertEquals("Quantity scale falsch", 3, Utils.getQuantityScale("NEOETH", ex));
		int quantityScale = 3;
		assertEquals("Quantity step falsch", "0.001", Utils.formatQuantity(BigDecimal.valueOf(Math.pow(10.0, -quantityScale)), "NEOETH", ex));
		
	}
}
