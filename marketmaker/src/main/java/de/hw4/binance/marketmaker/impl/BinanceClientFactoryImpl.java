package de.hw4.binance.marketmaker.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.general.ExchangeInfo;

import de.hw4.binance.marketmaker.BinanceClientFactory;
import de.hw4.binance.marketmaker.persistence.User;
import de.hw4.binance.marketmaker.persistence.UserRepository;

@Component
public class BinanceClientFactoryImpl implements BinanceClientFactory {

	@Autowired
	UserRepository userRepo;
	
	private Map<String, BinanceApiRestClient> binanceClients;

	private static ExchangeInfo exchangeInfo;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public BinanceClientFactoryImpl() {
		binanceClients = new HashMap<>();
	}

	public BinanceApiRestClient getClient(String pUsername) {
		BinanceApiRestClient client = binanceClients.get(pUsername);
		if (client == null) {
			
			User user = userRepo.findOne(pUsername);
			String apiKey = user.getApikey().trim();
			String secret = user.getSecret().trim();
			if (apiKey == null || secret == null) {
				try {
				    Properties configProperties = new Properties();
				    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("api-keys/" + pUsername + ".properties");
				    configProperties.load(inputStream);
				    apiKey = configProperties.getProperty("apiKey");
				    secret = configProperties.getProperty("secret");
				} catch(Exception e){
					logger.warn("Could not create binance client for user {}. Check api-keys/{}.properties!", pUsername, pUsername);
				}
			}
			if (apiKey != null && secret != null) {
				try {
					client = BinanceApiClientFactory.newInstance(apiKey, secret).newRestClient();
					binanceClients.put(pUsername, client);
				} catch(Exception e){
				    logger.warn("Could not create binance client for user {}. : {}", pUsername, e.getMessage());
				}
			}
		}
		return client;
	}
	
	static ThreadLocal<BinanceApiRestClient> anonClients = new ThreadLocal<>(); 

	public BinanceApiRestClient getClient() {
		BinanceApiRestClient client = anonClients.get();
		if (client == null) {
			client = BinanceApiClientFactory.newInstance().newRestClient();
		}
		return client;
	}
	
	@Override
	public ExchangeInfo getExchangeInfo() {
		if (exchangeInfo != null) {
			return exchangeInfo;
		}
		if (binanceClients.isEmpty()) {
			// can only be retrieved after a client was created.
			return null;
		}
		BinanceApiRestClient client = binanceClients.entrySet().iterator().next().getValue();
		exchangeInfo = client.getExchangeInfo();
		return exchangeInfo;
	}

	@Override
	public void destroyClient(String pUsername) {
		binanceClients.remove(pUsername);
	}
}
