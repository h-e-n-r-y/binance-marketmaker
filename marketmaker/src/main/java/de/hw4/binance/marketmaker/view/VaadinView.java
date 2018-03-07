package de.hw4.binance.marketmaker.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.request.AllOrdersRequest;
import com.binance.api.client.domain.market.TickerPrice;
import com.binance.api.client.exception.BinanceApiException;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.event.UIEvents;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import de.hw4.binance.marketmaker.BinanceClientComponent;
import de.hw4.binance.marketmaker.impl.AssetBalanceImpl;

@SpringUI(path = "/nice")
@Theme(ValoTheme.THEME_NAME)
@Title("Market Maker")
public class VaadinView extends UI {

  @Autowired
  BinanceClientComponent clientFactory;

  private VerticalLayout base = new VerticalLayout();
  private VerticalLayout home = new VerticalLayout();
  private VerticalLayout balances = new VerticalLayout();
  private VerticalLayout orders = new VerticalLayout();

  private TabSheet tabSheet = new TabSheet();
  private Label headline;

  @Override
  protected void init(VaadinRequest request) {

    setContent(base);
    setPollInterval(1500);

    base.setSpacing(true);
    base.setMargin(true);
    base.addComponent(tabSheet);

    tabSheet.addTab(home, "Home");
    tabSheet.addTab(balances, "Balances");
    tabSheet.addTab(orders, "Orders");

    initBalancesView();

    initHomeView();
  }

  private void initHomeView() {

    Grid<Order> allOrdersGrid = new Grid<>();
    allOrdersGrid
        .addColumn(Order::getTime, order -> new SimpleDateFormat("HH:mm - dd.MM.yyyy")
            .format(new Date(order))).setCaption("Time");
    allOrdersGrid.addColumn(Order::getSide).setCaption("Type");
    allOrdersGrid.addColumn(Order::getOrigQty).setCaption("Amount");
    allOrdersGrid.addColumn(Order::getPrice).setCaption("Price");
    allOrdersGrid.addColumn(Order::getExecutedQty).setCaption("Exec Qty");
    allOrdersGrid.addColumn(Order::getStatus).setCaption("Status");
    allOrdersGrid.setSizeFull();
    allOrdersGrid.sort("Time", SortDirection.DESCENDING);

    final String[] symbol = { "ETHBTC" };
    ListDataProvider<Order> allOrdersProvider = DataProvider.ofCollection(getAllOrdersForSymbol(symbol[0]));
    allOrdersGrid.setDataProvider(allOrdersProvider);

    TickerPrice tickerPrice = getTickerPrice(symbol[0]);
    final Double[] formerPrice = { Double.valueOf(tickerPrice.getPrice()) };
    headline = new Label();
    headline.setCaption("<h2>Tickerprice " + tickerPrice.getSymbol() + ": " + tickerPrice.getPrice() + "</h2>");
    headline.setCaptionAsHtml(true);

    addPollListener((UIEvents.PollListener) event -> {
      updateTickerPrice(symbol, formerPrice);
    });

    ComboBox<String> assets = new ComboBox<>("Choose Asset");
    List<String> allAssets = getAllAssets();
    assets.setItems(allAssets);
    assets.setSelectedItem(allAssets.get(0));

    ComboBox<String> currencies = new ComboBox<>("Choose Currency");
    currencies.setItems("ETH", "BTC");
    currencies.setSelectedItem("ETH");

    Button update = new Button("Update");
    update.addClickListener(
        (Button.ClickListener) event -> {

          String selectedSymbol = assets.getValue() + currencies.getValue();
          allOrdersGrid.setDataProvider(DataProvider.ofCollection(getAllOrdersForSymbol(selectedSymbol)));
          symbol[0] = selectedSymbol;
          updateTickerPrice(symbol, formerPrice);
        }
    );
    HorizontalLayout selectionGrid = new HorizontalLayout();
    selectionGrid.addComponents(assets, currencies, update);
    home.addComponents(headline, selectionGrid, allOrdersGrid);
  }

  private void updateTickerPrice(String[] symbol, Double[] formerPrice) {
    TickerPrice tickerPrice1 = getTickerPrice(symbol[0]);
    Double currentPrice = Double.valueOf(tickerPrice1.getPrice());
    boolean isHigher = currentPrice > formerPrice[0];
    home.removeComponent(headline);

    String priceWithColor = tickerPrice1.getPrice();
    if (isHigher) {
      priceWithColor = "<span style='color:green;font-weight:bold'>" + priceWithColor + "</span>";
    } else {
      priceWithColor = "<span style='color:red;font-weight:bold'>" + priceWithColor + "</span>";
    }

    headline = new Label();
    headline.setCaption("<h2>Tickerprice " + tickerPrice1.getSymbol() + ": " + priceWithColor + "</h2>");
    headline.setCaptionAsHtml(true);
    home.addComponent(headline, 0);

    formerPrice[0] = currentPrice;
  }

  private void initBalancesView() {

    Grid<AssetBalanceImpl> grid = new Grid<>();
    grid.addColumn(AssetBalanceImpl::getAsset).setCaption("Currency");
    grid.addColumn(AssetBalanceImpl::getFree).setCaption("Free");
    grid.addColumn(AssetBalanceImpl::getLocked).setCaption("Locked");
    grid.addColumn(AssetBalanceImpl::getValue).setCaption("Value in BTC");
    grid.setSizeFull();

    grid.setDataProvider(DataProvider.ofCollection(getBalances()));

    this.balances.addComponent(grid);
  }

  private TickerPrice getTickerPrice(String symbol) {
    try {
      return getBinanceClient().getPrice(symbol);
    } catch (BinanceApiException e) {
      Notification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
      TickerPrice price = new TickerPrice();
      price.setPrice("0");
      price.setSymbol("INVALID SYMBOL");
      return price;
    }
  }

  private List<Order> getAllOrdersForSymbol(String symbol) {
    try {
      return getBinanceClient().getAllOrders(new AllOrdersRequest(symbol));
    } catch (BinanceApiException e) {
      Notification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
      return Collections.emptyList();
    }
  }

  private List<String> getAllAssets() {
    List<String> assetBalances = new ArrayList<>();
    for (AssetBalance balance : getBinanceClient().getAccount().getBalances()) {
      assetBalances.add(balance.getAsset());
    }
    return assetBalances;
  }

  private List<AssetBalanceImpl> getBalances() {

    BinanceApiRestClient binanceClient = getBinanceClient();
    List<AssetBalanceImpl> displayBalances = new ArrayList<>();

    for (AssetBalance bal : binanceClient.getAccount().getBalances()) {
      String free = bal.getFree();
      String locked = bal.getLocked();
      if (free.equals("0.00000000") && locked.equals("0.00000000")) {
        continue;
      }
      String asset = bal.getAsset();
      TickerPrice tickerPrice = null;
      try {
        tickerPrice = binanceClient.getPrice(asset + "BTC");
      } catch (BinanceApiException bae) {
        //
      }
      AssetBalanceImpl assetBalance = new AssetBalanceImpl(bal, tickerPrice);
      displayBalances.add(assetBalance);
    }
    return displayBalances;
  }

  private BinanceApiRestClient getBinanceClient() {
    String currentPrincipalName = SecurityContextHolder.getContext().getAuthentication().getName();
    return clientFactory.getClient(currentPrincipalName);
  }

}
