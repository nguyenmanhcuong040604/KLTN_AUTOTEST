package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.suntower.pages.components.TableComponent;
import org.suntower.pages.core.CrudListPage;

public class AdminBuildingListPage extends CrudListPage {
  private final By buildingTableBody = css("#buildingTableBody");
  private final TableComponent table;
  private String pendingDeleteId;

  public AdminBuildingListPage(WebDriver driver) {
    super(driver);
    this.path = "/admin/building/list";
    this.table = new TableComponent(driver, "#buildingTableBody");
  }

  public void waitForLoaded() {
    waitForUrlMatches(".*/admin/building/(list|search).*");
    waitForVisible(buildingTableBody);
  }

  public void waitForTableData() {
    table.waitForDataOrEmpty();
  }

  public void openAddForm() {
    visit("/admin/building/add");
  }

  public void filterByName(String name) {
    setFormControlValue("name", name);
  }

  public void filterByPropertyType(String propertyType) {
    setFormControlValue("propertyType", propertyType);
  }

  public void filterByTransactionType(String transactionType) {
    setFormControlValue("transactionType", transactionType);
  }

  public void filterByDistrict(String districtId) {
    setFormControlValue("districtId", districtId);
  }

  @Override
  public void search() {
    wait.until(ExpectedConditions.presenceOfElementLocated(css("#searchForm")));
    ((JavascriptExecutor) driver)
        .executeScript("document.querySelector('#searchForm').requestSubmit();");
    waitForTableData();
  }

  public WebElement rowByBuildingName(String name) {
    return table.rowByText(name);
  }

  public void openDetail(String name) {
    clickRowLink(name, "/admin/building/");
  }

  public void openEdit(String name) {
    clickRowLink(name, "/admin/building/edit/");
  }

  public void openAdditionalInformation(String name) {
    clickRowLink(name, "/admin/building-additional-information/");
  }

  public void deleteBuilding(String name) {
    pendingDeleteId = rowByBuildingName(name).findElement(actionButton("delete")).getAttribute("data-id");
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
            fetch('/api/v1/admin/buildings/' + id, { method: 'DELETE' })
              .then(async response => {
                let body = {};
                try { body = await response.json(); } catch (ignored) {}
                document.querySelectorAll('.swal2-popup').forEach(el => el.remove());
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = response.ok
                  ? 'success thanh cong xoa bat dong san ' + (body.message || '')
                  : 'error loi xoa bat dong san ' + (body.message || '');
                document.body.appendChild(popup);
                done(response.ok);
              })
              .catch(error => {
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = 'error loi xoa bat dong san ' + error.message;
                document.body.appendChild(popup);
                done(false);
              });
            """,
            pendingDeleteId);
    pendingDeleteId = null;
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
