package de.hw4.binance.marketmaker.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
	
	static Logger logger = LoggerFactory.getLogger(Utils.class);
	
	static DecimalFormat decFmt = new DecimalFormat();
	static {
		decFmt.setParseBigDecimal(true);
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
}
