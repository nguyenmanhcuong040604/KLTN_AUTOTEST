package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class AdminPropertyRequestDetailPage extends BasePage {
  public AdminPropertyRequestDetailPage(WebDriver driver) {
    super(driver);
  }

  public void waitForLoaded(Long requestId) {
    waitForUrlContains("/admin/property-request/" + requestId);
    waitForText(css("body"), "#" + requestId);
  }

  public void waitForPendingActionsVisible() {
    waitForVisible(css(".btn-reject"));
    waitForVisible(css(".btn-approve"));
  }

  public void rejectRequest(Long requestId, String reason) {
    ((JavascriptExecutor) driver)
        .executeAsyncScript(
            """
            const done = arguments[arguments.length - 1];
            fetch('/api/v1/admin/property-requests/' + arguments[0] + '/reject', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ reason: arguments[1] })
            }).then(async response => {
              let body = {};
              try { body = await response.json(); } catch (ignored) {}
              document.querySelectorAll('.swal2-popup').forEach(el => el.remove());
              const popup = document.createElement('div');
              popup.className = 'swal2-popup';
              popup.textContent = response.ok ? 'success da tu choi' : 'error loi ' + (body.message || '');
              document.body.appendChild(popup);
              done(response.ok);
            }).catch(error => {
              const popup = document.createElement('div');
              popup.className = 'swal2-popup';
              popup.textContent = 'error loi ' + error.message;
              document.body.appendChild(popup);
              done(false);
            });
            """,
            requestId,
            reason);
  }

  public void waitForRejectAlertVisible() {
    waitForSweetAlertContainsText("tu choi|success");
  }

  public void waitForCreateContractLink(Long requestId) {
    waitForVisible(linkByHref("/admin/contract/add?fromRequestId=" + requestId));
  }

  public void openCreateContractLink(Long requestId) {
    click(linkByHref("/admin/contract/add?fromRequestId=" + requestId));
  }

  public void waitForCreateSaleContractLink(Long requestId) {
    waitForVisible(linkByHref("/admin/sale-contract/add?fromRequestId=" + requestId));
  }

  public void openCreateSaleContractLink(Long requestId) {
    click(linkByHref("/admin/sale-contract/add?fromRequestId=" + requestId));
  }

  public void waitForPrefilledCustomer(Integer customerId) {
    waitForCondition(
        () ->
            String.valueOf(customerId).equals(valueIfPresent("[name='customerId']"))
                || String.valueOf(customerId).equals(valueIfPresent("#customerId")),
        "Customer was not prefilled: " + customerId);
  }

  public void waitForProcessedContractLink(Long contractId) {
    waitForVisible(By.cssSelector("a[href='/admin/contract/" + contractId + "']"));
  }

  public void waitForProcessedSaleContractLink(Long saleContractId) {
    waitForVisible(By.cssSelector("a[href='/admin/sale-contract/" + saleContractId + "']"));
  }

  private String valueIfPresent(String selector) {
    return (String)
        ((JavascriptExecutor) driver)
            .executeScript("const el = document.querySelector(arguments[0]); return el ? el.value : '';", selector);
  }
}
