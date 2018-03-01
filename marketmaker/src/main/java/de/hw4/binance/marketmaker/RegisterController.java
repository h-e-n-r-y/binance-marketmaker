package de.hw4.binance.marketmaker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.hw4.binance.marketmaker.persistence.Authority;
import de.hw4.binance.marketmaker.persistence.AuthorityRepository;
import de.hw4.binance.marketmaker.persistence.User;
import de.hw4.binance.marketmaker.persistence.UserRepository;

@Controller
public class RegisterController {
	
	@Autowired
	UserRepository userRepo;

	@Autowired
	AuthorityRepository authorityRepo;

	@RequestMapping(value = "/register", method = RequestMethod.POST)
    public String createAccount(
    		@RequestParam(value="username", required=true) String pUsername,
    		@RequestParam(value="pwd", required=true) String pPassword, 
    		@RequestParam(value="pwd2", required=true) String pPwd2,
    		Model model) {
		
		model.addAttribute("username", pUsername);
		if (!pPassword.equals(pPwd2)) {
			model.addAttribute("error", "2 Passwords must be equal!");
			return "register";
		}
		
		if (null != userRepo.findOne(pUsername)) {
			model.addAttribute("error", "Username already registered!");
			return "register";
		}
		User newUser = new User(pUsername);
		newUser.setPassword(pPassword);
		newUser.setEnabled(true);
		userRepo.save(newUser);
		
		Authority auth = new Authority(pUsername, "USER");
		authorityRepo.save(auth);
		
		
		return "registered";
	}
	
	@RequestMapping(value = "/register", method = RequestMethod.GET)
    public String showRegisterForm() {
		return "register";
	}

}
