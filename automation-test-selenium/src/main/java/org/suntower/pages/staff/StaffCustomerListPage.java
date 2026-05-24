package org.suntower.pages.staff;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.suntower.pages.components.TableComponent;
import org.suntower.pages.core.CrudListPage;

public class StaffCustomerListPage extends CrudListPage {
  private final By tableBody = css("#customerTableBody");
  private final By detailModal = css(".modal.show");
  private final TableComponent table;

  public StaffCustomerListPage(WebDriver driver) {
    super(driver);
    this.path = "/staff/customers";
    this.table = new TableComponent(driver, "#customerTableBody");
  }

  public void waitForLoaded() {
    waitForUrlContains("/staff/customers");
    waitForVisible(tableBody);
  }

  public void waitForTableData() {
    table.waitForDataOrEmpty();
  }

  public void filterByFullName(String fullName) {
    fill(inputByName("fullName"), fullName);
  }

  public void submitFilters() {
    wait.until(ExpectedConditions.presenceOfElementLocated(css("#filterForm")));
    ((JavascriptExecutor) driver).executeScript("document.querySelector('#filterForm').requestSubmit();");
    waitForTableData();
  }

  public WebElement rowByCustomerName(String name) {
    return table.rowByText(name);
  }

  public void openCustomerDetail(String name) {
    waitForCondition(
        () -> {
          try {
            click(rowByCustomerName(name).findElement(By.cssSelector(".btn-view")));
            return true;
          } catch (RuntimeException ignored) {
            return false;
          }
        },
        "Could not open customer detail for: " + name);
  }

  public void waitForDetailModalContains(String name) {
    waitForVisible(detailModal);
    waitForText(detailModal, name);
  }
}
