package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.suntower.pages.core.CrudFormPage;

public class AdminStaffFormPage extends CrudFormPage {
  private final By staffForm = css("#staffForm");

  public AdminStaffFormPage(WebDriver driver) {
    super(driver);
    this.addPath = "/admin/staff/add";
  }

  public void waitForLoaded() {
    waitForUrlContains("/admin/staff/add");
    waitForVisible(staffForm);
  }

  public void fillStaffBasics(StaffBasics data) {
    if (data.fullName() != null) fillTextField("fullName", data.fullName());
    if (data.email() != null) fillTextField("email", data.email());
    if (data.phone() != null) fillTextField("phone", data.phone());
    if (data.username() != null) fillTextField("username", data.username());
    if (data.password() != null) fillTextField("password", data.password());
  }

  public void selectRole(String role) {
    check(css("ADMIN".equals(role) ? "#roleAdmin" : "#roleStaff"));
  }

  public void waitForSweetAlertContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }

  public record StaffBasics(String fullName, String email, String phone, String username, String password) {}
}
