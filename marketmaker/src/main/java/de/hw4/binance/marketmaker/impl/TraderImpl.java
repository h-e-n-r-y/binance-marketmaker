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

import de.hw4.binance.marketmaker.BinanceClientComponent;
import de.hw4.binance.marketmaker.Status;
import de.hw4.binance.marketmaker.Trader;
import de.hw4.binance.marketmaker.TradingAction;
import de.hw4.binance.marketmaker.persistence.SchedulerTask;

@Component
public class TraderImpl implements Trader {
	
	// of last sell order qty
	BigDecimal buyPercentage = BigDecimal.valueOf(0.995); // TODO: configurable
	// there is no last order
	BigDecimal buyPercentageCurrentPrice = BigDecimal.valueOf(0.995); // TODO: configurable
	
	// of last buy-order price 
	BigDecimal sellPercentage = BigDecimal.valueOf(1.01); // TODO: configurable
	
	// When market price is higher than last Buy
	BigDecimal sellPercentageCurrentPrice = BigDecimal.valueOf(1.005);

	
	@Autowired
	BinanceClientComponent clientFactory;

	@Override
	public TradingAction trade(SchedulerTask pTask) {
		String username = pTask.getUser();
		BinanceApiRestClient apiClient = clientFactory.getClient(username);
		TickerPrice tickerPrice = apiClient.getPrice(pTask.getSymbol());
		TradingAction action = new TradingAction(tickerPrice);
		List<OrderImpl> displayOrders = new ArrayList<>();
		List<AssetBalanceImpl> displayBalances = new ArrayList<>();
		proposeTradingAction(apiClient, displayOrders, displayBalances, action);
		
		return action;
	}
	
	
	public void proposeTradingAction(BinanceApiRestClient binanceClient, 
			List<OrderImpl> displayOrders, List<AssetBalanceImpl> displayBalances, TradingAction action) {
		
		String symbol = action.getTickerPrice().getSymbol();
        String symbol1 = Utils.getSymbol1(symbol);
        String symbol2 = Utils.getSymbol2(symbol);
        String errormsg = null;

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
				displayBalances.add(assetBalance);
				
				if (symbol1.equals(asset)) {
					assetBalance1 = assetBalance;
				} else {
					assetBalance2 = assetBalance;
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
	    			if (lastOrder != null) {
	    				tradePrice =  free2.divide(lastOrder.getOrigQty(), 8, RoundingMode.HALF_UP).multiply(buyPercentage);
	    				if (tradePrice.compareTo(price) > 0) {
	    					// current price is less 
		    				// take current price
		    				tradePrice = price.multiply(buyPercentageCurrentPrice);
	    				}
	    			} else {
	    				// take current price
	    				tradePrice = price.multiply(buyPercentageCurrentPrice);
	    			}
	    			action.setQuantity(free2.divide(tradePrice, 2, RoundingMode.FLOOR));
	        		action.setStatus(Status.PROPOSE_BUY);
	    		} else {
	    			// Sell
	    			if (lastOrder != null) {
	    				tradePrice = lastOrder.getPrice().multiply(sellPercentage);
	    			}
	    			if (tradePrice == null || tradePrice.compareTo(price) < 0) {
	    				tradePrice = price.multiply(sellPercentageCurrentPrice);
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
