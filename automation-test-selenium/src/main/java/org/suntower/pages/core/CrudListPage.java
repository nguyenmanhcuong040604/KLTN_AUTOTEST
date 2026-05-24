package org.suntower.pages.core;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.suntower.core.BasePage;
import org.suntower.helpers.browser.OptionalActionHelper;

public class CrudListPage extends BasePage {
  protected String path;
  protected final By tableBody = css("tbody");
  protected final By filterForm = css("form");
  protected final By searchButton = css("form button[type='submit'], .btn-filter.btn-search");
  protected final By resetButton = css(".btn-filter.btn-reset, form button[type='reset']");

  public CrudListPage(WebDriver driver) {
    super(driver);
  }

  public void open() {
    if (path == null || path.isBlank()) {
      throw new IllegalStateException("This list page does not define a path.");
    }
    visit(path);
  }

  public void search() {
    waitForVisible(searchButton);
    waitForEnabled(searchButton);
    click(searchButton);
  }

  public boolean searchIfAvailable() {
    if (!isVisible(searchButton)) {
      return false;
    }
    search();
    return true;
  }

  public void resetFilters() {
    OptionalActionHelper.clickIfPresent(driver, resetButton);
  }

  public void fillFilter(String fieldName, String value) {
    fill(inputByName(fieldName), value);
  }

  public boolean fillFilterIfPresent(String fieldName, String value) {
    return OptionalActionHelper.fillIfPresent(driver, inputByName(fieldName), value);
  }

  public void selectFilter(String fieldName, String value) {
    selectByValue(inputByName(fieldName), value);
  }

  public boolean selectFilterIfPresent(String fieldName, String value) {
    return OptionalActionHelper.selectIfPresent(driver, inputByName(fieldName), value);
  }

  public WebElement rowByText(String text) {
    By rows = css("tbody tr");
    waitForCondition(
        () -> driver.findElements(rows).stream().anyMatch(row -> row.isDisplayed() && row.getText().contains(text)),
        "No table row contained text: " + text);
    return driver.findElements(rows).stream().filter(row -> row.isDisplayed() && row.getText().contains(text)).findFirst().orElseThrow();
  }

  public WebElement firstRowLink(String hrefPart) {
    By link = css("tbody a[href*='" + hrefPart + "']");
    return firstVisible(link);
  }

  public WebElement firstViewButton() {
    return firstVisible(actionButton(tableBody, "view"));
  }

  public WebElement firstEditButton() {
    return firstVisible(actionButton(tableBody, "edit"));
  }

  public void clickRowLink(String rowText, String hrefPart) {
    WebElement row = rowByText(rowText);
    click(row.findElement(By.cssSelector("a[href*='" + hrefPart + "']")));
  }

  public void clickFirstRowLink(String hrefPart) {
    firstRowLink(hrefPart).click();
  }

  public void clickFirstViewButton() {
    firstViewButton().click();
  }

  public void clickFirstEditButton() {
    firstEditButton().click();
  }

  public void deleteRow(String rowText) {
    click(rowByText(rowText).findElement(actionButton("delete")));
  }

  public void waitForRowVisible(String text) {
    rowByText(text);
  }
}
