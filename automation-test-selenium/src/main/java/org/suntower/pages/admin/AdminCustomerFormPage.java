package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.suntower.pages.core.CrudFormPage;

public class AdminCustomerFormPage extends CrudFormPage {
  private final By customerForm = css("#customerForm");

  public AdminCustomerFormPage(WebDriver driver) {
    super(driver);
    this.addPath = "/admin/customer/add";
  }

  public void waitForLoaded() {
    waitForUrlContains("/admin/customer/add");
    waitForVisible(customerForm);
  }

  public void fillCustomerBasics(CustomerBasics data) {
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

  public void waitForSweetAlertContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }

  public record CustomerBasics(String fullName, String email, String phone, String username, String password) {}
}
