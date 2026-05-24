package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.suntower.pages.core.CrudDetailPage;

public class AdminBuildingDetailPage extends CrudDetailPage {
  private final By deleteButton = css(".btn-hero-delete");

  public AdminBuildingDetailPage(WebDriver driver) {
    super(driver);
    this.detailPath = "/admin/building";
  }

  public void waitForLoaded(long buildingId) {
    waitForUrlMatches(".*/admin/building/" + buildingId + "$");
    waitForVisible(pageHeader);
  }

  public void deleteBuilding() {
    click(deleteButton);
  }
}
