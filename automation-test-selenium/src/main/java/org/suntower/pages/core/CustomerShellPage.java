package org.suntower.pages.core;

import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class CustomerShellPage extends BasePage {
  protected String path;

  public CustomerShellPage(WebDriver driver) {
    super(driver);
  }

  public void open() {
    if (path == null || path.isBlank()) {
      throw new IllegalStateException("This customer page does not define a path.");
    }
    visit(path);
  }

  public void openHome() {
    visit("/customer/home");
  }

  public void goToContracts() {
    visit("/customer/contract/list");
  }

  public void goToInvoices() {
    visit("/customer/invoice/list");
  }

  public void goToBuildings() {
    visit("/customer/building/list");
  }

  public void goToTransactions() {
    visit("/customer/transaction/history");
  }

  public void goToServices() {
    visit("/customer/service");
  }

  public void goToProfile() {
    visit("/customer/profile");
  }
}
