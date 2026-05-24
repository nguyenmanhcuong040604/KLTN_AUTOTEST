package org.suntower.pages.customer;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class CustomerHomePage extends BasePage {
  private final By welcomeSection = css(".welcome-section");
  private final By contractsContainer = css("#contractsContainer");
  private final By pendingInvoiceContainer = css("#pendingInvoiceContainer");

  public CustomerHomePage(WebDriver driver) {
    super(driver);
  }

  public void waitForLoaded() {
    waitForUrlContains("/customer/home");
    waitForVisible(welcomeSection);
  }

  public void waitForDashboardSectionsVisible() {
    waitForVisible(contractsContainer);
    waitForVisible(pendingInvoiceContainer);
  }

  public void openContracts() {
    click(firstVisible(css(".view-all[href='/customer/contract/list'], .nav-link[href='/customer/contract/list']")));
  }

  public void openBuildings() {
    click(firstVisible(css(".nav-link[href='/customer/building/list']")));
  }

  public void waitForContractsRoute() {
    waitForUrlMatches(".*/customer/(contract/list|contracts).*");
  }

  public void waitForBuildingsRoute() {
    waitForUrlContains("/customer/building/list");
  }
}
