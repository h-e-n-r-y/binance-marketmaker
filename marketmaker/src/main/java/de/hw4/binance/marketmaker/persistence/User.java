package de.hw4.binance.marketmaker.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
public class User {
	@Id
	String username;
	
	@Column
	String password;
	
	@Column
	Boolean enabled;
	
	public User() {
	}
	
	public User(String pUsername) {
		username = pUsername;
	}
	
	public void setPassword(String pPassword) {
		password = new BCryptPasswordEncoder().encode(pPassword);
	}
	public void setEnabled(boolean pEnabled) {
		enabled = pEnabled;
	}
}
