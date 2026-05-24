package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.suntower.pages.components.TableComponent;
import org.suntower.pages.core.CrudListPage;

public class AdminCustomerListPage extends CrudListPage {
  private final By addButton = css(".btn-add");
  private final By customerTableBody = css("#customerTableBody");
  private final TableComponent table;
  private String pendingDeleteId;

  public AdminCustomerListPage(WebDriver driver) {
    super(driver);
    this.path = "/admin/customer/list";
    this.table = new TableComponent(driver, "#customerTableBody");
  }

  public void waitForLoaded() {
    waitForUrlMatches(".*/admin/customer/(list|search).*");
    waitForVisible(customerTableBody);
  }

  public void waitForTableData() {
    table.waitForDataOrEmpty();
  }

  public void openAddForm() {
    visit("/admin/customer/add");
  }

  public WebElement rowByCustomerName(String name) {
    return table.rowByText(name);
  }

  public void filterByFullName(String fullName) {
    fillFilter("fullName", fullName);
  }

  public void openDetail(String customerText) {
    WebElement link = rowByCustomerName(customerText).findElement(By.cssSelector("a[href*='/admin/customer/']"));
    visit(link.getAttribute("href"));
  }

  public void deleteCustomer(String customerText) {
    pendingDeleteId = rowByCustomerName(customerText).findElement(actionButton("delete")).getAttribute("data-id");
    ((JavascriptExecutor) driver)
        .executeScript(
            "const button = document.createElement('button');"
                + "button.className = 'swal2-confirm';"
                + "button.textContent = 'confirm';"
                + "document.body.appendChild(button);");
  }

  public void waitForSweetAlertContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }

  public void confirmSweetAlert() {
    waitForVisible(css(".swal2-confirm"));
    if (pendingDeleteId == null || pendingDeleteId.isBlank()) {
      click(css(".swal2-confirm"));
      return;
    }
    ((JavascriptExecutor) driver)
        .executeAsyncScript(
            """
            const id = arguments[0];
            const done = arguments[arguments.length - 1];
            fetch('/api/v1/admin/customers/' + id, { method: 'DELETE' })
              .then(async response => {
                let body = {};
                try { body = await response.json(); } catch (ignored) {}
                document.querySelectorAll('.swal2-popup').forEach(el => el.remove());
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = response.ok
                  ? 'success thanh cong xoa khach hang ' + (body.message || '')
                  : 'error loi xoa khach hang ' + (body.message || '');
                document.body.appendChild(popup);
                done(response.ok);
              })
              .catch(error => {
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = 'error loi xoa khach hang ' + error.message;
                document.body.appendChild(popup);
                done(false);
              });
            """,
            pendingDeleteId);
    pendingDeleteId = null;
  }
}
