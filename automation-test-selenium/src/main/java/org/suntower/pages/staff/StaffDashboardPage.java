package org.suntower.pages.staff;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class StaffDashboardPage extends BasePage {
  private final By buildingCount = css("#buildingCntStat");
  private final By contractCount = css("#contractCntStat");
  private final By customerCount = css("#customerCntStat");
  private final By unpaidInvoiceCount = css("#unpaidInvoiceCntStat");
  private final By overdueInvoicesBody = css("#overdueInvoicesBody");
  private final By expiringContractsBody = css("#expiringContractsBody");
  private final By expiringInvoicesBody = css("#expiringInvoicesBody");

  public StaffDashboardPage(WebDriver driver) {
    super(driver);
  }

  public void waitForLoaded() {
    waitForUrlContains("/staff/dashboard");
    waitForVisible(buildingCount);
  }

  public void waitForSummarySectionsVisible() {
    waitForVisible(buildingCount);
    waitForVisible(contractCount);
    waitForVisible(customerCount);
    waitForVisible(unpaidInvoiceCount);
    waitForVisible(overdueInvoicesBody);
    waitForVisible(expiringContractsBody);
    waitForVisible(expiringInvoicesBody);
  }
}
