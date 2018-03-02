# binance-marketmaker
Simple Trading Bot (still pre-alpha!). Connects to your binance account and tries to trade between 2 coins only accepting orders that will generate win. Needs https://github.com/binance-exchange/binance-java-api

Installation
============

## Clone and build binance-exchange/binance-java-api:

	cd ~/git
	git clone https://github.com/binance-exchange/binance-java-api
	cd binance-java-api
	mvn install
	
## Build binance-marketmaker:
	cd ~/git
	git clone https://github.com/h-e-n-r-y/binance-marketmaker.git
	cd binance-java-api
	mvn install

## Install your binance-api-key

Create your API-Key here: https://www.binance.com/userCenter/createApi.html
Rename src/main/resources/api-keys/username.properties. (Choose the username you will use)
Fill in api-key and secret.

## Start
	mvn spring-boot:run
	
Access your Marketmaker at http://localhost:8080/

First you must register a user with the same username, under which you installed your api-key.
