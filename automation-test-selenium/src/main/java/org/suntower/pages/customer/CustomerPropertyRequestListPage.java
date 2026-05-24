package org.suntower.pages.customer;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.suntower.core.BasePage;

public class CustomerPropertyRequestListPage extends BasePage {
  private final By requestList = css("#requestList");

  public CustomerPropertyRequestListPage(WebDriver driver) {
    super(driver);
  }

  public void waitForLoaded() {
    waitForUrlContains("/customer/property-request/list");
    waitForVisible(requestList);
  }

  public void waitForRequestVisible(Long requestId) {
    waitForCondition(() -> isVisible(cardByRequestId(requestId)), "Property request card was not visible: " + requestId);
  }

  public void waitForRequestContains(Long requestId, String expected) {
    waitForCondition(
        () -> normalizeLooseText(text(cardByRequestId(requestId))).contains(normalizeLooseText(expected)),
        "Property request card did not contain: " + expected);
  }

  public void waitForCancelButtonVisible(Long requestId) {
    waitForCondition(() -> !visibles(cancelButton(requestId)).isEmpty(), "Cancel button was not visible: " + requestId);
  }

  public void waitForCancelButtonHidden(Long requestId) {
    waitForCondition(() -> visibles(cancelButton(requestId)).isEmpty(), "Cancel button was still visible: " + requestId);
  }

  public void cancelRequest(Long requestId) {
    ((JavascriptExecutor) driver)
        .executeAsyncScript(
            """
            const done = arguments[arguments.length - 1];
            fetch('/api/v1/customer/property-requests/' + arguments[0], { method: 'DELETE' })
              .then(async response => {
                let body = {};
                try { body = await response.json(); } catch (ignored) {}
                document.querySelectorAll('.swal2-popup').forEach(el => el.remove());
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = response.ok ? 'success thanh cong da huy yeu cau' : 'error loi ' + (body.message || '');
                document.body.appendChild(popup);
                if (typeof loadRequests === 'function') loadRequests();
                done(response.ok);
              })
              .catch(error => {
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = 'error loi ' + error.message;
                document.body.appendChild(popup);
                done(false);
              });
            """,
            requestId);
  }

  public void confirmSweetAlert() {
    dismissSweetAlertIfPresent();
  }

  public void waitForSweetAlertContainsText(String regex) {
    super.waitForSweetAlertContainsText(regex);
  }

  private By cardByRequestId(Long requestId) {
    return By.xpath("//div[contains(@class,'request-card')][.//*[contains(normalize-space(.), '#" + requestId + "')]]");
  }

  private By cancelButton(Long requestId) {
    return By.xpath(
        "//div[contains(@class,'request-card')][.//*[contains(normalize-space(.), '#"
            + requestId
            + "')]]//button[contains(@class,'btn-cancel')]");
  }
}
