package de.hw4.binance.marketmaker;


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

import de.hw4.binance.marketmaker.persistence.Notification;
import de.hw4.binance.marketmaker.persistence.NotificationRepository;

@Controller
public class NotificationController {
	
	static Logger logger = LoggerFactory.getLogger(NotificationController.class);

	@Autowired
	NotificationRepository notificationRepo;

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
	        
        	List<Notification> latestNotifications = 
        			notificationRepo.findByUserAndTimestampGreaterThan(currentPrincipalName, pSince);
	        
	        String[] titles = new String[latestNotifications.size()];
	        String[] messages = new String[latestNotifications.size()];
	        int i = 0;
	        for (Notification notification : latestNotifications) {
	        	titles[i] = notification.getTitle();
	        	messages[i] = notification.getMessage();
	        	i++;
	        }
	
	        model.addAttribute("title", titles);
	        model.addAttribute("message", messages);
	        model.addAttribute("notificationcount", latestNotifications.size());
        }        
        return "js/notifications";
        
	}
}
