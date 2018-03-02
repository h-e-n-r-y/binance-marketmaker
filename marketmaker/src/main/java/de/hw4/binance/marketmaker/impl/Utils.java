package de.hw4.binance.marketmaker.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.FilterType;
import com.binance.api.client.domain.general.SymbolInfo;

public class Utils {
	
	static Logger logger = LoggerFactory.getLogger(Utils.class);
	
	static DecimalFormatSymbols decSymbols = new DecimalFormatSymbols(Locale.US);
	static DecimalFormat decFmt = new DecimalFormat("0.00000000", decSymbols);
	static DecimalFormat priceFmt = new DecimalFormat("0.########", decSymbols);
	static DecimalFormat qtyFmt = new DecimalFormat("0.00", decSymbols);
	static {
		decFmt.setParseBigDecimal(true);
		priceFmt.setParseBigDecimal(true);
		qtyFmt.setParseBigDecimal(true);
	}

	private Utils(){		
	}

	public static String getSymbol(String pSymbol) {
        int sep = pSymbol.indexOf('_');
        if (sep > 0) {
            return pSymbol.substring(0, sep) + pSymbol.substring(sep + 1, pSymbol.length());
        } else {
            return pSymbol.substring(0, pSymbol.length() - 3) + pSymbol.substring(pSymbol.length() - 3, pSymbol.length());
        }
	}
	
	public static String getSymbol1(String pSymbol) {
        int sep = pSymbol.indexOf('_');
        if (sep > 0) {
            return pSymbol.substring(0, sep);
        } else {
            return pSymbol.substring(0, pSymbol.length() - 3);
        }
	}
	
	public static String getSymbol2(String pSymbol) {
        int sep = pSymbol.indexOf('_');
        if (sep > 0) {
            return pSymbol.substring(sep + 1, pSymbol.length());
        } else {
            return pSymbol.substring(pSymbol.length() - 3, pSymbol.length());
        }
	}
	public static BigDecimal parseDecimal(String pString) {
		try {
			return (BigDecimal) decFmt.parse(pString);
		} catch (ParseException e) {
			logger.error("Failed to parse decimal '" + pString + "'", e);
		}
		return BigDecimal.valueOf(0);
	}

	public static String formatDecimal(BigDecimal pDecimal) {
		return decFmt.format(pDecimal);
	}

	/**
	 * Format a price according to it's ticksize rule. 
	 * @param pDecimal
	 * @param pSymbol
	 * @param pExchangeInfo
	 * @return
	 */
	public static String formatPrice(BigDecimal pDecimal, String pSymbol, ExchangeInfo pExchangeInfo) {
		SymbolInfo symbolInfo = pExchangeInfo.getSymbolInfo(pSymbol);
		String tickSize = symbolInfo.getSymbolFilter(FilterType.PRICE_FILTER).getTickSize();
		return roundToTickSize(pDecimal, tickSize);
	}

	protected static String roundToTickSize(BigDecimal pDecimal, String tickSize) {
		int precision = tickSize.indexOf('1') - 1;
		return priceFmt.format(pDecimal.setScale(precision, RoundingMode.HALF_UP));
	}

	public static String formatQuantity(BigDecimal pQuantity) {
		return qtyFmt.format(pQuantity);
	}

}
