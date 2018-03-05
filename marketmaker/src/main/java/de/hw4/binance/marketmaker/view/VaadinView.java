package de.hw4.binance.marketmaker.view;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import de.hw4.binance.marketmaker.BalancesController;
import de.hw4.binance.marketmaker.impl.AssetBalanceImpl;

@Push(transport = Transport.WEBSOCKET_XHR)
@SpringUI(path = "/nice")
@Theme(ValoTheme.THEME_NAME)
@Title("Market Maker")
public class VaadinView extends UI {

  @Autowired
  BalancesController balancesController;

  public VerticalLayout base = new VerticalLayout();
  public VerticalLayout home = new VerticalLayout();
  public VerticalLayout balances = new VerticalLayout();
  public VerticalLayout orders = new VerticalLayout();

  public TabSheet tabSheet = new TabSheet();

  @Override
  protected void init(VaadinRequest request) {

    setContent(base);

    base.setSpacing(true);
    base.setMargin(true);
    base.addComponent(tabSheet);

    tabSheet.addTab(home, "Home");

    tabSheet.addTab(balances, "Balances");

    tabSheet.addTab(orders, "Orders");

    Grid<AssetBalanceImpl> grid = new Grid();
    grid.setItems(balancesController.balances());
    grid.addColumn(AssetBalanceImpl::getAsset).setCaption("Currency");
    grid.addColumn(AssetBalanceImpl::getFree).setCaption("Free");
    grid.addColumn(AssetBalanceImpl::getLocked).setCaption("Locked");
    grid.addColumn(AssetBalanceImpl::getValue).setCaption("Value in BTC");
    this.balances.addComponent(grid);

  }
}
