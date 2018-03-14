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
		chartIntervalCfg.put(ChartInterval.FOURHOUR, new ChartIntervalConfig(CandlestickInterval.ONE_MINUTE, 14_400_000L));
		chartIntervalCfg.put(ChartInterval.DAY, new ChartIntervalConfig(CandlestickInterval.FIVE_MINUTES, 86_400_000L));
		chartIntervalCfg.put(ChartInterval.WEEK, new ChartIntervalConfig(CandlestickInterval.TWO_HOURLY, 604_800_000L));
		chartIntervalCfg.put(ChartInterval.MONTH, new ChartIntervalConfig(CandlestickInterval.EIGHT_HOURLY, 2_678_400_000L));
	}
	
	@Autowired
	BinanceClientFactory clientFactory;
	
	static Logger logger = LoggerFactory.getLogger(ChartController.class);


	@RequestMapping(value = "/public/chart", method = RequestMethod.GET)
    public String chart(
    		@RequestParam(value="symbol", required=true) String pSymbol,
    		Model model) {
		
		BinanceApiRestClient binanceClient = clientFactory.getClient();
        ExchangeInfo exchangeInfo = clientFactory.getExchangeInfo();

        collectChartData(binanceClient, exchangeInfo, pSymbol, model);
        model.addAttribute("symbol", pSymbol);
        return "include/chart";
        
	}

	@RequestMapping(value = "/public/chart.js", method = RequestMethod.GET)
    public String chartscript(
    		@RequestParam(value="symbol", required=true) String pSymbol,
    		Model model) {
		
		BinanceApiRestClient binanceClient = clientFactory.getClient();
        ExchangeInfo exchangeInfo = clientFactory.getExchangeInfo();

        collectChartData(binanceClient, exchangeInfo, pSymbol, model);
        model.addAttribute("symbol", pSymbol);
        return "js/chart";
        
	}

	protected static void collectChartData(BinanceApiRestClient binanceClient, ExchangeInfo exchangeInfo, String pSymbol, Model model) {

        long now = System.currentTimeMillis();
        ChartInterval interval = ChartInterval.WEEK;
        ChartIntervalConfig cfg = chartIntervalCfg.get(interval);
		List<Candlestick> chartData = binanceClient.getCandlestickBars(pSymbol, cfg.interval, 101, now - cfg.millis, now );
        List<List<Object>> googleChartData = new ArrayList<>();
        
        DateFormat df = (interval == ChartInterval.WEEK || interval == ChartInterval.MONTH) ? 
        		new SimpleDateFormat("dd.MM.") : new SimpleDateFormat("HH:mm");
        
        for(Candlestick cs : chartData) {
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
	}

}
