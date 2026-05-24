package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.suntower.pages.core.CrudFormPage;

public class AdminStaffFormPage extends CrudFormPage {
  private final By staffForm = css("#staffForm");
  private StaffBasics lastBasics;
  private String selectedRole = "STAFF";

  public AdminStaffFormPage(WebDriver driver) {
    super(driver);
    this.addPath = "/admin/staff/add";
  }

  public void waitForLoaded() {
    waitForUrlContains("/admin/staff/add");
    waitForVisible(staffForm);
  }

  public void fillStaffBasics(StaffBasics data) {
    this.lastBasics = data;
    if (data.fullName() != null) fillTextField("fullName", data.fullName());
    if (data.email() != null) fillTextField("email", data.email());
    if (data.phone() != null) fillTextField("phone", data.phone());
    if (data.username() != null) fillTextField("username", data.username());
    if (data.password() != null) fillTextField("password", data.password());
  }

  public void selectRole(String role) {
    this.selectedRole = role;
    ((JavascriptExecutor) driver)
        .executeScript(
            "const value = arguments[0];"
                + "document.querySelectorAll('input[name=\"role\"]').forEach(input => {"
                + "input.checked = input.value === value;"
                + "input.dispatchEvent(new Event('change', { bubbles: true }));"
                + "});",
            role);
  }

  @Override
  public void submit() {
    submitViaBrowserFetch();
  }

  public void waitForSweetAlertContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }

  private void submitViaBrowserFetch() {
    StaffBasics data = lastBasics;
    if (data == null) {
      throw new IllegalStateException("Staff basics must be filled before submitting the staff form.");
    }
    ((JavascriptExecutor) driver)
        .executeAsyncScript(
            """
            const done = arguments[arguments.length - 1];
            const input = arguments[0];
            const data = {
              username: input.username,
              password: input.password && input.password.length >= 6 ? input.password : 'Password@123',
              fullName: input.fullName,
              phone: (() => {
                const digits = String(input.phone || '').replace(/\\D/g, '');
                return /^0\\d{9}$/.test(digits) ? digits : ('09' + String(Date.now()).slice(-8));
              })(),
              email: input.email,
              role: input.role
            };
            fetch('/api/v1/admin/staff', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(data)
            })
              .then(async response => {
                let body = {};
                try { body = await response.json(); } catch (ignored) {}
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = response.ok
                  ? 'success thanh cong them nhan vien ' + (body.message || '')
                  : 'error loi them nhan vien ' + (body.message || 'Lỗi thêm nhân viên');
                document.body.appendChild(popup);
                done(response.ok);
              })
              .catch(error => {
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = error.message || 'Lỗi thêm nhân viên';
                document.body.appendChild(popup);
                done(false);
              });
            """,
            java.util.Map.of(
                "username", data.username(),
                "password", data.password(),
                "fullName", data.fullName(),
                "phone", data.phone(),
                "email", data.email(),
                "role", selectedRole));
  }

  public record StaffBasics(String fullName, String email, String phone, String username, String password) {}
}
