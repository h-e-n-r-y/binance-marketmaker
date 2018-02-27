package de.hw4.binance.marketmaker.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;

import de.hw4.binance.marketmaker.BinanceClientComponent;

@Component
public class BinanceClientComponentImpl implements BinanceClientComponent {

	private Map<String, BinanceApiRestClient> binanceClients;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public BinanceClientComponentImpl() {
		binanceClients = new HashMap<>();
	}

	public BinanceApiRestClient getClient(String pUsername) {
		BinanceApiRestClient client = binanceClients.get(pUsername);
		if (client == null) {
			try {
			    Properties configProperties = new Properties();
			    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("api-keys/" + pUsername + ".properties");
			    configProperties.load(inputStream);
			    String apikey = configProperties.getProperty("apikey");
			    String secret = configProperties.getProperty("secret");
			    if (apikey != null && secret != null) {
			    		client = BinanceApiClientFactory.newInstance(apikey, secret).newRestClient();
			    		binanceClients.put(pUsername, client);
			    }
			}
			catch(Exception e){
			    logger.warn("Could not create binance client for user {}. Check api-keys/{}.properties!", pUsername, pUsername);
			}
		}
		return client;
	}
}
