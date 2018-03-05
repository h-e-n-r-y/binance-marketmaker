package de.hw4.binance.marketmaker;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerPrice;

@Controller
public class TickerController {
	
	@Autowired
	BinanceClientFactory clientFactory;
	
	@RequestMapping(value = "/ticker.js", method = RequestMethod.GET)
    public String getPrice(
    		@RequestParam(value="symbols", required=true) String pSymbols,
    		Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

		BinanceApiRestClient client = clientFactory.getClient(userName);
		
		List<String> symbols = new ArrayList<>();
		List<String> prices = new ArrayList<>();
		for (String symbol : pSymbols.split(",")) {
			TickerPrice price = client.getPrice(symbol);
			symbols.add(price.getSymbol());
			prices.add(price.getPrice());
		}	
		
		model.addAttribute("symbols", symbols);
		model.addAttribute("prices", prices);
		return "js/ticker";
	}
}
