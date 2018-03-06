package de.hw4.binance.marketmaker;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.market.TickerPrice;
import com.binance.api.client.exception.BinanceApiException;

import de.hw4.binance.marketmaker.impl.AssetBalanceImpl;

@Controller
public class BalancesController {

  @Autowired
  BinanceClientComponent clientFactory;

  static Logger logger = LoggerFactory.getLogger(BalancesController.class);

  @RequestMapping(value = "/balances", method = RequestMethod.GET)
  public String balances(
      @RequestParam(value = "basecoin", required = false, defaultValue = "BTC")
          String pSymbol2, Model model) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentPrincipalName = authentication.getName();

    BinanceApiRestClient binanceClient = clientFactory.getClient(currentPrincipalName);

    if (binanceClient == null) {
      model.addAttribute("errormsg", "No valid api key found for user '" + currentPrincipalName + "'");
      model.addAttribute("errormsg2", "Please contact the administrator!");
      return "error";
    }

    List<AssetBalance> balances = binanceClient.getAccount().getBalances();
    List<AssetBalanceImpl> displayBalances = new ArrayList<>();
    for (AssetBalance bal : balances) {
      String free = bal.getFree();
      String locked = bal.getLocked();
      if (free.equals("0.00000000") && locked.equals("0.00000000")) {
        continue;
      }
      String asset = bal.getAsset();
      TickerPrice tickerPrice = null;
      try {
        tickerPrice = binanceClient.getPrice(asset + pSymbol2);
      } catch (BinanceApiException bae) {
        //
      }
      AssetBalanceImpl assetBalance = new AssetBalanceImpl(bal, tickerPrice);
      displayBalances.add(assetBalance);
    }
    model.addAttribute("symbol2", pSymbol2);
    model.addAttribute("balances", displayBalances);

    return "balances";

  }
}