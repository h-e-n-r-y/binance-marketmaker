package de.hw4.binance.marketmaker.persistence;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
public class User {
	@Id
	String username;
	
	@Column(length = 128)
	String password;
	
	@Column
	Boolean enabled;
	
	@Column(length = 128)
	String apikey;
	
	@Column(length = 128)
	String secret;
	
	@Column(precision = 5, scale = 4)
	BigDecimal fees;
	
	public User() {
	}
	
	public User(String pUsername) {
		username = pUsername;
	}
	
	public void setPassword(String pPassword) {
		password = new BCryptPasswordEncoder().encode(pPassword);
	}
	public boolean validatePassword(String pPassword) {
		return new BCryptPasswordEncoder().matches(pPassword, password);
	}
	public void setEnabled(boolean pEnabled) {
		enabled = pEnabled;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String pUsername) {
		this.username = pUsername;
	}

	public String getApikey() {
		return apikey;
	}

	public void setApikey(String pApikey) {
		this.apikey = pApikey;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String pSecret) {
		this.secret = pSecret;
	}

	public BigDecimal getFees() {
		return fees;
	}

	public void setFees(BigDecimal pFees) {
		this.fees = pFees;
	}
}
