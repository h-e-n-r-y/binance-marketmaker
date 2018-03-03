package de.hw4.binance.marketmaker;

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
	
	@RequestMapping(value = "/ticker", method = RequestMethod.GET)
    public String getPrice(
    		@RequestParam(value="symbol", required=true) String pSymbol,
    		Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

		BinanceApiRestClient client = clientFactory.getClient(userName);
		TickerPrice price = client.getPrice(pSymbol);
		
		model.addAttribute("price", price.getPrice());
		
		return "include/ticker";
	}
}
