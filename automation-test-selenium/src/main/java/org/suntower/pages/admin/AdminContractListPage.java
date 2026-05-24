package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.suntower.pages.components.TableComponent;
import org.suntower.pages.core.CrudListPage;

public class AdminContractListPage extends CrudListPage {
  private final By tableBody = css("#contractTableBody");
  private final TableComponent table;

  public AdminContractListPage(WebDriver driver) {
    super(driver);
    this.path = "/admin/contract/list";
    this.table = new TableComponent(driver, "#contractTableBody");
  }

  public void waitForLoaded() {
    waitForUrlMatches(".*/admin/contract/(list|search).*");
    waitForVisible(tableBody);
  }

  public void waitForTableData() {
    table.waitForDataOrEmpty();
  }

  public WebElement rowByContractText(String text) {
    return table.rowByText(text);
  }

  public void openDetail(String text) {
    String href = rowByContractText(text).findElement(By.cssSelector("a[href*='/admin/contract/']")).getAttribute("href");
    visit(href);
  }

  public void filterByCustomer(long customerId) {
    setFormControlValue("customerId", String.valueOf(customerId));
  }

  public void filterByBuilding(long buildingId) {
    setFormControlValue("buildingId", String.valueOf(buildingId));
  }

  @Override
  public void search() {
    wait.until(ExpectedConditions.presenceOfElementLocated(css("#contractFilterForm")));
    ((JavascriptExecutor) driver).executeScript("document.querySelector('#contractFilterForm').requestSubmit();");
    waitForTableData();
  }

  private void setFormControlValue(String fieldName, String value) {
    wait.until(ExpectedConditions.presenceOfElementLocated(By.name(fieldName)));
    ((JavascriptExecutor) driver)
        .executeScript(
            "const el = document.querySelector('[name=\"' + arguments[0] + '\"]');"
                + "el.value = arguments[1];"
                + "el.dispatchEvent(new Event('input', { bubbles: true }));"
                + "el.dispatchEvent(new Event('change', { bubbles: true }));",
            fieldName,
            value);
  }
}
