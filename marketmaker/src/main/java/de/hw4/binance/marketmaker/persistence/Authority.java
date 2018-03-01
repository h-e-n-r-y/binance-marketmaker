package de.hw4.binance.marketmaker.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Authority {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	Long id;
	
	@Column
	String username;
	
	@Column
	String authority;
	
	public Authority(){
	}
	
	public Authority(String pUsername, String pAuth){
		username = pUsername;
		authority = pAuth;
	}
	
}
