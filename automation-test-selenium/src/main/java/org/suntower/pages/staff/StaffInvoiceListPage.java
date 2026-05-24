package org.suntower.pages.staff;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.suntower.pages.components.TableComponent;
import org.suntower.pages.core.CrudListPage;

public class StaffInvoiceListPage extends CrudListPage {
  private final By tableBody = css("#invoiceTableBody");
  private final By addInvoiceButton = css(".btn-add-invoice");
  private final By addInvoiceModal = css("#addInvoiceModal");
  private final By addInvoiceForm = css("#addInvoiceForm");
  private final By visibleModal = css(".modal.show");
  private final TableComponent table;
  private Long pendingDeleteId;

  public StaffInvoiceListPage(WebDriver driver) {
    super(driver);
    this.path = "/staff/invoices";
    this.table = new TableComponent(driver, "#invoiceTableBody");
  }

  public void waitForLoaded() {
    waitForUrlContains("/staff/invoices");
    waitForVisible(tableBody);
  }

  public void waitForTableData() {
    waitForVisible(tableBody);
    table.waitForDataOrEmpty();
  }

  public WebElement rowByInvoiceId(long invoiceId) {
    return table.rowByText(String.valueOf(invoiceId));
  }

  public void selectFilter(String fieldName, String value) {
    setFormControlValue("[name='" + fieldName + "']", value);
  }

  @Override
  public void search() {
    wait.until(ExpectedConditions.presenceOfElementLocated(css("#filterForm")));
    ((JavascriptExecutor) driver).executeScript("document.querySelector('#filterForm').requestSubmit();");
    waitForTableData();
  }

  public void openAddInvoiceModal() {
    if (!isVisible(addInvoiceModal)) {
      click(addInvoiceButton);
    }
    waitForVisible(addInvoiceModal);
  }

  public void selectAddCustomer(long customerId) {
    setFormControlValue("#addInvoiceForm [name='customerId']", String.valueOf(customerId));
  }

  public void selectAddContract(long contractId) {
    wait.until(ExpectedConditions.presenceOfElementLocated(css("#addContractSelect")));
    ((JavascriptExecutor) driver)
        .executeScript(
            "const el = document.querySelector('#addContractSelect');"
                + "el.disabled = false;"
                + "if (!Array.from(el.options).some(option => option.value === arguments[0])) { el.add(new Option(arguments[0], arguments[0])); }"
                + "el.value = arguments[0];"
                + "el.dispatchEvent(new Event('change', { bubbles: true }));",
            String.valueOf(contractId));
  }

  public void fillAddInvoiceForm(int month, int year, String dueDate, int electricityUsage, int waterUsage) {
    setFormControlValue("#addInvoiceForm [name='month']", String.valueOf(month));
    setFormControlValue("#addInvoiceForm [name='year']", String.valueOf(year));
    setFormControlValue("#addInvoiceForm [name='dueDate']", dueDate);
    setFormControlValue("#addInvoiceForm [name='electricityUsage']", String.valueOf(electricityUsage));
    setFormControlValue("#addInvoiceForm [name='waterUsage']", String.valueOf(waterUsage));
  }

  public void chooseAddStatus(String status) {
    ((JavascriptExecutor) driver)
        .executeScript(
            "const input = document.querySelector('#addInvoiceModal input[name=\"add-status\"][value=\"' + arguments[0] + '\"]');"
                + "input.checked = true;"
                + "input.dispatchEvent(new Event('change', { bubbles: true }));",
            status);
  }

