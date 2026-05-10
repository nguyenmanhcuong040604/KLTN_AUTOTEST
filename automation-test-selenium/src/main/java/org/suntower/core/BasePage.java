package org.suntower.core;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.suntower.helpers.text.TextNormalizeHelper;
import org.suntower.pages.components.SweetAlertComponent;

public class BasePage {
  protected final WebDriver driver;
  protected final AppConfig config;
  protected final WebDriverWait wait;
  private final SweetAlertComponent sweetAlert;

  public BasePage(WebDriver driver) {
    this.driver = driver;
    this.config = AppConfig.get();
    this.wait = new WebDriverWait(driver, config.expectTimeout());
    this.sweetAlert = this instanceof SweetAlertComponent ? null : new SweetAlertComponent(driver);
  }

  protected void visit(String path) {
    String target = path.startsWith("http") ? path : config.baseUrl() + normalizePath(path);
    driver.navigate().to(target);
    waitForDomReady();
  }

  protected By css(String selector) {
    return By.cssSelector(selector);
  }

  protected By anyCss(String... selectors) {
    return By.cssSelector(String.join(", ", selectors));
  }

  protected By name(String name) {
    return By.name(name);
  }

  protected By inputByName(String name) {
    return By.cssSelector("[name='" + name + "']");
  }

  protected By inputById(String id) {
    return By.cssSelector("#" + id);
  }

  protected By linkByHref(String href) {
    return By.cssSelector("a[href='" + href + "']");
  }

  protected By linkByHrefContains(String hrefPart) {
    return By.cssSelector("a[href*='" + hrefPart + "']");
  }

  protected By buttonByLooseText(String text) {
    return By.xpath(
        "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
            + normalizeLooseText(text)
            + "')]");
  }

  protected By actionButton(String action) {
    return actionButton(By.cssSelector("body"), action);
  }

  protected By actionButton(By scope, String action) {
    String cssSelector =
        switch (action) {
          case "view" ->
              ".btn-view, .btn-action.btn-view, [data-action='view'], [title*='Xem'], [title*='Chi tiet'], [title*='detail']";
          case "edit" ->
              ".btn-edit, .btn-action.btn-edit, [data-action='edit'], [title*='Chinh sua'], [title*='Sua'], [title*='Edit']";
          case "delete" ->
              ".btn-delete, .btn-action.btn-delete, [data-action='delete'], [title*='Xoa'], [title*='Delete']";
          case "pay" ->
              ".btn-pay, .btn-action.btn-pay, [data-action='pay'], [title*='Thanh toan'], [title*='Payment']";
          case "approve" ->
              ".btn-approve, .btn-action.btn-approve, [data-action='approve'], [title*='Duyet'], [title*='Approve']";
          case "reject" ->
              ".btn-reject, .btn-action.btn-reject, [data-action='reject'], [title*='Tu choi'], [title*='Reject']";
          default -> throw new IllegalArgumentException("Unsupported action: " + action);
        };

    String scopeExpression = scope.toString().replace("By.cssSelector: ", "");
    if (!scope.toString().startsWith("By.cssSelector: ")) {
      return By.cssSelector(cssSelector);
    }
    return By.cssSelector(scopeExpression + " " + cssSelector);
  }

  protected WebElement visible(By locator) {
    return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
  }

  protected List<WebElement> visibles(By locator) {
    return driver.findElements(locator).stream().filter(WebElement::isDisplayed).toList();
  }

  protected WebElement firstVisible(By locator) {
    waitForCondition(() -> !visibles(locator).isEmpty(), "No visible element found for " + locator);
    return visibles(locator).get(0);
  }

  protected void click(By locator) {
    wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
  }

  protected void click(WebElement element) {
    wait.until(ExpectedConditions.elementToBeClickable(element)).click();
  }

  protected void fill(By locator, String value) {
    WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
    element.clear();
    element.sendKeys(value);
  }

  protected void fill(WebElement element, String value) {
    wait.until(ExpectedConditions.elementToBeClickable(element));
    element.clear();
    element.sendKeys(value);
  }

