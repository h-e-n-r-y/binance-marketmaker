package de.hw4.binance.marketmaker;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import groovy.lang.ExpandoMetaClassCreationHandle;

@Controller
public class ChartController {
	
	@Autowired
	BinanceClientFactory clientFactory;
	
	static Logger logger = LoggerFactory.getLogger(ChartController.class);


	@RequestMapping(value = "/public/chart", method = RequestMethod.GET)
    public String chart(
    		@RequestParam(value="symbol", required=true) String pSymbol,
    		Model model) {
		
        
        BinanceApiRestClient binanceClient = clientFactory.getClient();
        
        model.addAttribute("symbol", pSymbol);

        
        return "include/chart";
        
	}

	@RequestMapping(value = "/public/chart.js", method = RequestMethod.GET)
    public String chartscript(
    		@RequestParam(value="symbol", required=true) String pSymbol,
    		Model model) {
		
        
        BinanceApiRestClient binanceClient = clientFactory.getClient();
        ExchangeInfo exchangeInfo = clientFactory.getExchangeInfo();

        long now = System.currentTimeMillis();
		List<Candlestick> chartData = binanceClient.getCandlestickBars(pSymbol, CandlestickInterval.ONE_MINUTE, 101, now - 6000000L, now );
        List<List<Object>> googleChartData = new ArrayList<>();
        
        DateFormat df = new SimpleDateFormat("HH:mm");
        
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
        model.addAttribute("symbol", pSymbol);
        return "js/chart";
        
	}

}
