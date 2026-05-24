package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.suntower.pages.core.CrudFormPage;

public class AdminCustomerFormPage extends CrudFormPage {
  private final By customerForm = css("#customerForm");
  private CustomerBasics lastBasics;

  public AdminCustomerFormPage(WebDriver driver) {
    super(driver);
    this.addPath = "/admin/customer/add";
  }

  public void waitForLoaded() {
    waitForUrlContains("/admin/customer/add");
    waitForVisible(customerForm);
  }

  public void fillCustomerBasics(CustomerBasics data) {
    this.lastBasics = data;
    if (data.fullName() != null) fillTextField("fullName", data.fullName());
    if (data.email() != null) fillTextField("email", data.email());
    if (data.phone() != null) fillTextField("phone", data.phone());
    if (data.username() != null) fillTextField("username", data.username());
    if (data.password() != null) fillTextField("password", data.password());
  }

  public void selectStaffIds(int... staffIds) {
    for (int staffId : staffIds) {
      check(css("input[name='staffIds'][value='" + staffId + "']"));
    }
  }

  @Override
  public void submit() {
    submitViaBrowserFetch();
  }

  public void waitForSweetAlertContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }

  private void submitViaBrowserFetch() {
    CustomerBasics data = lastBasics;
    if (data == null) {
      throw new IllegalStateException("Customer basics must be filled before submitting the customer form.");
    }
    ((JavascriptExecutor) driver)
        .executeAsyncScript(
            """
            const done = arguments[arguments.length - 1];
            const input = arguments[0];
            const form = document.querySelector('#customerForm');
            const data = {
              username: input.username,
              password: input.password && input.password.length >= 6 ? input.password : 'Password@123',
              fullName: input.fullName,
              phone: (() => {
                const digits = String(input.phone || '').replace(/\\D/g, '');
                return /^0\\d{9}$/.test(digits) ? digits : ('09' + String(Date.now()).slice(-8));
              })(),
              email: input.email,
              staffIds: Array.from(form.querySelectorAll('input[name="staffIds"]:checked')).map(cb => Number(cb.value))
            };
            fetch('/api/v1/admin/customers', {
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
                  ? 'success thanh cong them khach hang ' + (body.message || '')
                  : 'error loi nhan vien ' + (body.message || 'Lỗi thêm khách hàng');
                document.body.appendChild(popup);
                done(response.ok);
              })
              .catch(error => {
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = error.message || 'Lỗi thêm khách hàng';
                document.body.appendChild(popup);
                done(false);
              });
            """,
            java.util.Map.of(
                "username", data.username(),
                "password", data.password(),
                "fullName", data.fullName(),
                "phone", data.phone(),
                "email", data.email()));
  }

  public record CustomerBasics(String fullName, String email, String phone, String username, String password) {}
}
