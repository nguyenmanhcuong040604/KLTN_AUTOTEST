package org.suntower.pages.components;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.suntower.core.BasePage;

public class TableComponent extends BasePage {
  private final By body;
  private final By emptyState;

  public TableComponent(WebDriver driver, String bodySelector) {
    this(driver, bodySelector, ".empty-state");
  }

  public TableComponent(WebDriver driver, String bodySelector, String emptySelector) {
    super(driver);
    this.body = css(bodySelector);
    this.emptyState = css(emptySelector);
  }

  public By body() {
    return body;
  }

  public By rowsLocator() {
    return By.cssSelector(selector(body) + " tr");
  }

  public List<WebElement> rows() {
    return driver.findElements(rowsLocator());
  }

  public WebElement rowByText(String text) {
    waitForCondition(
        () ->
            rows().stream()
                .anyMatch(
                    row -> {
                      try {
                        return row.isDisplayed() && row.getText().contains(text);
                      } catch (RuntimeException ignored) {
                        return false;
                      }
                    }),
        "No visible table row contained text: " + text);
    return rows().stream()
        .filter(
            row -> {
              try {
                return row.isDisplayed() && row.getText().contains(text);
              } catch (RuntimeException ignored) {
                return false;
              }
            })
        .findFirst()
        .orElseThrow();
  }

  public void waitForDataOrEmpty() {
    waitForCondition(
        () -> !rows().isEmpty() || isVisible(emptyState),
        "Table did not show data rows or empty state.");
  }

  private String selector(By locator) {
    String raw = locator.toString();
    if (!raw.startsWith("By.cssSelector: ")) {
      throw new IllegalArgumentException("TableComponent requires CSS selectors.");
    }
    return raw.replace("By.cssSelector: ", "");
  }
}
