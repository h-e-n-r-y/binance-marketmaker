package de.hw4.binance.marketmaker.impl;

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
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.request.OrderRequest;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.exception.BinanceApiException;

import de.hw4.binance.marketmaker.BinanceClientFactory;
import de.hw4.binance.marketmaker.Status;
import de.hw4.binance.marketmaker.Trader;
import de.hw4.binance.marketmaker.TradingAction;
import de.hw4.binance.marketmaker.persistence.Notification;
import de.hw4.binance.marketmaker.persistence.NotificationRepository;
import de.hw4.binance.marketmaker.persistence.SchedulerTask;
import de.hw4.binance.marketmaker.persistence.SchedulerTaskRepository;

@Component
public class Scheduler {

    private static final Logger log = LoggerFactory.getLogger(Scheduler.class);
    
    @Autowired
    SchedulerTaskRepository tasksRepo;
    
    @Autowired
    NotificationRepository notificationRepo;
    
    @Autowired
    BinanceClientFactory clientFactory;
    
    @Autowired
    Trader trader;

    @Scheduled(fixedRate = 5000)
    public void run() {
        
        List<SchedulerTask> activeTasks = tasksRepo.findByActive(true);
        if (activeTasks.isEmpty()) {
        		log.info("No active Tasks.");
        }
        for (SchedulerTask task : activeTasks) {
    		BinanceApiRestClient apiClient = clientFactory.getClient(task.getUser());
    		
    		List<Order> openOrders = apiClient.getOpenOrders(new OrderRequest(task.getMarketSymbol()));
    		for (Order order : openOrders) {
    			long orderId = order.getOrderId();
    			if (order.getType() == OrderType.LIMIT && (task.getCurrentOrderId() == null || task.getCurrentOrderId() != orderId)) {
    				// update task
    				log.info("updating Order in Task: {}", order);
    				task.setCurrentOrderId(orderId);
    				task.setCurrentOrderPrice(Utils.parseDecimal(order.getPrice()));
    				task.setCurrentOrderQty(Utils.parseDecimal(order.getOrigQty()));
    				task.setCurrentOrderSite(order.getSide().name());
    				tasksRepo.save(task);
    			}
    		}
       		TradingAction action = trader.trade(task);
        		
    		String tradePriceLog = "";
    		if (action.getTradePrice() != null) {
    			tradePriceLog = "@" + action.getTradePrice();
    			log.info("Task ({}: {} {} {})", task.getUser(), action.getTickerPrice(), action.getStatus(), tradePriceLog);
    		} else {
    			log.info("Task ({}: {} waiting {} {}@{})", task.getUser(), action.getTickerPrice(), 
    					task.getCurrentOrderSite(),
    					Utils.formatQuantity(task.getCurrentOrderQty()), 
    					Utils.formatDecimal(task.getCurrentOrderPrice()));
    		}
        		
        		
            if (action.getStatus() == Status.PROPOSE_BUY || action.getStatus() == Status.PROPOSE_SELL) {
            		ExchangeInfo exchangeInfo = apiClient.getExchangeInfo();
            		NewOrder order = new NewOrder(action.getTickerPrice().getSymbol(), 
            				action.getStatus() == Status.PROPOSE_BUY ? OrderSide.BUY : OrderSide.SELL, 
            				OrderType.LIMIT, TimeInForce.GTC, 
            				Utils.formatQuantity(action.getQuantity()), 
            				Utils.formatPrice(action.getTradePrice(), action.getTickerPrice().getSymbol(), exchangeInfo));
            		try {
                		Utils.sleep(50); // prevent weird server time issues.
            			NewOrderResponse newOrderResponse = apiClient.newOrder(order);
            			task.setCurrentOrderPrice(action.getTradePrice());
            			task.setCurrentOrderQty(action.getQuantity());
            			task.setCurrentOrderId(newOrderResponse.getOrderId());
            			task.setCurrentOrderSite(action.getStatus() == Status.PROPOSE_BUY ? "BUY" : "SELL");
            			tasksRepo.save(task);
            			
            			Notification notification = new Notification(task.getUser(), "New Order", 
            					(action.getStatus() == Status.PROPOSE_BUY ? "BUY" : "SELL") + " " + 
            							task.getMarketSymbol() + ": " +
            							Utils.formatQuantity(action.getQuantity()) + 
            							"@" + Utils.formatDecimal(action.getTradePrice()));
            			notification.setSymbol(Utils.getSymbol1(task.getMarketSymbol()));
            			notificationRepo.save(notification);
            			
            		} catch (BinanceApiException bae) {
            			log.error("error creating " + (action.getStatus() == Status.PROPOSE_BUY ? "BUY" : "SELL") + " order: ", 
            					bae.getMessage());
            		}
            }

        }
    }

}
