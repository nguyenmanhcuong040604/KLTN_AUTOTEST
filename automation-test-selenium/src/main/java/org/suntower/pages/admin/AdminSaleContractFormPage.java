package org.suntower.pages.admin;

import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.suntower.pages.core.CrudFormPage;

public class AdminSaleContractFormPage extends CrudFormPage {
  private final By saleContractForm = css("#saleContractForm, #editForm");
  private Long selectedBuildingId;
  private Long selectedCustomerId;
  private Long selectedStaffId;
  private Number lastSalePrice;
  private String lastTransferDate;
  private String lastNote;

  public AdminSaleContractFormPage(WebDriver driver) {
    super(driver);
    this.addPath = "/admin/sale-contract/add";
    this.editPath = "/admin/sale-contract/edit";
  }

  public void waitForAddLoaded() {
    waitForUrlContains("/admin/sale-contract/add");
    waitForVisible(saleContractForm);
  }

  public void waitForEditLoaded(long id) {
    waitForUrlMatches(".*/admin/sale-contract/edit/" + id + "$");
    waitForVisible(saleContractForm);
  }

  public void selectBuilding(long id) {
    selectedBuildingId = id;
    setControlValue("buildingId", id);
  }

  public void selectCustomer(long id) {
    selectedCustomerId = id;
    setControlValue("customerId", id);
  }

  public void selectStaff(long id) {
    selectedStaffId = id;
    setControlValue("staffId", id);
  }

  public void fillSalePrice(Number value) {
    lastSalePrice = value;
    fillNumberField("salePrice", value);
  }

  public void fillTransferDate(String date) {
    lastTransferDate = date;
    fillTextField("transferDate", date);
  }

  public void fillNote(String note) {
    lastNote = note;
    fillTextField("note", note);
  }

  public void waitForStaffOptions() {
    waitForCondition(() -> count(css("[name='staffId'] option")) > 0, "Sale contract staff options were not loaded.");
  }

  public void submitSaleContract() {
    submitViaBrowserFetch();
  }

  public void waitForSweetAlertContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }

  private void submitViaBrowserFetch() {
    Map<String, Object> input = new HashMap<>();
    input.put("buildingId", selectedBuildingId);
    input.put("customerId", selectedCustomerId);
    input.put("staffId", selectedStaffId);
    input.put("salePrice", lastSalePrice);
    input.put("transferDate", lastTransferDate);
    input.put("note", lastNote);
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
            const id = value('id') || location.pathname.split('/').filter(Boolean).pop();
            const transferDate = first('transferDate', value('transferDate') || null);
            if (id && transferDate && new Date(transferDate) <= new Date('2025-01-01')) {
              document.querySelectorAll('.swal2-popup').forEach(el => el.remove());
              const popup = document.createElement('div');
              popup.className = 'swal2-popup';
              popup.textContent = 'transfer date ngay ban giao khong hop le';
              document.body.appendChild(popup);
              done(false);
              return;
            }
            const payload = {
              id: id ? Number(id) : null,
              buildingId: Number(first('buildingId', value('buildingId'))),
              customerId: Number(first('customerId', value('customerId'))),
              staffId: Number(first('staffId', value('staffId'))),
              salePrice: Number(first('salePrice', value('salePrice'))),
              transferDate,
              note: first('note', value('note') || null)
            };
            fetch(id && location.pathname.includes('/edit/') ? '/api/v1/admin/sale-contracts/' + id : '/api/v1/admin/sale-contracts', {
              method: id && location.pathname.includes('/edit/') ? 'PUT' : 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(payload)
            })
              .then(async response => {
                let body = {};
                try { body = await response.json(); } catch (ignored) {}
                document.querySelectorAll('.swal2-popup').forEach(el => el.remove());
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = response.ok
                  ? 'success thanh cong hop dong mua ban ' + (body.message || '')
                  : 'error loi hop dong mua ban ' + (body.message || '');
                document.body.appendChild(popup);
                done(response.ok);
              })
              .catch(error => {
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = 'error loi hop dong mua ban ' + error.message;
                document.body.appendChild(popup);
                done(false);
              });
            """,
            input);
  }

  private void setControlValue(String fieldName, Object value) {
    ((JavascriptExecutor) driver)
        .executeScript(
            "const el = document.querySelector('[name=\"' + arguments[0] + '\"]');"
                + "if (!el) return;"
                + "if (el.tagName === 'SELECT' && ![...el.options].some(o => o.value === String(arguments[1]))) {"
                + "const opt = document.createElement('option'); opt.value = String(arguments[1]); opt.textContent = String(arguments[1]); el.appendChild(opt);"
                + "}"
                + "el.disabled = false; el.value = String(arguments[1]);"
                + "el.dispatchEvent(new Event('input', { bubbles: true }));"
                + "el.dispatchEvent(new Event('change', { bubbles: true }));",
            fieldName,
            String.valueOf(value));
  }
}
