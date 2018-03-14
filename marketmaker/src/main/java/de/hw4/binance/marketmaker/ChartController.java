package de.hw4.binance.marketmaker;


import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.binance.api.client.BinanceApiRestClient;
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
	
	private static Map<ChartInterval, ChartIntervalConfig> chartIntervalCfg = new HashMap<>();
	static {
		chartIntervalCfg.put(ChartInterval.HOUR, new ChartIntervalConfig(CandlestickInterval.ONE_MINUTE, 6_000_000L));
		chartIntervalCfg.put(ChartInterval.EIGHTHOUR, new ChartIntervalConfig(CandlestickInterval.FIVE_MINUTES, 24_800_000L));
		chartIntervalCfg.put(ChartInterval.DAY, new ChartIntervalConfig(CandlestickInterval.FIFTEEN_MINUTES, 86_400_000L));
		chartIntervalCfg.put(ChartInterval.WEEK, new ChartIntervalConfig(CandlestickInterval.TWO_HOURLY, 604_800_000L));
		chartIntervalCfg.put(ChartInterval.MONTH, new ChartIntervalConfig(CandlestickInterval.EIGHT_HOURLY, 2_678_400_000L));
	}
	
	@Autowired
	BinanceClientFactory clientFactory;
	
	static Logger logger = LoggerFactory.getLogger(ChartController.class);


	@RequestMapping(value = "/public/chart", method = RequestMethod.GET)
    public String chart(
    		@RequestParam(value="symbol", required=true) String pSymbol,
    		@RequestParam(value="interval", required=true) String pInterval,
    		Model model) {
		
		ChartInterval interval = ChartInterval.valueOf(pInterval);

		BinanceApiRestClient binanceClient = clientFactory.getClient();
        ExchangeInfo exchangeInfo = clientFactory.getExchangeInfo();

        collectChartData(binanceClient, exchangeInfo, pSymbol, interval, model);
        model.addAttribute("symbol", pSymbol);
        return "include/chart";
        
	}

	@RequestMapping(value = "/public/chart.js", method = RequestMethod.GET)
    public String chartscript(
    		@RequestParam(value="symbol", required=true) String pSymbol,
    		@RequestParam(value="interval", required=true) String pInterval,
    		Model model) {
		
		ChartInterval interval = ChartInterval.valueOf(pInterval);
		
		BinanceApiRestClient binanceClient = clientFactory.getClient();
        ExchangeInfo exchangeInfo = clientFactory.getExchangeInfo();

        collectChartData(binanceClient, exchangeInfo, pSymbol, interval, model);
        model.addAttribute("symbol", pSymbol);
        return "js/chart";
        
	}

	protected static void collectChartData(BinanceApiRestClient binanceClient, ExchangeInfo exchangeInfo, String pSymbol, ChartInterval pInterval, Model model) {

        long now = System.currentTimeMillis();
        ChartIntervalConfig cfg = chartIntervalCfg.get(pInterval);
		List<Candlestick> chartData = binanceClient.getCandlestickBars(pSymbol, cfg.interval, 500, now - cfg.millis, now );
        List<List<Object>> googleChartData = new ArrayList<>();
        
        DateFormat df = (pInterval == ChartInterval.WEEK || pInterval == ChartInterval.MONTH) ? 
        		new SimpleDateFormat("dd.MM.") : new SimpleDateFormat("HH:mm");
        
        // int s = chartData.size();
        	// int i = 0;
        for(Candlestick cs : chartData) {
        		/*
        		if (i++ < s - 100) {
        			continue;
        		}
        		*/
        		List<Object> gcs = new ArrayList<>();
        		
        		gcs.add(df.format(new Date(cs.getOpenTime())));
        		BigDecimal low = Utils.parseDecimal(cs.getLow());
        		gcs.add(Utils.scalePrice(low, pSymbol, exchangeInfo));
        		BigDecimal open = Utils.parseDecimal(cs.getOpen());
        		gcs.add(Utils.scalePrice(open, pSymbol, exchangeInfo));
        		BigDecimal close = Utils.parseDecimal(cs.getClose());
        		gcs.add(Utils.scalePrice(close, pSymbol, exchangeInfo));
        		BigDecimal high = Utils.parseDecimal(cs.getHigh());
        		gcs.add(Utils.scalePrice(high, pSymbol, exchangeInfo));

        		googleChartData.add(gcs);
        }
        
        model.addAttribute("chartData", googleChartData);
        model.addAttribute("interval", pInterval.name());

	}

}
