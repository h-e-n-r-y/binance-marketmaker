package de.hw4.binance.marketmaker;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderType;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.request.CancelOrderRequest;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.market.TickerPrice;
import com.binance.api.client.exception.BinanceApiException;

import de.hw4.binance.marketmaker.impl.AssetBalanceImpl;
import de.hw4.binance.marketmaker.impl.OrderImpl;
import de.hw4.binance.marketmaker.impl.Utils;
import de.hw4.binance.marketmaker.persistence.SchedulerTask;
import de.hw4.binance.marketmaker.persistence.SchedulerTaskRepository;

@Controller
public class MarketController {
	
	@Autowired
	BinanceClientComponent clientFactory;
	
	@Autowired
	SchedulerTaskRepository schedulerTaskRepo;
	
	@Autowired
	Trader trader;
		
	
	static Logger logger = LoggerFactory.getLogger(MarketController.class);

	@RequestMapping(value = "/task", method = RequestMethod.POST)
    public String handleTask(
    		@RequestParam(value="symbol", required=true) String pSymbol,
    		// activate the task
    		@RequestParam(value="activateTask", required=false) String pActivateTask, 
    		// remove a task
    		@RequestParam(value="removeTask", required=false) String pRemoveTask, 

    		Model model) {
		
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        
        String symbol = Utils.getSymbol(pSymbol);

        SchedulerTask schedulerTask = schedulerTaskRepo.findOne(SchedulerTask.hash(userName, symbol));
        if (schedulerTask == null) {
        		schedulerTask = new SchedulerTask(userName, symbol);
        		schedulerTaskRepo.save(schedulerTask);
        } else {
            if ("Remove".equals(pRemoveTask)) {
            		schedulerTaskRepo.delete(schedulerTask);
                 model.addAttribute("activeTasks", schedulerTaskRepo.findByUser(userName));
            		return "index";
            }
        }
		if ("Activate".equals(pActivateTask)) {
			schedulerTask.activate();
			schedulerTaskRepo.save(schedulerTask);
		}

		if ("Deactivate".equals(pActivateTask)) {
			schedulerTask.deactivate();
			schedulerTaskRepo.save(schedulerTask);
		}
		model.addAttribute("task", schedulerTask);
		return trade(pSymbol, null, null, null, null, null, null, model);
	}

	@RequestMapping(value = "/trade", method = RequestMethod.POST)
    public String trade(
    		@RequestParam(value="symbol", required=true) String pSymbol,
    		// cancel an order
    		@RequestParam(value="cancelOrder", required=false) String pCancelOrder, 
    		@RequestParam(value="orderID", required=false) String pOrderID,
    		// create an order
    		@RequestParam(value="createOrder", required=false) String pCreateOrder, 
    		@RequestParam(value="side", required=false) String pSide,
    		@RequestParam(value="quantity", required=false) String pQuantity,
    		@RequestParam(value="pricelimit", required=false) String pPriceLimit,
    		
    		Model pModel) {
		

        String symbol1 = Utils.getSymbol1(pSymbol);
        String symbol2 = Utils.getSymbol2(pSymbol);
        String symbol = Utils.getSymbol(pSymbol);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        
        BinanceApiRestClient binanceClient = clientFactory.getClient(userName);
                
        if (binanceClient == null) {
            pModel.addAttribute("errormsg", "No valid api key found for user '" + userName + "'");
            pModel.addAttribute("errormsg2", "Please contact the administrator!");
            return "error";
        }
        
        SchedulerTask schedulerTask = schedulerTaskRepo.findOne(SchedulerTask.hash(userName, symbol));
        if (schedulerTask == null) {
        		schedulerTask = new SchedulerTask(userName, symbol);
        		schedulerTaskRepo.save(schedulerTask);
        }

        ExchangeInfo exchangeInfo = binanceClient.getExchangeInfo();
        
        if ("Cancel".equals(pCancelOrder)) {
        		// Cancel Order
        		CancelOrderRequest cancelOrderRequest = new CancelOrderRequest(symbol, Long.parseLong(pOrderID));
        		Utils.sleep(50); // prevent weird server time issues.
        		try {
        			binanceClient.cancelOrder(cancelOrderRequest);
        		} catch (BinanceApiException bae) {
        			pModel.addAttribute("errormsg", bae.getMessage());
        		}
        }
        if ("Buy".equals(pCreateOrder)) {
        		NewOrder order = new NewOrder(symbol, OrderSide.BUY, OrderType.LIMIT, TimeInForce.GTC, pQuantity, pPriceLimit);
        		Utils.sleep(50); // prevent weird server time issues.
        		try {
        			binanceClient.newOrder(order);
        		} catch (BinanceApiException bae) {
        			pModel.addAttribute("errormsg", bae.getMessage());
        		}
        }
        if ("Sell".equals(pCreateOrder)) {
        		NewOrder order = new NewOrder(symbol, OrderSide.SELL, OrderType.LIMIT, TimeInForce.GTC, pQuantity, pPriceLimit);
        		Utils.sleep(50); // prevent weird server time issues.
        		try {
        			binanceClient.newOrder(order);
        		} catch (BinanceApiException bae) {
        			pModel.addAttribute("errormsg", bae.getMessage());
        		}
        }

        
        TickerPrice tickerPrice = binanceClient.getPrice(symbol);
        List<OrderImpl> displayOrders = new ArrayList<>();
        List<AssetBalanceImpl> displayBalances = new ArrayList<>();
        TradingAction action = new TradingAction(tickerPrice);
        

        
        trader.proposeTradingAction(binanceClient, displayOrders, displayBalances, action);

        if (action.getQuantity() != null) {
        		pModel.addAttribute("quantity", Utils.formatQuantity(action.getQuantity()));
        }
        if (action.getTradePrice() != null) {
        		pModel.addAttribute("tradePrice", Utils.formatPrice(action.getTradePrice(), symbol, exchangeInfo));
        }
        pModel.addAttribute("symbol", symbol);
        pModel.addAttribute("symbol1", symbol1);
        pModel.addAttribute("symbol2", symbol2);
        pModel.addAttribute("price", tickerPrice.getPrice());
        pModel.addAttribute("status", action.getStatus());
        pModel.addAttribute("orders", displayOrders);
        pModel.addAttribute("balances", displayBalances);
        pModel.addAttribute("task", schedulerTask);
        if (action.getStatus() == Status.ERROR) {
            pModel.addAttribute("errormsg", action.getErrorMsg());
        }

        return "trade";
    }

	
	@RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        if ("sa".equals(userName)) {
        		return "redirect:/console";
        }
        List<SchedulerTask> schedulerTasks = schedulerTaskRepo.findByUser(userName);
        model.addAttribute("activeTasks", schedulerTasks);
		return "index";
	}
	
	@RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Model model) {
        return "login";
	}
	
}
