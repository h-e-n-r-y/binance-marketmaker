package de.hw4.binance.marketmaker;


import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class NotificationController {
	
	static Logger logger = LoggerFactory.getLogger(NotificationController.class);


	static int count = 0;
	
	@RequestMapping(value = "/notifications.js", method = RequestMethod.GET)
    public String balances(
    		@RequestParam(value="since", required=true) Long pSince,
    		Model model) {
		
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        
        if (currentPrincipalName == null || "anonymousUser".equals(currentPrincipalName)) {
            model.addAttribute("notificationcount", 0);
        } else {
	        Date now = new Date();
	        
	        String[] titles = {"title #1", "Title #2"};
	        String[] messages = {"msg1 " + count++, "msg 2 " + count++};
	
	        model.addAttribute("title", titles);
	        model.addAttribute("message", messages);
	        model.addAttribute("notificationcount", 0);
        }        
        return "js/notifications";
        
	}
}
