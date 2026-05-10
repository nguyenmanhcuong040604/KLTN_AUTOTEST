package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.suntower.pages.components.TableComponent;
import org.suntower.pages.core.CrudListPage;

public class AdminBuildingListPage extends CrudListPage {
  private final By addButton = css(".btn-add");
  private final By buildingTableBody = css("#buildingTableBody");
  private final TableComponent table;

  public AdminBuildingListPage(WebDriver driver) {
    super(driver);
    this.path = "/admin/building/list";
    this.table = new TableComponent(driver, "#buildingTableBody");
  }

  public void waitForLoaded() {
    waitForUrlMatches(".*/admin/building/(list|search).*");
    waitForVisible(buildingTableBody);
  }

  public void waitForTableData() {
    table.waitForDataOrEmpty();
  }

  public void openAddForm() {
    click(addButton);
  }

  public void filterByName(String name) {
    fillFilter("name", name);
  }

  public void filterByPropertyType(String propertyType) {
    selectFilter("propertyType", propertyType);
  }

  public void filterByTransactionType(String transactionType) {
    selectFilter("transactionType", transactionType);
  }

  public void filterByDistrict(String districtId) {
    selectFilter("districtId", districtId);
  }

  public WebElement rowByBuildingName(String name) {
    return table.rowByText(name);
  }

  public void openDetail(String name) {
    clickRowLink(name, "/admin/building/");
  }

  public void openEdit(String name) {
    clickRowLink(name, "/admin/building/edit/");
  }

  public void openAdditionalInformation(String name) {
    clickRowLink(name, "/admin/building-additional-information/");
  }

  public void deleteBuilding(String name) {
    rowByBuildingName(name).findElement(actionButton("delete")).click();
  }

  public void waitForSweetAlertContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }

  public void confirmSweetAlert() {
    dismissSweetAlertIfPresent();
  }
}
