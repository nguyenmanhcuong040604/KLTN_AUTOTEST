package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class AdminPropertyRequestListPage extends BasePage {
  private final By tableBody = css("#requestTableBody");

  public AdminPropertyRequestListPage(WebDriver driver) {
    super(driver);
  }

  public void waitForLoaded() {
    waitForUrlContains("/admin/property-request/list");
    waitForVisible(tableBody);
  }

  public void filterByStatus(String status) {
    selectByValue(css("#statusFilter"), status);
    waitForTableData();
  }

  public void waitForTableData() {
    waitForCondition(() -> count(css("#requestTableBody tr")) > 0, "Property request table did not load.");
  }

  public void waitForRowVisible(Long requestId) {
    waitForCondition(() -> isVisible(rowByRequestId(requestId)), "Property request row was not visible: " + requestId);
  }

  public void openDetail(Long requestId) {
    click(By.cssSelector("a[href='/admin/property-request/" + requestId + "']"));
  }

  private By rowByRequestId(Long requestId) {
    return By.xpath("//tbody[@id='requestTableBody']//tr[.//td[contains(normalize-space(.), '#" + requestId + "')]]");
  }
}
