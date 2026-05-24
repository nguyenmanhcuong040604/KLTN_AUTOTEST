package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.suntower.pages.core.AdminShellPage;

public class AdminDashboardPage extends AdminShellPage {
  public AdminDashboardPage(WebDriver driver) {
    super(driver);
    this.path = "/admin/dashboard";
  }

  public void waitForLoaded() {
    waitForUrlContains("/admin/dashboard");
    waitForVisible(css("#totalBuildingsStat"));
  }

  public void waitForOverviewVisible() {
    waitForVisible(css("#totalBuildingsStat"));
    waitForVisible(css("#totalCustomersStat"));
    waitForVisible(css("#totalStaffsStat"));
    waitForVisible(css("#totalContractsStat"));
    waitForVisible(css("#revenueChartCombined"));
    waitForVisible(css("#contractsByBuildingChart"));
    waitForVisible(css("#districtChart"));
    waitForVisible(css("#saleRateChart"));
    waitForVisible(css("#potentialCustomersBody"));
    waitForVisible(css("#topStaffsBody"));
    waitForVisible(css("#recentBuildingsContainer"));
  }

  public void openBuildingsFromStatCard() {
    clickStatCard("totalBuildingsStat");
    waitForUrlContains("/admin/building/list");
  }

  public void openCustomersFromStatCard() {
    clickStatCard("totalCustomersStat");
    waitForUrlContains("/admin/customer/list");
  }

  public void openStaffsFromStatCard() {
    clickStatCard("totalStaffsStat");
    waitForUrlContains("/admin/staff/list");
  }

  public void openContractsFromStatCard() {
    clickStatCard("totalContractsStat");
    waitForUrlContains("/admin/contract/list");
  }

  public void waitForRecentBuildingVisible(String buildingName) {
    waitForText(css("#recentBuildingsContainer"), buildingName);
  }

  public void openRecentBuilding(String buildingName) {
    By item =
        By.xpath(
            "//*[@id='recentBuildingsContainer']//*[contains(@class,'recent-item') and contains(.,'"
                + buildingName
                + "')]");
    click(item);
  }

  private By statCardByStatId(String statId) {
    return By.xpath("//*[@id='" + statId + "']/ancestor::div[contains(@class,'stat-card')]");
  }

  private void clickStatCard(String statId) {
    WebElement statCard = visible(statCardByStatId(statId));
    scrollIntoView(statCard);
    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", statCard);
  }
}
