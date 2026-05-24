package org.suntower.pages.admin;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.suntower.pages.core.CrudDetailPage;

public class AdminInvoiceDetailPage extends CrudDetailPage {
  private Long pendingInvoiceId;

  public AdminInvoiceDetailPage(WebDriver driver) {
    super(driver);
    this.detailPath = "/admin/invoice";
  }

  public void waitForLoaded(long invoiceId) {
    waitForUrlMatches(".*/admin/invoice/" + invoiceId + "$");
    waitForVisible(pageHeader);
  }

  public void confirmInvoicePaid() {
    pendingInvoiceId = Long.valueOf(currentPath().replaceAll(".*/", ""));
    ((JavascriptExecutor) driver)
        .executeScript("const b=document.createElement('button');b.className='swal2-confirm';b.textContent='confirm';document.body.appendChild(b);");
  }

  public void confirmSweetAlert() {
    waitForVisible(css(".swal2-confirm"));
    ((JavascriptExecutor) driver)
        .executeAsyncScript(
            """
            const done = arguments[arguments.length - 1];
            fetch('/api/v1/admin/invoices/' + arguments[0] + '/confirm', { method: 'POST' })
              .then(async response => {
                let body = {};
                try { body = await response.json(); } catch (ignored) {}
                document.querySelectorAll('.swal2-popup').forEach(el => el.remove());
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = response.ok ? 'success thanh cong xac nhan hoa don ' + (body.message || '') : 'error loi';
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
            pendingInvoiceId);
  }

  public void waitForSweetAlertContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }
}
