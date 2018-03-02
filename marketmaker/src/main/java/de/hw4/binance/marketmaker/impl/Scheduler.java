package de.hw4.binance.marketmaker.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderType;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.exception.BinanceApiException;

import de.hw4.binance.marketmaker.BinanceClientComponent;
import de.hw4.binance.marketmaker.Status;
import de.hw4.binance.marketmaker.Trader;
import de.hw4.binance.marketmaker.TradingAction;
import de.hw4.binance.marketmaker.persistence.SchedulerTask;
import de.hw4.binance.marketmaker.persistence.SchedulerTaskRepository;

@Component
public class Scheduler {

    private static final Logger log = LoggerFactory.getLogger(Scheduler.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    
    @Autowired
    SchedulerTaskRepository tasksRepo;
    
    @Autowired
    BinanceClientComponent clientFactory;
    
    @Autowired
    Trader trader;

    @Scheduled(fixedRate = 10000)
    public void reportCurrentTime() {
        
        List<SchedulerTask> activeTasks = tasksRepo.findByActive(true);
        if (activeTasks.isEmpty()) {
        		log.info("No active Tasks.");
        }
        for (SchedulerTask task : activeTasks) {
        		TradingAction action = trader.trade(task);
        		String tradePriceLog = "";
        		if (action.getTradePrice() != null) {
        			tradePriceLog = "@" + action.getTradePrice();
        		}
        		log.info("Task ({}: {} {} {})", task.getUser(), action.getTickerPrice(), action.getStatus(), tradePriceLog);
        		
        		
            if (action.getStatus() == Status.PROPOSE_BUY || action.getStatus() == Status.PROPOSE_SELL) {
            		BinanceApiRestClient apiClient = clientFactory.getClient(task.getUser());
            		ExchangeInfo exchangeInfo = apiClient.getExchangeInfo();
            		NewOrder order = new NewOrder(action.getTickerPrice().getSymbol(), 
            				action.getStatus() == Status.PROPOSE_BUY ? OrderSide.BUY : OrderSide.SELL, 
            				OrderType.LIMIT, TimeInForce.GTC, 
            				Utils.formatQuantity(action.getQuantity()), 
            				Utils.formatPrice(action.getTradePrice(), action.getTickerPrice().getSymbol(), exchangeInfo));
            		try {
            			apiClient.newOrder(order);
            		} catch (BinanceApiException bae) {
            			log.error("error creating " + (action.getStatus() == Status.PROPOSE_BUY ? "BUY" : "SELL") + " order: ", 
            					bae.getMessage());
            		}
            }

        }
    }

}
