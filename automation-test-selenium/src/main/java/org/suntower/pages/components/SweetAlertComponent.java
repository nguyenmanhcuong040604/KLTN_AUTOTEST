package org.suntower.pages.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class SweetAlertComponent extends BasePage {
  private final By popup = css(".swal2-popup");
  private final By confirmButton = css(".swal2-confirm");
  private final By cancelButton = css(".swal2-cancel");
  private final By textarea = css(".swal2-textarea");

  public SweetAlertComponent(WebDriver driver) {
    super(driver);
  }

  public void waitForVisible() {
    waitForVisible(popup);
    waitUntilNotProcessing();
  }

  public void waitForTextLoose(String regex) {
    waitForVisible();
    waitForCondition(
        () -> normalizeLooseText(text(popup)).matches("(?is).*(" + regex + ").*"),
        "SweetAlert did not contain expected text: " + regex);
  }

  public void confirm() {
    click(confirmButton);
  }

  public void cancel() {
    click(cancelButton);
  }

  public void fillTextarea(String value) {
    fill(textarea, value);
  }

  public void confirmIfPresent() {
    if (isVisible(confirmButton)) {
      click(confirmButton);
      waitForHidden(popup);
    }
  }

  public void waitUntilNotProcessing() {
    waitForCondition(
        () -> !normalizeLooseText(text(popup)).matches(".*(dang xu ly|vui long doi|processing|please wait).*"),
        "SweetAlert remained in processing state.");
  }
}
