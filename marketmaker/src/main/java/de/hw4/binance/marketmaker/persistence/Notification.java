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
}
