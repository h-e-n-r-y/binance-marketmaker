package de.hw4.binance.marketmaker;


import java.math.BigDecimal;
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
import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderType;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.CancelOrderResponse;
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
import de.hw4.binance.marketmaker.persistence.User;
import de.hw4.binance.marketmaker.persistence.UserRepository;

@Controller
public class MarketController {
	
	@Autowired
	BinanceClientFactory clientFactory;
	
	@Autowired
	SchedulerTaskRepository schedulerTaskRepo;
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	Trader trader;
		
	
	static Logger logger = LoggerFactory.getLogger(MarketController.class);


	@RequestMapping(value = "/task", method = RequestMethod.POST)
    public String handleTask(
    		@RequestParam(value="symbol", required=true) String pSymbol,
    		// activate the task
    		@RequestParam(value="activateTask", required=false) String pActivateTask, 
    		@RequestParam(value="winbuy", required=false) String pBuyPercentage,
    		@RequestParam(value="winsell", required=false) String pSellPercentage,
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
			BigDecimal bp = Utils.parseDecimal(pBuyPercentage);
			schedulerTask.setBuyPercentage(bp);
			BigDecimal sp = Utils.parseDecimal(pSellPercentage);
			schedulerTask.setSellPercentage(sp);
			schedulerTask.activate();
			schedulerTaskRepo.save(schedulerTask);
		}

		if ("Deactivate".equals(pActivateTask)) {
			schedulerTask.deactivate();
			schedulerTaskRepo.save(schedulerTask);
		}
		model.addAttribute("task", schedulerTask);
		return trade(pSymbol, null, null, null, null, null, null, pBuyPercentage, pSellPercentage, model);
	}
	
	@RequestMapping(value = "/trade", method = RequestMethod.GET)
    public String tradeEntry(
    		@RequestParam(value="symbol", required=true) String pSymbol,
    		Model pModel) {
		return trade(pSymbol, null, null, null, null, null, null, null, null, pModel);
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
    		//adjust percentages
    		@RequestParam(value="winbuy", required=false) String pBuyPercentage,
    		@RequestParam(value="winsell", required=false) String pSellPercentage,

    		
    		Model pModel) {
		

        String symbol1 = Utils.getSymbol1(pSymbol);
        String symbol2 = Utils.getSymbol2(pSymbol);
        String symbol = Utils.getSymbol(pSymbol);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        
        BinanceApiRestClient binanceClient = clientFactory.getClient(userName);
                
        if (binanceClient == null) {
            pModel.addAttribute("errormsg", "No valid api key found for user '" + userName + "'");
            pModel.addAttribute("errormsg2", "Please add your api key to your profile!");
            return "error";
        }
        
        BigDecimal bp = null;
        BigDecimal sp = null;
        SchedulerTask schedulerTask = schedulerTaskRepo.findOne(SchedulerTask.hash(userName, symbol));
        if (schedulerTask == null) {
        		schedulerTask = new SchedulerTask(userName, symbol);
        } else {
        		bp = schedulerTask.getBuyPercentage();
        		sp = schedulerTask.getSellPercentage();
        }
        if (pBuyPercentage != null) {
        		bp = Utils.parseDecimal(pBuyPercentage);
        		schedulerTask.setBuyPercentage(bp);
        }
        if (pSellPercentage != null) {
        		sp = Utils.parseDecimal(pSellPercentage);
        		schedulerTask.setSellPercentage(sp);
        }
        schedulerTaskRepo.save(schedulerTask);

        ExchangeInfo exchangeInfo = binanceClient.getExchangeInfo();
        
        if ("Cancel".equals(pCancelOrder)) {
        		// Cancel Order
        		try {
        			logger.info("Cancel Order {} {}", symbol, pOrderID);
        			CancelOrderRequest cancelOrderRequest = new CancelOrderRequest(symbol, Long.parseLong(pOrderID));
        			CancelOrderResponse cancelOrderResponse = binanceClient.cancelOrder(cancelOrderRequest);
        			logger.info(cancelOrderResponse.toString());
        		} catch (BinanceApiException bae) {
        			logger.error(bae.getMessage(), bae);
        			pModel.addAttribute("errormsg", bae.getMessage());
        		} catch (NumberFormatException nfe) {
        			logger.error(nfe.getMessage(), nfe);
        		} catch (Throwable t) {
        			logger.error(t.getMessage(), t);
        		}
        }
        if ("Buy".equals(pCreateOrder)) {
        		NewOrder order = new NewOrder(symbol, OrderSide.BUY, OrderType.LIMIT, TimeInForce.GTC, pQuantity, pPriceLimit);
        		Utils.sleep(50); // prevent weird server time issues.
        		try {
        			binanceClient.newOrder(order);
        		} catch (BinanceApiException bae) {
        			logger.error(bae.getMessage(), bae);
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
        			logger.error(bae.getMessage(), bae);
        		}
        }

        
        TickerPrice tickerPrice = binanceClient.getPrice(symbol);
        List<OrderImpl> displayOrders = new ArrayList<>();
        List<AssetBalanceImpl> displayBalances = new ArrayList<>();
        TradingAction action = new TradingAction(tickerPrice);
        
        User user = userRepo.findOne(userName);
        
        trader.proposeTradingAction(binanceClient, userName, displayOrders, displayBalances, action);

        if (action.getQuantity() != null) {
        		pModel.addAttribute("quantity", Utils.formatQuantity(action.getQuantity()));
        }
        if (action.getTradePrice() != null) {
			pModel.addAttribute("tradePrice", Utils.formatPrice(action.getTradePrice(), symbol, exchangeInfo));
        }
        pModel.addAttribute("symbol", symbol);
        pModel.addAttribute("symbol1", symbol1);
        pModel.addAttribute("icon1", Utils.iconUrl(symbol1));
        pModel.addAttribute("symbol2", symbol2);
        pModel.addAttribute("icon2", Utils.iconUrl(symbol2));
        if (bp != null) {
        		pModel.addAttribute("winbuy", bp);
        }
        if (sp != null) {
	        	pModel.addAttribute("winsell", sp);
        }
        pModel.addAttribute("price", Utils.parseDecimal(tickerPrice.getPrice()));
        pModel.addAttribute("symbols", new String[]{symbol});
        pModel.addAttribute("prices", new String[]{tickerPrice.getPrice()});
        pModel.addAttribute("status", action.getStatus());
        pModel.addAttribute("orders", displayOrders);
        pModel.addAttribute("balances", displayBalances);
        pModel.addAttribute("qty1", displayBalances.get(0).getFree());
        pModel.addAttribute("qty2", displayBalances.get(1).getFree());
        pModel.addAttribute("task", schedulerTask);
        pModel.addAttribute("fees", user.getFees());
        if (action.getStatus() == Status.ERROR) {
            pModel.addAttribute("errormsg", action.getErrorMsg());
        }
        
        ChartController.collectChartData(binanceClient, exchangeInfo, symbol, ChartInterval.HOUR, action.getTradePrice(), pModel);

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
        List<String> symbols = new ArrayList<>();
        for (SchedulerTask t : schedulerTasks) {
        		symbols.add(t.getMarketSymbol());
        }
        model.addAttribute("symbols", symbols);
        model.addAttribute("activeTasks", schedulerTasks);
		return "index";
	}
	
	@RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Model model) {
        return "login";
	}
	
}
