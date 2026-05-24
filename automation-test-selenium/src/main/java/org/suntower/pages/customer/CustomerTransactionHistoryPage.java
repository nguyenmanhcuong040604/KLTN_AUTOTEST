package org.suntower.pages.customer;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.suntower.pages.components.TableComponent;
import org.suntower.pages.core.CrudListPage;

public class CustomerTransactionHistoryPage extends CrudListPage {
  private final By filterForm = css("#filterForm");
  private final By tableBody = css("#transactionTableBody");
  private final By resultBanner = css("#totalTransactionSearch");
  private final By pagination = css("#transactionPagination");
  private final By detailModal = css(".modal.show");
  private final TableComponent table;

  public CustomerTransactionHistoryPage(WebDriver driver) {
    super(driver);
    this.path = "/customer/transaction/history";
    this.table = new TableComponent(driver, "#transactionTableBody");
  }

  public void waitForLoaded() {
    waitForUrlContains("/customer/transaction/history");
    waitForVisible(filterForm);
    waitForVisible(tableBody);
  }

  public void waitForSummaryVisible() {
    waitForVisible(css(".stats-bar"));
    waitForCondition(() -> driver.findElements(css(".stats-bar .stat-item")).size() >= 2, "Transaction stats did not load.");
  }

  public void waitForResultCountBanner(int total) {
    waitForText(resultBanner, String.valueOf(total));
  }

  public void filterByMonth(int month) {
    setFormControlValue("month", String.valueOf(month));
  }

  public void filterByYear(int year) {
    setFormControlValue("year", String.valueOf(year));
  }

  public void submitFilters() {
    wait.until(ExpectedConditions.presenceOfElementLocated(filterForm));
    ((JavascriptExecutor) driver).executeScript("document.querySelector('#filterForm').requestSubmit();");
    table.waitForDataOrEmpty();
  }

  public void resetFilters() {
    ((JavascriptExecutor) driver).executeScript("document.querySelector('#filterForm').reset();");
  }

  public WebElement rowByBuildingName(String buildingName) {
    return table.rowByText(buildingName);
  }

  public void openTransactionDetail(String buildingName) {
    click(rowByBuildingName(buildingName));
  }

  public void waitForDetailModalContains(String text) {
    waitForVisible(detailModal);
    waitForText(detailModal, text);
  }

  public void closeDetailModal() {
    click(firstVisible(css(".modal.show .btn-close, .modal.show .modal-footer button")));
    waitForHidden(detailModal);
  }

  public void waitForEmptyState() {
    waitForTextMatches(tableBody, "khong co giao dich|không có giao dịch");
  }

  public void waitForPaginationHidden() {
    waitForCondition(() -> driver.findElement(pagination).getText().isBlank(), "Transaction pagination was not hidden.");
  }

  private void setFormControlValue(String fieldName, String value) {
    wait.until(ExpectedConditions.presenceOfElementLocated(inputByName(fieldName)));
    ((JavascriptExecutor) driver)
        .executeScript(
            "const el = document.querySelector('[name=\"' + arguments[0] + '\"]');"
                + "if (el.tagName === 'SELECT' && !Array.from(el.options).some(option => option.value === arguments[1])) { el.add(new Option(arguments[1], arguments[1])); }"
                + "el.value = arguments[1];"
                + "el.dispatchEvent(new Event('input', { bubbles: true }));"
                + "el.dispatchEvent(new Event('change', { bubbles: true }));",
            fieldName,
            value);
  }
}
