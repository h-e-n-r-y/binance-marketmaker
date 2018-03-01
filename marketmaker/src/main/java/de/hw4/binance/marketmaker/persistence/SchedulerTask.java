package de.hw4.binance.marketmaker.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SchedulerTask {
	
	@Id
	private Long id;

	@Column(length = 30, nullable = false)
	String user;

	@Column(length = 8, nullable = false)
	String marketSymbol;
	
	@Column
	Boolean active;
	
	public SchedulerTask() {
		
	}

	public SchedulerTask(String pUserName, String pSymbol) {
		user = pUserName;
		marketSymbol = pSymbol;
		id = hash(pUserName, pSymbol);
	}
	
	public void activate() {
		active = true;
	}

	public void deactivate() {
		active = false;
	}
	
	public String getUser() {
		return user;
	}
	
	public boolean getActive() {
		return active != null ? active : false;
	}

	public static long hash(String userName, String symbol) {
		return Long.valueOf(userName.hashCode()) * 31L + symbol.hashCode();
	}
	
	public String getSymbol() {
		return marketSymbol;
	}

}
