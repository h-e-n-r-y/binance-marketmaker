package de.hw4.binance.marketmaker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.hw4.binance.marketmaker.impl.Utils;
import de.hw4.binance.marketmaker.persistence.User;
import de.hw4.binance.marketmaker.persistence.UserRepository;

@Controller
public class UserProfileController {
	
	@Autowired
	UserRepository userRepo;
	
	@RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String getProfile(Model pModel) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        
        if (userName == null || "anonymousUser".equals(userName)) {
    		return "redirect:/login";
        }

        User user = userRepo.findOne(userName);
        pModel.addAttribute("user", user);
        
		return "profile";
	}
	
	@RequestMapping(value = "/profile", method = RequestMethod.POST)
    public String saveProfile(
    		@RequestParam(value="fees", required=false) String pFees,
    		@RequestParam(value="apikey", required=false) String pApikey,
    		@RequestParam(value="secret", required=false) String pSecret,
    		
    		// change Password
    		@RequestParam(value="changePwd", required=false) String pChangePwd,
    		@RequestParam(value="oldpwd", required=false) String pOldPwd,
    		@RequestParam(value="pwd", required=false) String pPwd,
    		@RequestParam(value="pwd2", required=false) String pPwd2,


    		Model pModel) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        User user = userRepo.findOne(userName);

        if ("Change Password".equals(pChangePwd)) {
        	if (user.validatePassword(pOldPwd)) {
        		if (pPwd.equals(pPwd2)) {
        			if (pPwd.length() < 5) {
            	        pModel.addAttribute("info", "Password to short!");
        			} else {
        				user.setPassword(pPwd);
            	        pModel.addAttribute("info", "Password successfully changed!");
            	        userRepo.save(user);
        			}
        		} else {
        	        pModel.addAttribute("info", "Passwords do not match!");
        		}
        	} else {
    	        pModel.addAttribute("info", "Old password was not correct!");
        	}
        } else {
        
	        if (pFees != null) {
	        	user.setFees(Utils.parseDecimal(pFees));
	        }
	        if (pApikey != null) {
	        	user.setApikey(pApikey);
	        }
	        if (pSecret != null) {
	        	user.setSecret(pSecret);
	        }
	        userRepo.save(user);
	        
	        pModel.addAttribute("info", "Profile data successfully saved.");
        }
        return getProfile(pModel);
	}
}
