package de.hw4.binance.marketmaker;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

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
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.request.AllOrdersRequest;
import com.binance.api.client.domain.account.request.CancelOrderRequest;
import com.binance.api.client.domain.market.TickerPrice;

import de.hw4.binance.marketmaker.impl.AssetBalanceImpl;
import de.hw4.binance.marketmaker.impl.OrderImpl;
import de.hw4.binance.marketmaker.impl.Utils;

@Controller
public class MarketController {
	
	// of last sell order qty
	BigDecimal buyPercentage = BigDecimal.valueOf(0.99); // TODO: configurable
	// there is no last order
	BigDecimal buyPercentageCurrentPrice = BigDecimal.valueOf(0.99); // TODO: configurable
	
	// of last buy-order price 
	BigDecimal sellPercentage = BigDecimal.valueOf(1.02); // TODO: configurable
	
	// When market price is higher than last Buy
	BigDecimal sellPercentageCurrentPrice = BigDecimal.valueOf(1.01);

	@Autowired
	BinanceClientComponent clientFactory;
	
	static Logger logger = LoggerFactory.getLogger(MarketController.class);


	@RequestMapping(value = "/trade", method = RequestMethod.POST)
    public String trade(
    		@RequestParam(value="symbol", required=true) String pSymbol, 
    		@RequestParam(value="cancelOrder", required=false) String pCancelOrder, 
    		@RequestParam(value="orderID", required=false) String pOrderID, 
    		Model model) {
		
		Status status = Status.UNKNOWN;
		

        String symbol1 = Utils.getSymbol1(pSymbol);
        String symbol2 = Utils.getSymbol2(pSymbol);
        String symbol = Utils.getSymbol(pSymbol);
        model.addAttribute("symbol", symbol);
        model.addAttribute("symbol1", symbol1);
        model.addAttribute("symbol2", symbol2);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        
        BinanceApiRestClient binanceClient = clientFactory.getClient(currentPrincipalName);
        
        if (binanceClient == null) {
            model.addAttribute("errormsg", "No valid api key found for user '" + currentPrincipalName + "'");
            model.addAttribute("errormsg2", "Please contact the administrator!");
            return "error";
        }
        
        if (pCancelOrder != null && pCancelOrder.equals("Cancel")) {
        		// Cancel Order
        		CancelOrderRequest cancelOrderRequest = new CancelOrderRequest(symbol, Long.parseLong(pOrderID));
        		binanceClient.cancelOrder(cancelOrderRequest);
        }
        
        
        TickerPrice tickerPrice = binanceClient.getPrice(symbol);
        BigDecimal price = Utils.parseDecimal(tickerPrice.getPrice());
        model.addAttribute("price", tickerPrice.getPrice());
        
        // Orders
        AllOrdersRequest orderRequest = new AllOrdersRequest(symbol);
        List<Order> allOrders = binanceClient.getAllOrders(orderRequest);
        List<OrderImpl> displayOrders = new ArrayList<>();
        for (Order order : allOrders) {
        		if (order.getStatus() == OrderStatus.FILLED || 
        				order.getStatus() == OrderStatus.NEW || 
        				order.getStatus() == OrderStatus.PARTIALLY_FILLED) {
        			displayOrders.add(0, new OrderImpl(order));
        		}
        }
        model.addAttribute("orders", displayOrders);
        
        AssetBalanceImpl assetBalance1 = null;
        AssetBalanceImpl assetBalance2 = null;
        
        List<AssetBalance> balances = binanceClient.getAccount().getBalances();
        List<AssetBalanceImpl> displayBalances = new ArrayList<>();
        for (AssetBalance bal : balances) {
        		String asset = bal.getAsset();
			if (symbol1.equals(asset) ||
        				symbol2.equals(asset)) {
        			AssetBalanceImpl assetBalance = new AssetBalanceImpl(bal, tickerPrice);
				displayBalances.add(assetBalance);
				
				if (symbol1.equals(asset)) {
					assetBalance1 = assetBalance;
				} else {
					assetBalance2 = assetBalance;
				}
        		}
        }
        model.addAttribute("balances", displayBalances);
        
        OrderImpl lastOrder = displayOrders.isEmpty() ? null : displayOrders.get(0);
        
        if (!displayOrders.isEmpty()) {
        		// inspect last order
        		if (lastOrder.getStatus() == OrderStatus.NEW ||
        				lastOrder.getStatus() == OrderStatus.PARTIALLY_FILLED) {
        			status = Status.WAITING;
        		}
        }
        
        
        
        BigDecimal tradePrice = null;
        
        
        if (status == Status.UNKNOWN) {
	        	BigDecimal free2 = assetBalance2.getFree();
	        	
	    		if (assetBalance1 == null || assetBalance1.getValue().compareTo(assetBalance2.getFree()) < 0) {
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
	        		status = Status.PROPOSE_BUY;
	    		} else {
	    			// Sell
	    			if (lastOrder != null) {
	    				tradePrice = lastOrder.getPrice().multiply(sellPercentage);
	    			}
	    			if (tradePrice == null || tradePrice.compareTo(price) < 0) {
	    				tradePrice = price.multiply(sellPercentageCurrentPrice);
	    			}
	    			status = Status.PROPOSE_SELL;
	    		}
        		
        }

        model.addAttribute("status", status);
        model.addAttribute("tradePrice", tradePrice);

        return "trade";
    }
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Model model) {
        return "index";
	}
	
	@RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Model model) {
        return "login";
	}
	
}