  protected void selectByValue(By locator, String value) {
    new Select(visible(locator)).selectByValue(value);
  }

  protected void setInputValue(By locator, String value) {
    WebElement element = visible(locator);
    if (element.isEnabled()) {
      element.clear();
      element.sendKeys(value);
      return;
    }
    ((JavascriptExecutor) driver)
        .executeScript(
            "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input', { bubbles: true })); arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
            element,
            value);
  }

  protected String value(By locator) {
    return visible(locator).getAttribute("value");
  }

  protected String text(By locator) {
    return visible(locator).getText();
  }

  protected String looseText(By locator) {
    return normalizeLooseText(text(locator));
  }

  protected int count(By locator) {
    return driver.findElements(locator).size();
  }

  protected boolean isVisible(By locator) {
    try {
      return driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
    } catch (RuntimeException error) {
      return false;
    }
  }

  protected void waitForVisible(By locator) {
    wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
  }

  protected void waitForHidden(By locator) {
    wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
  }

  protected void waitForUrlContains(String path) {
    wait.until(ExpectedConditions.urlContains(path));
  }

  protected void waitForUrlMatches(String regex) {
    wait.until(ExpectedConditions.urlMatches(regex));
  }

  protected void waitForText(By locator, String expected) {
    waitForCondition(() -> driver.findElements(locator).stream().anyMatch(el -> el.getText().contains(expected)),
        "Text not found for " + locator + ": " + expected);
  }

  protected void waitForTextMatches(By locator, String regex) {
    waitForCondition(() -> driver.findElements(locator).stream().anyMatch(el -> el.getText().matches("(?is).*" + regex + ".*")),
        "Text regex not found for " + locator + ": " + regex);
  }

  protected void waitForValue(By locator, String expected) {
    waitForCondition(() -> expected.equals(driver.findElement(locator).getAttribute("value")),
        "Value not found for " + locator + ": " + expected);
  }

  protected void waitForEnabled(By locator) {
    wait.until(ExpectedConditions.elementToBeClickable(locator));
  }

  protected void waitForDisabled(By locator) {
    waitForCondition(() -> !driver.findElement(locator).isEnabled(), "Locator did not become disabled: " + locator);
  }

  protected void waitForEmpty(By locator) {
    waitForCondition(() -> text(locator).isBlank(), "Locator did not become empty: " + locator);
  }

  protected void waitForCondition(BooleanSupplier predicate, String message) {
    try {
      new WebDriverWait(driver, config.expectTimeout()).until(ignored -> predicate.getAsBoolean());
    } catch (TimeoutException error) {
      throw new AssertionError(message, error);
    }
  }

  protected void waitForDomReady() {
    new WebDriverWait(driver, config.navigationTimeout())
        .until(
            ignored ->
                "complete".equals(((JavascriptExecutor) driver).executeScript("return document.readyState"))
                    || "interactive".equals(((JavascriptExecutor) driver).executeScript("return document.readyState")));
  }

  protected String normalizeLooseText(String value) {
    if (value == null) {
      return "";
    }
    return TextNormalizeHelper.normalizeLooseText(value);
  }

  protected String currentPath() {
    return URI.create(driver.getCurrentUrl()).getPath();
  }

  protected void waitForCurrentUrlContains(String value) {
    waitForUrlContains(value);
  }

  protected void check(By locator) {
    WebElement element = visible(locator);
    if (!element.isSelected()) {
      element.click();
    }
  }

  protected String normalizePath(String path) {
    return path.startsWith("/") ? path : "/" + path;
  }

  protected String urlEncode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  protected void dismissSweetAlertIfPresent() {
    By confirm = By.cssSelector(".swal2-confirm");
    if (isVisible(confirm) && sweetAlert != null) {
      sweetAlert.confirmIfPresent();
    }
  }

  protected void waitForSweetAlertContainsText(String regex) {
    if (sweetAlert == null) {
      throw new IllegalStateException("Nested SweetAlert calls are not supported.");
    }
    sweetAlert.waitForTextLoose(regex);
  }
}
