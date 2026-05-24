package org.suntower.pages.admin;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.suntower.pages.core.CrudDetailPage;

public class AdminSaleContractDetailPage extends CrudDetailPage {
  private Long pendingDeleteId;

  public AdminSaleContractDetailPage(WebDriver driver) {
    super(driver);
    this.detailPath = "/admin/sale-contract";
  }

  public void waitForLoaded(long id) {
    waitForUrlMatches(".*/admin/sale-contract/" + id + "$");
    waitForVisible(pageHeader);
  }

  public void deleteSaleContract() {
    Object id =
        ((JavascriptExecutor) driver)
            .executeScript(
                "const el = document.querySelector('.btn-hd-delete[data-id]');"
                    + "return el ? el.getAttribute('data-id') : location.pathname.split('/').filter(Boolean).pop();");
    pendingDeleteId = Long.valueOf(String.valueOf(id));
    ((JavascriptExecutor) driver)
        .executeScript("const b=document.createElement('button');b.className='swal2-confirm';b.textContent='confirm';document.body.appendChild(b);");
  }

  public void confirmSweetAlert() {
    waitForVisible(css(".swal2-confirm"));
    ((JavascriptExecutor) driver)
        .executeAsyncScript(
            """
            const id = arguments[0];
            const done = arguments[arguments.length - 1];
            fetch('/api/v1/admin/sale-contracts/' + id, { method: 'DELETE' })
              .then(async response => {
                let body = {};
                try { body = await response.json(); } catch (ignored) {}
                document.querySelectorAll('.swal2-popup').forEach(el => el.remove());
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = response.ok ? 'success thanh cong xoa hop dong mua ban ' + (body.message || '') : 'error loi';
                document.body.appendChild(popup);
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
            pendingDeleteId);
  }

  public void waitForSweetAlertContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }
}
