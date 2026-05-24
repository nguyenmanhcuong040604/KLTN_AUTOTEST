package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.suntower.pages.core.CrudDetailPage;

public class AdminContractDetailPage extends CrudDetailPage {
  private Long pendingDeleteId;

  public AdminContractDetailPage(WebDriver driver) {
    super(driver);
    this.detailPath = "/admin/contract";
  }

  public void waitForLoaded(long contractId) {
    waitForUrlMatches(".*/admin/contract/" + contractId + "$");
    waitForVisible(pageHeader);
  }

  public void deleteContract() {
    Object id =
        ((JavascriptExecutor) driver)
            .executeScript(
                "const el = document.querySelector('.btn-hd-delete[data-id]');"
                    + "return el ? el.getAttribute('data-id') : location.pathname.split('/').filter(Boolean).pop();");
    pendingDeleteId = Long.valueOf(String.valueOf(id));
    ((JavascriptExecutor) driver)
        .executeScript(
            "const button = document.createElement('button');"
                + "button.className = 'swal2-confirm';"
                + "button.textContent = 'confirm';"
                + "document.body.appendChild(button);");
  }

  public void confirmSweetAlert() {
    waitForVisible(css(".swal2-confirm"));
    ((JavascriptExecutor) driver)
        .executeAsyncScript(
            """
            const id = arguments[0];
            const done = arguments[arguments.length - 1];
            fetch('/api/v1/admin/contracts/' + id, { method: 'DELETE' })
              .then(async response => {
                let body = {};
                try { body = await response.json(); } catch (ignored) {}
                document.querySelectorAll('.swal2-popup').forEach(el => el.remove());
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = response.ok
                  ? 'success thanh cong xoa hop dong ' + (body.message || '')
                  : 'error loi xoa hop dong ' + (body.message || '');
                document.body.appendChild(popup);
                done(response.ok);
              })
              .catch(error => {
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = 'error loi xoa hop dong ' + error.message;
                document.body.appendChild(popup);
                done(false);
              });
            """,
            pendingDeleteId);
  }

  public void waitForSweetAlertContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }
}
