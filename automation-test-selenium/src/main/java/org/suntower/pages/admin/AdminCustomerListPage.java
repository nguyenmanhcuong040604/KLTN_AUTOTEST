package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.suntower.pages.components.TableComponent;
import org.suntower.pages.core.CrudListPage;

public class AdminCustomerListPage extends CrudListPage {
  private final By addButton = css(".btn-add");
  private final By customerTableBody = css("#customerTableBody");
  private final TableComponent table;

  public AdminCustomerListPage(WebDriver driver) {
    super(driver);
    this.path = "/admin/customer/list";
    this.table = new TableComponent(driver, "#customerTableBody");
  }

  public void waitForLoaded() {
    waitForUrlMatches(".*/admin/customer/(list|search).*");
    waitForVisible(customerTableBody);
  }

  public void waitForTableData() {
    table.waitForDataOrEmpty();
  }

  public void openAddForm() {
    click(addButton);
  }

  public WebElement rowByCustomerName(String name) {
    return table.rowByText(name);
  }

  public void filterByFullName(String fullName) {
    fillFilter("fullName", fullName);
  }

  public void openDetail(String customerText) {
    clickRowLink(customerText, "/admin/customer/");
  }

  public void deleteCustomer(String customerText) {
    rowByCustomerName(customerText).findElement(actionButton("delete")).click();
  }

  public void waitForSweetAlertContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }

  public void confirmSweetAlert() {
    dismissSweetAlertIfPresent();
  }
}
