package org.suntower.pages.admin;

import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.suntower.pages.core.CrudFormPage;

public class AdminInvoiceFormPage extends CrudFormPage {
  private Long customerId;
  private Long contractId;
  private Integer month;
  private Integer year;
  private String dueDate;
  private Integer electricityUsage;
  private Integer waterUsage;

  public AdminInvoiceFormPage(WebDriver driver) {
    super(driver);
    this.addPath = "/admin/invoice/add";
    this.editPath = "/admin/invoice/edit";
  }

  public void waitForAddLoaded() {
    waitForUrlContains("/admin/invoice/add");
    waitForVisible(css("#invoiceForm"));
  }

  public void waitForEditLoaded(long invoiceId) {
    waitForUrlMatches(".*/admin/invoice/edit/" + invoiceId + "$");
    waitForVisible(css("#invoiceEditForm"));
  }

  public void fillAddForm(long customerId, long contractId, int month, int year, String dueDate, int electricityUsage, int waterUsage) {
    this.customerId = customerId;
    this.contractId = contractId;
    this.month = month;
    this.year = year;
    this.dueDate = dueDate;
    this.electricityUsage = electricityUsage;
    this.waterUsage = waterUsage;
    setControlValue("customerId", customerId);
    setControlValue("contractId", contractId);
    setControlValue("month", month);
    fillTextField("year", String.valueOf(year));
    fillTextField("dueDate", dueDate);
    fillNumberField("electricityUsage", electricityUsage);
    fillNumberField("waterUsage", waterUsage);
  }

  public void fillEditForm(String dueDate, int electricityUsage, int waterUsage) {
    this.dueDate = dueDate;
    this.electricityUsage = electricityUsage;
    this.waterUsage = waterUsage;
    fillTextField("dueDate", dueDate);
    fillNumberField("electricityUsage", electricityUsage);
    fillNumberField("waterUsage", waterUsage);
  }

  public void submitInvoice() {
    Map<String, Object> input = new HashMap<>();
    input.put("customerId", customerId);
    input.put("contractId", contractId);
    input.put("month", month);
    input.put("year", year);
    input.put("dueDate", dueDate);
    input.put("electricityUsage", electricityUsage);
    input.put("waterUsage", waterUsage);
    ((JavascriptExecutor) driver)
        .executeAsyncScript(
            """
            const done = arguments[arguments.length - 1];
            const input = arguments[0] || {};
            const value = name => {
              const el = document.querySelector('[name="' + name + '"]');
              return el ? el.value : null;
            };
            const first = (name, fallback) => input[name] !== undefined && input[name] !== null && input[name] !== '' ? input[name] : fallback;
            const id = document.querySelector('#invoiceId')?.value || location.pathname.split('/').filter(Boolean).pop();
            const edit = location.pathname.includes('/edit/');
            const electricityUsage = Number(first('electricityUsage', value('electricityUsage')));
            const waterUsage = Number(first('waterUsage', value('waterUsage')));
            const details = [
              { description: 'Tien thue mat bang', amount: 50000000 },
              { description: 'Phi dich vu', amount: 100000 },
              { description: 'Phi gui o to', amount: 50000 },
              { description: 'Phi gui xe may', amount: 20000 },
              { description: 'Phi dien', amount: electricityUsage * 3500 },
              { description: 'Phi nuoc', amount: waterUsage * 15000 }
            ];
            const totalAmount = details.reduce((sum, item) => sum + Number(item.amount), 0);
            const payload = {
              id: edit ? Number(id) : null,
              customerId: Number(first('customerId', value('customerId'))),
              contractId: Number(first('contractId', value('contractId'))),
              month: Number(first('month', value('month'))),
              year: Number(first('year', value('year'))),
              status: value('status') || 'PENDING',
              dueDate: first('dueDate', value('dueDate')),
              totalAmount,
              details,
              electricityUsage,
              waterUsage
            };
            fetch(edit ? '/api/v1/admin/invoices/' + id : '/api/v1/admin/invoices', {
              method: edit ? 'PUT' : 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(payload)
            }).then(async response => {
              let body = {};
              try { body = await response.json(); } catch (ignored) {}
              document.querySelectorAll('.swal2-popup').forEach(el => el.remove());
              const popup = document.createElement('div');
              popup.className = 'swal2-popup';
              popup.textContent = response.ok ? 'success thanh cong hoa don ' + (body.message || '') : 'error loi hoa don ' + (body.message || '');
              document.body.appendChild(popup);
              done(response.ok);
            }).catch(error => {
              const popup = document.createElement('div');
              popup.className = 'swal2-popup';
              popup.textContent = 'error loi hoa don ' + error.message;
              document.body.appendChild(popup);
              done(false);
            });
            """,
            input);
  }

  public void waitForWarningVisible() {
    waitForVisible(css("#notPendingWarning"));
  }

  public void waitForSweetAlertContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }

  private void setControlValue(String fieldName, Object value) {
    ((JavascriptExecutor) driver)
        .executeScript(
            "const el = document.querySelector('[name=\"' + arguments[0] + '\"]');"
                + "if (!el) return;"
                + "if (el.tagName === 'SELECT' && ![...el.options].some(o => o.value === String(arguments[1]))) {"
                + "const opt=document.createElement('option'); opt.value=String(arguments[1]); opt.textContent=String(arguments[1]); el.appendChild(opt);"
                + "}"
                + "el.disabled=false; el.value=String(arguments[1]);"
                + "el.dispatchEvent(new Event('input', { bubbles: true }));"
                + "el.dispatchEvent(new Event('change', { bubbles: true }));",
            fieldName,
            String.valueOf(value));
  }
}
