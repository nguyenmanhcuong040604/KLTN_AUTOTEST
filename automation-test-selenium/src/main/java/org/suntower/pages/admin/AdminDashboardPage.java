package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
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
    click(statCardByStatId("totalBuildingsStat"));
  }

  public void openCustomersFromStatCard() {
    click(statCardByStatId("totalCustomersStat"));
  }

  public void openStaffsFromStatCard() {
    click(statCardByStatId("totalStaffsStat"));
  }

  public void openContractsFromStatCard() {
    click(statCardByStatId("totalContractsStat"));
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
}
