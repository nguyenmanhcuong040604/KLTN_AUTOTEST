package org.suntower.pages.staff;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.suntower.pages.components.TableComponent;
import org.suntower.pages.core.CrudListPage;

public class StaffContractListPage extends CrudListPage {
  private final By filterForm = css("#filterForm");
  private final By tableBody = css("#contractTableBody");
  private final By detailModal = css(".modal.show");
  private final TableComponent table;

  public StaffContractListPage(WebDriver driver) {
    super(driver);
    this.path = "/staff/contracts";
    this.table = new TableComponent(driver, "#contractTableBody");
  }

  public void waitForLoaded() {
    waitForUrlContains("/staff/contracts");
    waitForVisible(filterForm);
    waitForVisible(tableBody);
  }

  public void waitForTableData() {
    table.waitForDataOrEmpty();
  }

  public WebElement rowByContractText(String text) {
    return table.rowByText(text);
  }

  public void filterByCustomer(long customerId) {
    setSelectValue("customerId", String.valueOf(customerId));
  }

  public void filterByBuilding(long buildingId) {
    setSelectValue("buildingId", String.valueOf(buildingId));
  }

  public void filterByStatus(String status) {
    setSelectValue("status", status);
  }

  public void submitFilters() {
    wait.until(ExpectedConditions.presenceOfElementLocated(filterForm));
    ((JavascriptExecutor) driver).executeScript("document.querySelector('#filterForm').requestSubmit();");
    waitForTableData();
  }

  public void openContractDetail(String text) {
    WebElement row = rowByContractText(text);
    click(row.findElement(By.cssSelector(".btn-view")));
  }

  public void waitForDetailModalContains(String text) {
    waitForVisible(detailModal);
    waitForText(detailModal, text);
  }

  private void setSelectValue(String fieldName, String value) {
    wait.until(ExpectedConditions.presenceOfElementLocated(By.name(fieldName)));
    ((JavascriptExecutor) driver)
        .executeScript(
            "const el = document.querySelector('[name=\"' + arguments[0] + '\"]');"
                + "if (!Array.from(el.options).some(option => option.value === arguments[1])) {"
                + "  el.add(new Option(arguments[1], arguments[1]));"
                + "}"
                + "el.value = arguments[1];"
                + "el.dispatchEvent(new Event('input', { bubbles: true }));"
                + "el.dispatchEvent(new Event('change', { bubbles: true }));",
            fieldName,
            value);
  }
}
