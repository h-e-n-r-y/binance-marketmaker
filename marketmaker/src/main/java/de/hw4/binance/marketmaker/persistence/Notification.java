package de.hw4.binance.marketmaker.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Notification {
	
	@Id
	Long timestamp;
	
	@Column
	String user;
	
	@Column(length = 64)
	String title;
	
	@Column(length = 128)
	String message;
	
	@Column(length = 5)
	String symbol;
	
	public Notification() {
	}
	
	public Notification(String pUser, String pTitle, String pMessage) {
		timestamp = System.currentTimeMillis();
		user = pUser;
		title = pTitle;
		message = pMessage;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getMessage() {
		return message;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

}
