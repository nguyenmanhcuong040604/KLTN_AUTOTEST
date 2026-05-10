package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.suntower.pages.components.TableComponent;
import org.suntower.pages.core.CrudListPage;

public class AdminStaffListPage extends CrudListPage {
  private final By addButton = css(".btn-hd-add, .btn-add");
  private final By anyStaffTableBody = css("#staffTableBody, #adminTableBody");
  private final TableComponent staffTable;

  public AdminStaffListPage(WebDriver driver) {
    super(driver);
    this.path = "/admin/staff/list";
    this.staffTable = new TableComponent(driver, "#staffTableBody");
  }

  public void waitForLoaded() {
    waitForUrlMatches(".*/admin/staff/(list|search).*");
    waitForVisible(anyStaffTableBody);
  }

  public void openAddForm() {
    click(addButton);
  }

  public WebElement rowByStaffName(String fullName) {
    By rows = css("tbody tr");
    waitForCondition(
        () -> driver.findElements(rows).stream().anyMatch(row -> row.isDisplayed() && row.getText().contains(fullName)),
        "No staff row contained text: " + fullName);
    return driver.findElements(rows).stream().filter(row -> row.isDisplayed() && row.getText().contains(fullName)).findFirst().orElseThrow();
  }

  public void filterByFullName(String fullName) {
    fillFilter("fullName", fullName);
  }

  public void filterByRole(String role) {
    selectFilter("role", role);
  }

  public void openDetail(String text) {
    clickRowLink(text, "/admin/staff/");
  }

  public void deleteStaff(String text) {
    rowByStaffName(text).findElement(actionButton("delete")).click();
  }

  public void waitForSearchTableData() {
    staffTable.waitForDataOrEmpty();
  }

  public void waitForSweetAlertContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }

  public void confirmSweetAlert() {
    dismissSweetAlertIfPresent();
  }
}
