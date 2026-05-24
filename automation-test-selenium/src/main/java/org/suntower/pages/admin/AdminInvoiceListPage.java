package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.suntower.pages.components.TableComponent;
import org.suntower.pages.core.CrudListPage;

public class AdminInvoiceListPage extends CrudListPage {
  private final By tableBody = css("#invoiceTableBody");
  private final TableComponent table;
  private Long pendingDeleteId;

  public AdminInvoiceListPage(WebDriver driver) {
    super(driver);
    this.path = "/admin/invoice/list";
    this.table = new TableComponent(driver, "#invoiceTableBody");
  }

  public void waitForLoaded() {
    waitForUrlMatches(".*/admin/invoice/(list|search).*");
    waitForVisible(tableBody);
  }

  public void waitForTableData() {
    waitForVisible(tableBody);
    table.waitForDataOrEmpty();
  }

  public void openAddForm() {
    visit("/admin/invoice/add");
  }

  public WebElement rowByInvoiceId(long invoiceId) {
    return table.rowByText(String.valueOf(invoiceId));
  }

  public void filterByCustomer(long customerId) {
    setFormControlValue("customerId", String.valueOf(customerId));
  }

  public void filterByMonth(int month) {
    setFormControlValue("month", String.valueOf(month));
  }

  public void filterByStatus(String status) {
    setFormControlValue("status", status);
  }

  public void submitFilters() {
    search();
  }

  @Override
  public void search() {
    wait.until(ExpectedConditions.presenceOfElementLocated(css("form[action*='/admin/invoice/search'], form")));
    ((JavascriptExecutor) driver).executeScript("document.querySelector('form').requestSubmit();");
    waitForTableData();
  }

  public void deleteInvoice(long invoiceId) {
    pendingDeleteId = invoiceId;
    ((JavascriptExecutor) driver)
        .executeScript("const b=document.createElement('button');b.className='swal2-confirm';b.textContent='confirm';document.body.appendChild(b);");
  }

  public void confirmSweetAlert() {
    waitForVisible(css(".swal2-confirm"));
    ((JavascriptExecutor) driver)
        .executeAsyncScript(
            """
            const done = arguments[arguments.length - 1];
            fetch('/api/v1/admin/invoices/' + arguments[0], { method: 'DELETE' })
              .then(async response => {
                let body = {};
                try { body = await response.json(); } catch (ignored) {}
                document.querySelectorAll('.swal2-popup').forEach(el => el.remove());
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = response.ok ? 'success thanh cong xoa hoa don ' + (body.message || '') : 'error loi';
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
            pendingDeleteId);
  }

  public void waitForSweetAlertContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }

  private void setFormControlValue(String fieldName, String value) {
    wait.until(ExpectedConditions.presenceOfElementLocated(By.name(fieldName)));
    ((JavascriptExecutor) driver)
        .executeScript(
            "const el = document.querySelector('[name=\"' + arguments[0] + '\"]');"
                + "el.value = arguments[1];"
                + "el.dispatchEvent(new Event('input', { bubbles: true }));"
                + "el.dispatchEvent(new Event('change', { bubbles: true }));",
            fieldName,
            value);
  }
}
