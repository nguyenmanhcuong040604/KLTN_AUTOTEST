package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.suntower.pages.components.TableComponent;
import org.suntower.pages.core.CrudListPage;

public class AdminSaleContractListPage extends CrudListPage {
  private final By tableBody = css("#saleContractTableBody");
  private final TableComponent table;

  public AdminSaleContractListPage(WebDriver driver) {
    super(driver);
    this.path = "/admin/sale-contract/list";
    this.table = new TableComponent(driver, "#saleContractTableBody");
  }

  public void waitForLoaded() {
    waitForUrlMatches(".*/admin/sale-contract/(list|search).*");
    waitForVisible(tableBody);
  }

  public void waitForTableData() {
    table.waitForDataOrEmpty();
  }

  public WebElement rowBySaleContractText(String text) {
    return table.rowByText(text);
  }

  public void openDetail(String text) {
    String href = rowBySaleContractText(text).findElement(By.cssSelector("a[href*='/admin/sale-contract/']")).getAttribute("href");
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
    wait.until(ExpectedConditions.presenceOfElementLocated(css("form")));
    ((JavascriptExecutor) driver).executeScript("document.querySelector('form').requestSubmit();");
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
