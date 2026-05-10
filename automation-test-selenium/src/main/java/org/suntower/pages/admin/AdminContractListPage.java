package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.suntower.pages.components.TableComponent;
import org.suntower.pages.core.CrudListPage;

public class AdminContractListPage extends CrudListPage {
  private final By addButton = css(".btn-add");
  private final By updateStatusesButton = css(".btn-update-status");
  private final By contractTableBody = css("#contractTableBody");
  private final TableComponent table;

  public AdminContractListPage(WebDriver driver) {
    super(driver);
    this.path = "/admin/contract/list";
    this.table = new TableComponent(driver, "#contractTableBody");
  }

  public void waitForLoaded() {
    waitForUrlMatches(".*/admin/contract/(list|search).*");
    waitForVisible(contractTableBody);
  }

  public void waitForTableData() {
    table.waitForDataOrEmpty();
  }

  public void openAddForm() {
    click(addButton);
  }

  public void filterByCustomer(Object customerId) {
    selectFilter("customerId", String.valueOf(customerId));
  }

  public void filterByBuilding(Object buildingId) {
    selectFilter("buildingId", String.valueOf(buildingId));
  }

  public void filterByStaff(Object staffId) {
    selectFilter("staffId", String.valueOf(staffId));
  }

  public void filterByStatus(String status) {
    selectFilter("status", status);
  }

  public void fillRentPriceRange(Number rentPriceFrom, Number rentPriceTo) {
    if (rentPriceFrom != null) fillFilter("rentPriceFrom", String.valueOf(rentPriceFrom));
    if (rentPriceTo != null) fillFilter("rentPriceTo", String.valueOf(rentPriceTo));
  }

  public WebElement rowByContractText(String text) {
    return table.rowByText(text);
  }

  public void submitFilters() {
    search();
  }

  public void openDetail(String text) {
    clickRowLink(text, "/admin/contract/");
  }

  public void openEdit(String text) {
    clickRowLink(text, "/admin/contract/edit/");
  }

  public void deleteContract(String text) {
    rowByContractText(text).findElement(actionButton("delete")).click();
  }

  public void updateStatuses() {
    click(updateStatusesButton);
  }

  public void waitForSweetAlertContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }
}
