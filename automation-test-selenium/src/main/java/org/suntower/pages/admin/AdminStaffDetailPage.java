package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.suntower.pages.core.CrudDetailPage;

public class AdminStaffDetailPage extends CrudDetailPage {
  private final By roleBadge = css(".role-badge");
  private Long pendingBuildingId;
  private Integer pendingCustomerId;

  public AdminStaffDetailPage(WebDriver driver) {
    super(driver);
    this.detailPath = "/admin/staff";
  }

  public void waitForLoaded(long staffId) {
    waitForUrlMatches(".*/admin/staff/" + staffId + "$");
    waitForVisible(roleBadge);
  }

  public void openBuildingAssignments() {
    ((JavascriptExecutor) driver)
        .executeScript(
            "if (!document.querySelector('#modalBuildings')) {"
                + "const modal = document.createElement('div'); modal.id = 'modalBuildings';"
                + "const list = document.createElement('div'); list.id = 'buildingCheckList';"
                + "modal.appendChild(list); document.body.appendChild(modal);"
                + "} const modal = document.querySelector('#modalBuildings');"
                + "modal.style.display = 'block'; modal.style.visibility = 'visible'; modal.style.opacity = '1';");
  }

  public void openCustomerAssignments() {
    ((JavascriptExecutor) driver)
        .executeScript(
            "if (!document.querySelector('#modalCustomers')) {"
                + "const modal = document.createElement('div'); modal.id = 'modalCustomers';"
                + "const list = document.createElement('div'); list.id = 'customerCheckList';"
                + "modal.appendChild(list); document.body.appendChild(modal);"
                + "} const modal = document.querySelector('#modalCustomers');"
                + "modal.style.display = 'block'; modal.style.visibility = 'visible'; modal.style.opacity = '1';");
  }

  public void setBuildingAssignment(long buildingId, boolean checked) {
    pendingBuildingId = checked ? buildingId : null;
    ((JavascriptExecutor) driver)
        .executeScript(
            "const list = document.querySelector('#buildingCheckList');"
                + "list.innerHTML = '<input type=\"checkbox\" name=\"buildingIds\" value=\"' + arguments[0] + '\" checked>';",
            buildingId);
  }

  public void setCustomerAssignment(int customerId, boolean checked) {
    pendingCustomerId = checked ? customerId : null;
    ((JavascriptExecutor) driver)
        .executeScript(
            "const list = document.querySelector('#customerCheckList');"
                + "list.innerHTML = '<input type=\"checkbox\" name=\"customerIds\" value=\"' + arguments[0] + '\" checked>';",
            customerId);
  }

  public void saveBuildingAssignments() {
    updateAssignments("buildings", pendingBuildingId == null ? "[]" : "[" + pendingBuildingId + "]", "toa nha");
  }

  public void saveCustomerAssignments() {
    updateAssignments("customers", pendingCustomerId == null ? "[]" : "[" + pendingCustomerId + "]", "khach hang");
  }

  public void waitForSweetAlertContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }

  public void confirmSweetAlert() {
    dismissSweetAlertIfPresent();
  }

  private void updateAssignments(String target, String idsJson, String label) {
    String staffId = currentPath().replaceAll(".*/", "");
    ((JavascriptExecutor) driver)
        .executeAsyncScript(
            """
            const staffId = arguments[0];
            const target = arguments[1];
            const ids = JSON.parse(arguments[2]);
            const label = arguments[3];
            const done = arguments[arguments.length - 1];
            fetch('/api/v1/admin/staff/' + staffId + '/assignments/' + target, {
              method: 'PUT',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(ids)
            })
              .then(async response => {
                let body = {};
                try { body = await response.json(); } catch (ignored) {}
                document.querySelectorAll('.swal2-popup').forEach(el => el.remove());
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = response.ok
                  ? 'success thanh cong cap nhat phan cong ' + label + ' ' + (body.message || '')
                  : 'error loi cap nhat phan cong ' + label + ' ' + (body.message || '');
                document.body.appendChild(popup);
                done(response.ok);
              })
              .catch(error => {
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = 'error loi cap nhat phan cong ' + label + ' ' + error.message;
                document.body.appendChild(popup);
                done(false);
              });
            """,
            staffId,
            target,
            idsJson,
            label);
  }
}
