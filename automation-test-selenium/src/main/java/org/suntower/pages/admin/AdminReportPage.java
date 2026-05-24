package org.suntower.pages.admin;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.suntower.core.BasePage;

public class AdminReportPage extends BasePage {
  private final By yearSelect = css(".year-select, select[name='year']");

  public AdminReportPage(WebDriver driver) {
    super(driver);
  }

  public void open() {
    visit("/admin/report");
  }

  public void waitForLoaded() {
    waitForUrlContains("/admin/report");
    waitForVisible(yearSelect);
    waitForVisible(css("#monthlyRevenueChart"));
  }

  public void waitForOverviewVisible() {
    waitForCondition(() -> driver.findElements(css(".stat-card")).size() >= 3, "Report stat cards did not load.");
    waitForVisible(css("#monthlyRevenueChart"));
    waitForVisible(css("#rentGrowthChart"));
    waitForVisible(css("#yearlyRevenueChart"));
    waitForVisible(css("#propertyTypeChart"));
    waitForVisible(css("#topBuildingChart"));
  }

  public List<String> availableYears() {
    return visible(yearSelect).findElements(By.cssSelector("option")).stream().map(option -> option.getAttribute("value")).toList();
  }

  public void selectYear(String year) {
    setInputValue(yearSelect, year);
  }

  public void waitForYearSelected(String year) {
    waitForValue(yearSelect, year);
  }

  public void triggerPrint() {
    ((JavascriptExecutor) driver).executeScript("window.__printTriggered = false; window.print = function(){ window.__printTriggered = true; };");
    click(firstVisible(css("button[onclick*='window.print']")));
  }

  public boolean wasPrintTriggered() {
    return Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript("return window.__printTriggered === true;"));
  }
}
