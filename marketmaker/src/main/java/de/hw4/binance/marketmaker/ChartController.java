package de.hw4.binance.marketmaker;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.request.AllOrdersRequest;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;

import de.hw4.binance.marketmaker.impl.Utils;

@Controller
public class ChartController {
	
	private static class ChartIntervalConfig {
		CandlestickInterval interval;
		long millis;
		private ChartIntervalConfig(CandlestickInterval pCsi, long pRange) {
			this.interval = pCsi;
			this.millis = pRange;
		}
	}
	
	private static Map<ChartInterval, ChartIntervalConfig> chartIntervalCfg = new EnumMap<>(ChartInterval.class);
	static {
		chartIntervalCfg.put(ChartInterval.HOUR, new ChartIntervalConfig(CandlestickInterval.ONE_MINUTE, 12_000_000L));
		chartIntervalCfg.put(ChartInterval.EIGHTHOUR, new ChartIntervalConfig(CandlestickInterval.FIVE_MINUTES, 60_000_000L));
		chartIntervalCfg.put(ChartInterval.DAY, new ChartIntervalConfig(CandlestickInterval.FIFTEEN_MINUTES, 180_000_000L));
		chartIntervalCfg.put(ChartInterval.WEEK, new ChartIntervalConfig(CandlestickInterval.TWO_HOURLY, 1_440_000_000L));
		chartIntervalCfg.put(ChartInterval.MONTH, new ChartIntervalConfig(CandlestickInterval.EIGHT_HOURLY, 5_760_000_000L));
		chartIntervalCfg.put(ChartInterval.SIXMONTH, new ChartIntervalConfig(CandlestickInterval.DAILY, 31_104_000_000L)); // actually only getting data for 3 months
	}
	
	@Autowired
	BinanceClientFactory clientFactory;
	
	static Logger logger = LoggerFactory.getLogger(ChartController.class);


	@RequestMapping(value = "/public/chart", method = RequestMethod.GET)
    public String chart(
    		@RequestParam(value="symbol", required=true) String pSymbol,
    		@RequestParam(value="interval", required=true) String pInterval,
    		@RequestParam(value="limit", required=false) String pLimit,
    		Model model) {
		
		ChartInterval interval = ChartInterval.valueOf(pInterval);

		BinanceApiRestClient binanceClient = clientFactory.getClient();
        ExchangeInfo exchangeInfo = clientFactory.getExchangeInfo();

        BigDecimal limit = null;
		if (pLimit != null ) {
			limit = Utils.parseDecimal(pLimit);
		}

        collectChartData(binanceClient, exchangeInfo, pSymbol, interval, limit, model);
        model.addAttribute("symbol", pSymbol);
        return "include/chart";
        
	}

