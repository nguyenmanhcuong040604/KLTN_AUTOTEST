package org.suntower.pages.customer;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.suntower.pages.core.CrudListPage;

public class CustomerContractListPage extends CrudListPage {
  private final org.openqa.selenium.By list = css("#contractList");

  public CustomerContractListPage(WebDriver driver) {
    super(driver);
    this.path = "/customer/contract/list";
  }

  public void waitForLoaded() {
    waitForUrlMatches(".*/customer/(contract/list|contracts).*");
    waitForVisible(list);
  }

  public void waitForContractData() {
    waitForCondition(
        () -> !driver.findElements(css("#contractList .contract-container")).isEmpty() || isVisible(css("#contractList .empty-state")),
        "Customer contract list did not load cards or empty state.");
  }

  public void filterByBuilding(long buildingId) {
    setFormControlValue("buildingId", String.valueOf(buildingId));
  }

  public void filterByStatus(String status) {
    setFormControlValue("status", status);
  }

  public void submitFilters() {
    wait.until(ExpectedConditions.presenceOfElementLocated(css("#filterForm")));
    ((JavascriptExecutor) driver).executeScript("document.querySelector('#filterForm').requestSubmit();");
    waitForContractData();
  }

  public WebElement cardByBuildingName(String name) {
    waitForCondition(
        () ->
            driver.findElements(css("#contractList .contract-container")).stream()
                .anyMatch(
                    card -> {
                      try {
                        return card.isDisplayed() && card.getText().contains(name);
                      } catch (RuntimeException ignored) {
                        return false;
                      }
                    }),
        "No visible customer contract card contained text: " + name);
    return driver.findElements(css("#contractList .contract-container")).stream()
        .filter(
            card -> {
              try {
                return card.isDisplayed() && card.getText().contains(name);
              } catch (RuntimeException ignored) {
                return false;
              }
            })
        .findFirst()
        .orElseThrow();
  }

  public void waitForEmptyState() {
    waitForVisible(css("#contractList .empty-state"));
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
