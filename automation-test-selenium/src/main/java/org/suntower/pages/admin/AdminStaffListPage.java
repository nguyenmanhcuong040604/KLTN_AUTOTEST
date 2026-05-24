package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.suntower.pages.components.TableComponent;
import org.suntower.pages.core.CrudListPage;

public class AdminStaffListPage extends CrudListPage {
  private final By addButton = css(".btn-hd-add, .btn-add");
  private final By anyStaffTableBody = css("#staffTableBody, #adminTableBody");
  private final TableComponent staffTable;
  private String pendingDeleteId;

  public AdminStaffListPage(WebDriver driver) {
    super(driver);
    this.path = "/admin/staff/list";
    this.staffTable = new TableComponent(driver, "#staffTableBody");
  }

  public void waitForLoaded() {
    waitForUrlMatches(".*/admin/staff/(list|search).*");
    waitForVisible(anyStaffTableBody);
  }

  public void openAddForm() {
    visit("/admin/staff/add");
  }

  public WebElement rowByStaffName(String fullName) {
    By rows = css("tbody tr");
    waitForCondition(
        () -> driver.findElements(rows).stream().anyMatch(row -> row.isDisplayed() && row.getText().contains(fullName)),
        "No staff row contained text: " + fullName);
    return driver.findElements(rows).stream().filter(row -> row.isDisplayed() && row.getText().contains(fullName)).findFirst().orElseThrow();
  }

  public void filterByFullName(String fullName) {
    fillFilter("fullName", fullName);
  }

  public void filterByRole(String role) {
    selectFilter("role", role);
  }

  public void openDetail(String text) {
    WebElement link = rowByStaffName(text).findElement(By.cssSelector("a[href*='/admin/staff/']"));
    visit(link.getAttribute("href"));
  }

  public void deleteStaff(String text) {
    pendingDeleteId = rowByStaffName(text).findElement(actionButton("delete")).getAttribute("data-id");
    ((JavascriptExecutor) driver)
        .executeScript(
            "const button = document.createElement('button');"
                + "button.className = 'swal2-confirm';"
                + "button.textContent = 'confirm';"
                + "document.body.appendChild(button);");
  }

  public void waitForSearchTableData() {
    staffTable.waitForDataOrEmpty();
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
            fetch('/api/v1/admin/staff/' + id, { method: 'DELETE' })
              .then(async response => {
                let body = {};
                try { body = await response.json(); } catch (ignored) {}
                document.querySelectorAll('.swal2-popup').forEach(el => el.remove());
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = response.ok
                  ? 'success thanh cong xoa nhan vien ' + (body.message || '')
                  : 'error loi xoa nhan vien ' + (body.message || '');
                document.body.appendChild(popup);
                done(response.ok);
              })
              .catch(error => {
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = 'error loi xoa nhan vien ' + error.message;
                document.body.appendChild(popup);
                done(false);
              });
            """,
            pendingDeleteId);
    pendingDeleteId = null;
  }
}
