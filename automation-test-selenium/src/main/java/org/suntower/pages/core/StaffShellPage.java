package org.suntower.pages.core;

import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class StaffShellPage extends BasePage {
  protected String path;

  public StaffShellPage(WebDriver driver) {
    super(driver);
  }

  public void open() {
    if (path == null || path.isBlank()) {
      throw new IllegalStateException("This staff page does not define a path.");
    }
    visit(path);
  }

  public void openDashboard() {
    visit("/staff/dashboard");
  }

  public void goToBuildings() {
    visit("/staff/buildings");
  }

  public void goToCustomers() {
    visit("/staff/customers");
  }

  public void goToContracts() {
    visit("/staff/contracts");
  }

  public void goToInvoices() {
    visit("/staff/invoices");
  }

  public void goToProfile() {
    visit("/staff/profile");
  }
}
