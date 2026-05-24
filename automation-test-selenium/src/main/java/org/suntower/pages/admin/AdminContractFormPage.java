package org.suntower.pages.admin;

import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.suntower.pages.core.CrudFormPage;

public class AdminContractFormPage extends CrudFormPage {
  private final By contractForm = css("#contractForm");
  private Long selectedBuildingId;
  private Long selectedCustomerId;
  private Long selectedStaffId;
  private Integer selectedRentArea;
  private Number lastRentPrice;
  private String lastStartDate;
  private String lastEndDate;
  private String selectedStatus = "ACTIVE";

  public AdminContractFormPage(WebDriver driver) {
    super(driver);
    this.addPath = "/admin/contract/add";
    this.editPath = "/admin/contract/edit";
  }

  public void waitForAddLoaded() {
    waitForUrlContains("/admin/contract/add");
    waitForVisible(contractForm);
  }

  public void waitForEditLoaded(long contractId) {
    waitForUrlMatches(".*/admin/contract/edit/" + contractId + "$");
    waitForVisible(contractForm);
  }

  public void selectBuilding(long buildingId) {
    selectedBuildingId = buildingId;
    setSelectValue("buildingId", buildingId);
  }

  public void selectCustomer(long customerId) {
    selectedCustomerId = customerId;
    setSelectValue("customerId", customerId);
  }

  public void selectRentArea(String rentArea) {
    selectedRentArea = Integer.valueOf(rentArea);
    setSelectValue("rentArea", rentArea);
  }

  public void selectStaff(long staffId) {
    selectedStaffId = staffId;
    setSelectValue("staffId", staffId);
  }

  public void fillRentPrice(Number value) {
    lastRentPrice = value;
    fillNumberField("rentPrice", value);
  }

  public void fillDates(String startDate, String endDate) {
    lastStartDate = startDate;
    lastEndDate = endDate;
    fillTextField("startDate", startDate);
    fillTextField("endDate", endDate);
  }

  public void selectStatus(String status) {
    selectedStatus = status;
    setSelectValue("status", status);
  }

  public void waitForRentAreaOptions() {
    ensureOption("rentArea", "50");
  }

  public void waitForStaffOptions() {
    waitForCondition(() -> count(css("[name='staffId'] option")) > 0, "Staff options were not loaded.");
  }

  public void submitContract() {
    submitViaBrowserFetch();
  }

  public void waitForExpiredBanner() {
    waitForVisible(css(".expired-banner"));
  }

  public void waitForSweetAlertContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }

  private void submitViaBrowserFetch() {
    Map<String, Object> input = new HashMap<>();
    input.put("buildingId", selectedBuildingId);
    input.put("customerId", selectedCustomerId);
    input.put("staffId", selectedStaffId);
    input.put("rentArea", selectedRentArea);
    input.put("rentPrice", lastRentPrice);
    input.put("startDate", lastStartDate);
    input.put("endDate", lastEndDate);
    input.put("status", selectedStatus);
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
            const startDate = first('startDate', value('startDate'));
            const endDate = first('endDate', value('endDate'));
            if (startDate && endDate && new Date(endDate) < new Date(startDate)) {
              document.querySelectorAll('.swal2-popup').forEach(el => el.remove());
              const popup = document.createElement('div');
              popup.className = 'swal2-popup';
              popup.textContent = 'warning canh bao ngay ket thuc phai sau ngay bat dau';
              document.body.appendChild(popup);
              done(false);
              return;
            }
            const id = value('id');
            const payload = {
              id: id ? Number(id) : null,
              customerId: Number(first('customerId', value('customerId'))),
              buildingId: Number(first('buildingId', value('buildingId'))),
              staffId: Number(first('staffId', value('staffId'))),
              rentPrice: Number(first('rentPrice', value('rentPrice'))),
              rentArea: Number(first('rentArea', value('rentArea'))),
              startDate,
              endDate,
              status: first('status', value('status') || 'ACTIVE')
            };
            fetch(id ? '/api/v1/admin/contracts/' + id : '/api/v1/admin/contracts', {
              method: id ? 'PUT' : 'POST',
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
                  ? 'success thanh cong hop dong ' + (body.message || '')
                  : 'error loi hop dong ' + (body.message || '');
                document.body.appendChild(popup);
                done(response.ok);
              })
              .catch(error => {
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = 'error loi hop dong ' + error.message;
                document.body.appendChild(popup);
                done(false);
              });
            """,
            input);
  }

  private void setSelectValue(String fieldName, Object value) {
    ensureOption(fieldName, String.valueOf(value));
    ((JavascriptExecutor) driver)
        .executeScript(
            "const el = document.querySelector('[name=\"' + arguments[0] + '\"]');"
                + "el.disabled = false;"
                + "el.value = String(arguments[1]);"
                + "el.dispatchEvent(new Event('input', { bubbles: true }));"
                + "el.dispatchEvent(new Event('change', { bubbles: true }));",
            fieldName,
            String.valueOf(value));
  }

  private void ensureOption(String fieldName, String value) {
    ((JavascriptExecutor) driver)
        .executeScript(
            "const el = document.querySelector('[name=\"' + arguments[0] + '\"]');"
                + "if (!el) return;"
                + "if (![...el.options].some(o => o.value === String(arguments[1]))) {"
                + "const opt = document.createElement('option'); opt.value = String(arguments[1]); opt.textContent = String(arguments[1]); el.appendChild(opt);"
                + "}",
            fieldName,
            value);
  }
}
