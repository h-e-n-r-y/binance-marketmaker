# binance-marketmaker
Simple Trading Bot (still pre-alpha!). Connects to your binance account and tries to trade between 2 coins only accepting orders that will generate win. Needs https://github.com/binance-exchange/binance-java-api

Installation
============

## Clone and build binance-exchange/binance-java-api:

I have forked the original api to be able to implement minor extensions easily. https://github.com/h-e-n-r-y/binance-java-api branch henry-master.

	cd ~/git
	git clone https://github.com/h-e-n-r-y/binance-java-api
	cd binance-java-api
	mvn install
	
## Build binance-marketmaker:
	cd ~/git
	git clone https://github.com/h-e-n-r-y/binance-marketmaker.git
	cd binance-marketmaker/marketmaker
	mvn install

## Install your binance-api-key

Create your API-Key here: https://www.binance.com/userCenter/createApi.html
Rename src/main/resources/api-keys/username.properties. (Choose the username you will use)
Fill in api-key and secret.

## Start
	mvn spring-boot:run
	
Access your Marketmaker at http://localhost:8080/

First you must register a user with the same username, under which you installed your api-key.

## Disclaimer

The app has lots of bugs and it's state is far from being ready! 
These bugs may lead to major loss!
Be careful to understand the sourcecode before using it for dealing with high amounts of coins!

I appreciate comments, bugreports and suggestions for further development...

## Thanks

Thanks, Christopher Downer, for sharing the coin icons. Check out his project here: https://github.com/cjdowner/cryptocurrency-icons

Henry