	@RequestMapping(value = "/public/chart.js", method = RequestMethod.GET)
    public String chartscript(
    		@RequestParam(value="symbol", required=true) String pSymbol,
    		@RequestParam(value="interval", required=true) String pInterval,
    		@RequestParam(value="limit", required=false) String pLimit,
    		Model model) {
		
		ChartInterval interval = ChartInterval.valueOf(pInterval);
		BigDecimal limit = null;
		if (pLimit != null && !pLimit.equals("undefined") && !pLimit.equals("null")) {
			limit = Utils.parseDecimal(pLimit);
		}
		
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        
		BinanceApiRestClient binanceClient = clientFactory.getClient(userName);
        ExchangeInfo exchangeInfo = clientFactory.getExchangeInfo();

        collectChartData(binanceClient, exchangeInfo, pSymbol, interval, limit, model);
        model.addAttribute("symbol", pSymbol);
        return "js/chart";
        
	}

	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100L);
	protected static void collectChartData(BinanceApiRestClient binanceClient, ExchangeInfo exchangeInfo, 
			String pSymbol, ChartInterval pInterval, BigDecimal pLimit, Model model) {
		
		BigDecimal limit = pLimit;
		List<Order> orders = null;
		AllOrdersRequest or = new AllOrdersRequest(pSymbol);
		orders = binanceClient.getAllOrders(or);
		
        long now = System.currentTimeMillis();
        ChartIntervalConfig cfg = chartIntervalCfg.get(pInterval);
		List<Candlestick> chartData = binanceClient.getCandlestickBars(pSymbol, cfg.interval, 500, now - cfg.millis, now );
        List<List<Object>> googleChartData = new ArrayList<>();
        
        DateFormat df = (pInterval == ChartInterval.WEEK || pInterval == ChartInterval.MONTH || pInterval == ChartInterval.SIXMONTH) ? 
        		new SimpleDateFormat("dd.MM.") : new SimpleDateFormat("HH:mm");
        
        int s = chartData.size();
        BigDecimal sum = BigDecimal.valueOf(0L);
        BigDecimal actlimit = null;
        for(int i = 0; i < chartData.size(); i++) {
        		Candlestick cs = chartData.get(i);
        		BigDecimal close = Utils.parseDecimal(cs.getClose());
        		sum = sum.add(close);
        		if (i >= 100) {
        			Candlestick cs2 = chartData.get(i - 100);
            		BigDecimal close2 = Utils.parseDecimal(cs2.getClose());
        			sum = sum.subtract(close2);
        		}
        		if (i < s - 100) {
        			continue;
        		}
        		List<Object> gcs = new ArrayList<>();
        		
        		gcs.add(df.format(new Date(cs.getOpenTime())));
        		BigDecimal low = Utils.parseDecimal(cs.getLow());
        		gcs.add(Utils.scalePrice(low, pSymbol, exchangeInfo));
        		BigDecimal open = Utils.parseDecimal(cs.getOpen());
        		gcs.add(Utils.scalePrice(open, pSymbol, exchangeInfo));
        		gcs.add(Utils.scalePrice(close, pSymbol, exchangeInfo));
        		BigDecimal high = Utils.parseDecimal(cs.getHigh());
        		gcs.add(Utils.scalePrice(high, pSymbol, exchangeInfo));
        		BigDecimal average = sum.divide(ONE_HUNDRED, 8, RoundingMode.HALF_EVEN);
        		gcs.add(Utils.scalePrice(average, pSymbol, exchangeInfo));

        		if (limit != null) {
            		if ((i == s - 100) || (i == s - 1)) {
            			gcs.add(limit);
            		} else {
            			gcs.add(null);
            		}
        		}
        		if (i == s - 100) {
        			if (orders != null) {
        				actlimit = getOrderLimitBefore(orders, cs.getCloseTime());
        				gcs.add(actlimit);
        			}
        		} else if (i == s - 1) {
    				gcs.add(actlimit);
        		} else {
    				BigDecimal curlimit = getOrderLimit(orders, cs.getOpenTime(), cs.getCloseTime());
    				gcs.add(curlimit);
    				if (curlimit != null) {
    					actlimit = curlimit;
    				}
        		}

        		googleChartData.add(gcs);
        }
        
        model.addAttribute("chartData", googleChartData);
        model.addAttribute("interval", pInterval.name());
        model.addAttribute("withLimit", limit != null || actlimit != null);
        model.addAttribute("withLimitAndHistory", limit != null && actlimit != null);

	}
	
	private static BigDecimal getOrderLimit(List<Order> pOrders, long pOpen, long pClose) {
		
		long candleDuration = pClose - pOpen;
		String lastPrice = null;
		for (Order p : pOrders) {
			if (p.getStatus() == OrderStatus.FILLED || p.getStatus() == OrderStatus.NEW || p.getStatus() == OrderStatus.PARTIALLY_FILLED) {
				if (p.getTime() > pOpen && p.getTime() <= pClose) {
					return getOrderLimitBefore(pOrders, pClose);
				}
				if (p.getTime() > pOpen  + candleDuration && p.getTime() <= pClose + candleDuration ) {
					if (lastPrice == null) {
						return null;
					}
					return Utils.parseDecimal(lastPrice);
				}
				if (p.getTime() > pOpen + candleDuration + 1) {
					return null;
				}
				lastPrice = p.getPrice();
			}
		}
		return null;
	}
	
	private static BigDecimal getOrderLimitBefore(List<Order> pOrders, long pClose) {
		BigDecimal limit = null;
		for (Order p : pOrders) {
			if (p.getStatus() == OrderStatus.FILLED || p.getStatus() == OrderStatus.NEW || p.getStatus() == OrderStatus.PARTIALLY_FILLED) {
				if (p.getTime() <= pClose) {
					limit = Utils.parseDecimal(p.getPrice());
				}
			}
		}
		return limit;
	}

}
