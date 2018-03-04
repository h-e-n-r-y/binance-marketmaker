package de.hw4.binance.marketmaker.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.request.AllOrdersRequest;
import com.binance.api.client.domain.market.TickerPrice;
import com.binance.api.client.exception.BinanceApiException;

import de.hw4.binance.marketmaker.BinanceClientFactory;
import de.hw4.binance.marketmaker.Status;
import de.hw4.binance.marketmaker.Trader;
import de.hw4.binance.marketmaker.TradingAction;
import de.hw4.binance.marketmaker.persistence.SchedulerTask;
import de.hw4.binance.marketmaker.persistence.SchedulerTaskRepository;

@Component
public class TraderImpl implements Trader {
	
	// TODO: configurable
	// of last sell order qty
	private static final BigDecimal BUY_PERCENTAGE_DEFAULT = BigDecimal.valueOf(0.005); 
	// there is no last order
	private static final BigDecimal BUY_PERCENTAGE_CURRENTPRICE = BigDecimal.valueOf(0.005); 
	
	// of last buy-order price 
	private static final BigDecimal SELL_PERCENTAGE_DEFAULT = BigDecimal.valueOf(0.01);
	
	// When market price is higher than last Buy
	private static final BigDecimal SELL_PERCENTAGE_CURRENTPRICE = BigDecimal.valueOf(0.005);

	private static final BigDecimal ONE = BigDecimal.valueOf(1.0);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100.0);
	
	@Autowired
	BinanceClientFactory clientFactory;

	@Autowired
	SchedulerTaskRepository schedulerTaskRepo;

	@Override
	public TradingAction trade(SchedulerTask pTask) {
		String username = pTask.getUser();
		BinanceApiRestClient apiClient = clientFactory.getClient(username);
		TickerPrice tickerPrice = apiClient.getPrice(pTask.getSymbol());
		TradingAction action = new TradingAction(tickerPrice);
		List<OrderImpl> displayOrders = new ArrayList<>();
		List<AssetBalanceImpl> displayBalances = new ArrayList<>();
		proposeTradingAction(apiClient, username, displayOrders, displayBalances, action);
		
		return action;
	}
	
	
	public void proposeTradingAction(BinanceApiRestClient binanceClient, String pUser,
			List<OrderImpl> displayOrders, List<AssetBalanceImpl> displayBalances, TradingAction action) {
		
		String symbol = action.getTickerPrice().getSymbol();
        String symbol1 = Utils.getSymbol1(symbol);
        String symbol2 = Utils.getSymbol2(symbol);
        String errormsg = null;

        SchedulerTask schedulerTask = schedulerTaskRepo.findOne(SchedulerTask.hash(pUser, symbol));
        
		// Orders
        AllOrdersRequest orderRequest = new AllOrdersRequest(symbol);
        try {
        		List<Order> allOrders = binanceClient.getAllOrders(orderRequest);
        		for (Order order : allOrders) {
        			if (order.getStatus() == OrderStatus.FILLED || 
        					order.getStatus() == OrderStatus.NEW || 
        					order.getStatus() == OrderStatus.PARTIALLY_FILLED) {
        				displayOrders.add(0, new OrderImpl(order));
        			}
        		}
        } catch (BinanceApiException bae){
			errormsg = bae.getMessage();
        }
        
        AssetBalanceImpl assetBalance1 = null;
        AssetBalanceImpl assetBalance2 = null;
        
        List<AssetBalance> balances = binanceClient.getAccount().getBalances();
        for (AssetBalance bal : balances) {
        		String asset = bal.getAsset();
			if (symbol1.equals(asset) ||
        				symbol2.equals(asset)) {
        			AssetBalanceImpl assetBalance = new AssetBalanceImpl(bal, action.getTickerPrice());
				
				if (symbol1.equals(asset)) {
					assetBalance1 = assetBalance;
					displayBalances.add(0, assetBalance);
				} else {
					assetBalance2 = assetBalance;
					displayBalances.add(assetBalance);
				}
        		}
        }
        
        OrderImpl lastOrder = displayOrders.isEmpty() ? null : displayOrders.get(0);
        
        if (!displayOrders.isEmpty()) {
        		// inspect last order
        		if (lastOrder.getStatus() == OrderStatus.NEW ||
        				lastOrder.getStatus() == OrderStatus.PARTIALLY_FILLED) {
        			action.setStatus(Status.WAITING);
        		}
        }
        
        
        if (action.getStatus() == Status.UNKNOWN) {
        		BigDecimal tradePrice = null;
        		BigDecimal price = Utils.parseDecimal(action.getTickerPrice().getPrice());
	        	BigDecimal free2 = assetBalance2 == null ? BigDecimal.valueOf(0) : assetBalance2.getFree();
	        	
	    		if (assetBalance1 == null || assetBalance1.getValue().compareTo(free2) < 0) {
	    			// BUY
	    			BigDecimal percentageFactorPrice = ONE.subtract(BUY_PERCENTAGE_CURRENTPRICE);
	    			if (lastOrder != null) {
	    				BigDecimal buyPercentage = schedulerTask.getBuyPercentage();
	    				if (buyPercentage == null) {
	    					buyPercentage = BUY_PERCENTAGE_DEFAULT;
	    				} else {
	    					buyPercentage = buyPercentage.divide(HUNDRED, 8, RoundingMode.HALF_EVEN);
	    				}
	    				BigDecimal percentageFactor = ONE.subtract(buyPercentage); 
	    				tradePrice =  free2.divide(lastOrder.getOrigQty(), 8, RoundingMode.HALF_DOWN).multiply(percentageFactor);
	    				if (tradePrice.compareTo(price) > 0) {
	    					// current price is less 
		    				// take current price
		    				tradePrice = price.multiply(percentageFactorPrice);
	    				}
	    			} else {
	    				// take current price
	    				tradePrice = price.multiply(percentageFactorPrice);
	    			}
	    			action.setQuantity(free2.divide(tradePrice, 2, RoundingMode.FLOOR));
	        		action.setStatus(Status.PROPOSE_BUY);
	    		} else {
	    			// Sell
	    			BigDecimal percentageFactorPrice = ONE.add(SELL_PERCENTAGE_CURRENTPRICE);
	    			if (lastOrder != null) {
	    				BigDecimal sellPercentage = schedulerTask.getSellPercentage();
	    				if (sellPercentage == null) {
	    					sellPercentage = SELL_PERCENTAGE_DEFAULT;
	    				} else {
	    					sellPercentage = sellPercentage.divide(HUNDRED, 8, RoundingMode.HALF_EVEN);
	    				}
	    				BigDecimal percentageFactor = ONE.add(sellPercentage); 
	    				tradePrice = lastOrder.getPrice().multiply(percentageFactor);
	    			}
	    			if (tradePrice == null || tradePrice.compareTo(price) < 0) {
	    				tradePrice = price.multiply(percentageFactorPrice);
	    			}
	    			action.setQuantity(assetBalance1.getFree());
	    			action.setStatus(Status.PROPOSE_SELL);
	    		}
	    		action.setTradePrice(tradePrice);
        }
        if (errormsg != null) {
        		action.setErrorMsg(errormsg);
    			action.setStatus(Status.ERROR);
        }
	}


}
