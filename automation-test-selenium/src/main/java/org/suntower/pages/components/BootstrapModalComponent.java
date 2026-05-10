package org.suntower.pages.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class BootstrapModalComponent extends BasePage {
  private final By modal;

  public BootstrapModalComponent(WebDriver driver) {
    this(driver, ".modal.show");
  }

  public BootstrapModalComponent(WebDriver driver, String selector) {
    super(driver);
    this.modal = css(selector);
  }

  public By byId(String id) {
    return css("#" + id);
  }

  public By visible() {
    return modal;
  }

  public void waitForVisible() {
    waitForVisible(modal);
  }

  public void waitForVisible(By locator) {
    super.waitForVisible(locator);
  }

  public void waitForHidden() {
    waitForHidden(modal);
  }

  public void waitForHidden(By locator) {
    super.waitForHidden(locator);
  }

  public void close() {
    close(modal);
  }

  public void close(By locator) {
    By closeButton = css(selector(locator) + " .modal-header .btn-close, " + selector(locator) + " .modal-footer button, " + selector(locator) + " [data-bs-dismiss='modal']");
    click(closeButton);
    waitForHidden(locator);
  }

  public String text() {
    return text(modal).trim();
  }

  public String text(By locator) {
    return super.text(locator).trim();
  }

  private String selector(By locator) {
    String raw = locator.toString();
    if (!raw.startsWith("By.cssSelector: ")) {
      throw new IllegalArgumentException("BootstrapModalComponent requires CSS selectors.");
    }
    return raw.replace("By.cssSelector: ", "");
  }
}
