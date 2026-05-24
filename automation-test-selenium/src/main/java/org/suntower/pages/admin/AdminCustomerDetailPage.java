package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.suntower.pages.core.CrudDetailPage;

public class AdminCustomerDetailPage extends CrudDetailPage {
  private final By customerStrip = css(".strip-id");

  public AdminCustomerDetailPage(WebDriver driver) {
    super(driver);
    this.detailPath = "/admin/customer";
  }

  public void waitForLoaded(int customerId) {
    waitForUrlMatches(".*/admin/customer/" + customerId + "$");
    waitForVisible(customerStrip);
  }
}
