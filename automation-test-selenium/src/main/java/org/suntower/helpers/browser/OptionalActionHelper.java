package org.suntower.helpers.browser;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public final class OptionalActionHelper {
  private OptionalActionHelper() {}

  public static boolean clickIfPresent(WebDriver driver, By locator) {
    return driver.findElements(locator).stream()
        .filter(WebElement::isDisplayed)
        .findFirst()
        .map(
            element -> {
              element.click();
              return true;
            })
        .orElse(false);
  }

  public static boolean fillIfPresent(WebDriver driver, By locator, String value) {
    return driver.findElements(locator).stream()
        .filter(WebElement::isDisplayed)
        .findFirst()
        .map(
            element -> {
              element.clear();
              element.sendKeys(value);
              return true;
            })
        .orElse(false);
  }

  public static boolean selectIfPresent(WebDriver driver, By locator, String value) {
    return driver.findElements(locator).stream()
        .filter(WebElement::isDisplayed)
        .findFirst()
        .map(
            element -> {
              new Select(element).selectByValue(value);
              return true;
            })
        .orElse(false);
  }
}