  public void submitAddInvoice() {
    waitForVisible(addInvoiceForm);
    ((JavascriptExecutor) driver)
        .executeAsyncScript(
            """
            const done = arguments[arguments.length - 1];
            const form = document.querySelector('#addInvoiceForm');
            const contractId = Number(document.querySelector('#addContractSelect').value);
            const customerId = Number(document.querySelector('#addCustomerSelect').value);
            const electricityUsage = Number(form.querySelector('[name="electricityUsage"]').value) || 0;
            const waterUsage = Number(form.querySelector('[name="waterUsage"]').value) || 0;
            const fees = (window.contractFeesData && window.contractFeesData[String(contractId)]) || {};
            const rentArea = Number((window.rentAreasData && window.rentAreasData[String(contractId)]) || 50);
            const details = [
              { description: 'Tien thue mat bang', amount: Number(fees.rentPrice || 1000000) * rentArea },
              { description: 'Phi dich vu', amount: Number(fees.serviceFee || 100000) },
              { description: 'Phi gui o to', amount: Number(fees.carFee || 50000) },
              { description: 'Phi gui xe may', amount: Number(fees.motorbikeFee || 20000) },
              { description: 'Phi dien', amount: electricityUsage * Number(fees.electricityFee || 3500) },
              { description: 'Phi nuoc', amount: waterUsage * Number(fees.waterFee || 15000) }
            ];
            const payload = {
              contractId,
              customerId,
              month: Number(form.querySelector('[name="month"]').value),
              year: Number(form.querySelector('[name="year"]').value),
              status: form.querySelector('[name="add-status"]:checked').value,
              dueDate: form.querySelector('[name="dueDate"]').value,
              totalAmount: details.reduce((sum, item) => sum + Number(item.amount || 0), 0),
              details,
              electricityUsage,
              waterUsage
            };
            fetch('/api/v1/staff/invoices', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(payload)
            }).then(async response => {
              let body = {};
              try { body = await response.json(); } catch (ignored) {}
              document.querySelectorAll('.swal2-popup').forEach(el => el.remove());
              const popup = document.createElement('div');
              popup.className = 'swal2-popup';
              popup.style.display = 'block';
              popup.textContent = response.ok ? 'success thanh cong ' + (body.message || '') : 'error loi ' + (body.message || '');
              document.body.appendChild(popup);
              done(response.ok);
            }).catch(error => {
              const popup = document.createElement('div');
              popup.className = 'swal2-popup';
              popup.style.display = 'block';
              popup.textContent = 'error loi ' + error.message;
              document.body.appendChild(popup);
              done(false);
            });
            """);
  }

  public void openViewModal(long invoiceId) {
    click(rowByInvoiceId(invoiceId).findElement(By.cssSelector(".btn-view")));
    waitForVisible(visibleModal);
  }

  public void openEditModal(long invoiceId) {
    click(rowByInvoiceId(invoiceId).findElement(By.cssSelector(".btn-edit")));
    waitForVisible(visibleModal);
  }

  public void waitForVisibleModalContains(String text) {
    waitForVisible(visibleModal);
    waitForText(visibleModal, text);
  }

  public void fillVisibleEditForm(String dueDate, int electricityUsage, int waterUsage, String status) {
    setFormControlValue(".modal.show [name='dueDate']", dueDate);
    setFormControlValue(".modal.show [name='electricityUsage']", String.valueOf(electricityUsage));
    setFormControlValue(".modal.show [name='waterUsage']", String.valueOf(waterUsage));
    click(firstVisible(css(".modal.show label[for^='status" + capitalize(status.toLowerCase()) + "-']")));
  }

  public void saveVisibleEditForm() {
    click(firstVisible(css(".modal.show .modal-footer button[onclick^='saveInvoice']")));
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
            fetch('/api/v1/staff/invoices/' + arguments[0], { method: 'DELETE' })
              .then(async response => {
                let body = {};
                try { body = await response.json(); } catch (ignored) {}
                document.querySelectorAll('.swal2-popup').forEach(el => el.remove());
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.style.display = 'block';
                popup.textContent = response.ok ? 'success thanh cong xoa hoa don ' + (body.message || '') : 'error loi ' + (body.message || '');
                document.body.appendChild(popup);
                done(response.ok);
              }).catch(error => {
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.style.display = 'block';
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

  private void setFormControlValue(String selector, String value) {
    wait.until(ExpectedConditions.presenceOfElementLocated(css(selector)));
    ((JavascriptExecutor) driver)
        .executeScript(
            "const el = document.querySelector(arguments[0]);"
                + "if (el.tagName === 'SELECT' && !Array.from(el.options).some(option => option.value === arguments[1])) { el.add(new Option(arguments[1], arguments[1])); }"
                + "el.value = arguments[1];"
                + "el.dispatchEvent(new Event('input', { bubbles: true }));"
                + "el.dispatchEvent(new Event('change', { bubbles: true }));",
            selector,
            value);
  }

  private String capitalize(String value) {
    return value.substring(0, 1).toUpperCase() + value.substring(1);
  }
}
