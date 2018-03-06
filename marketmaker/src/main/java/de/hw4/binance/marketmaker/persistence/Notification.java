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
	
	@Column
	String title;
	
	@Column
	String message;
	
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

}
