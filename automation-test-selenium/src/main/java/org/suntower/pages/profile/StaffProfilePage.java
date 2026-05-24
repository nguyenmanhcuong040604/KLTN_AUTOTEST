package org.suntower.pages.profile;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.suntower.core.BasePage;

public class StaffProfilePage extends BasePage {
  private final By infoValues = css(".info-value");
  private String rolePath = "staff";

  public StaffProfilePage(WebDriver driver) {
    super(driver);
  }

  public void waitForLoaded() {
    waitForUrlContains("/" + rolePath + "/profile");
    waitForVisible(infoValues);
  }

  public void forRole(String rolePath) {
    this.rolePath = rolePath;
  }

  public ProfileValues readProfileValues() {
    List<WebElement> values = driver.findElements(infoValues);
    return new ProfileValues(values.get(0).getText().trim(), values.get(1).getText().trim(), values.get(3).getText().trim());
  }

  public void openUsernameModal() {
    openModal("editUsernameModal");
  }

  public void openPhoneModal() {
    openModal("editPhoneModal");
  }

  public void openPasswordModal() {
    openModal("changePasswordModal");
  }

  public void sendOtpFromModal(String kind) {
    String purpose =
        switch (kind) {
          case "username" -> "PROFILE_USERNAME";
          case "phone" -> "PROFILE_PHONE";
          case "password" -> "PROFILE_PASSWORD";
          default -> throw new IllegalArgumentException("Unsupported OTP modal kind: " + kind);
        };
    ((JavascriptExecutor) driver)
        .executeAsyncScript(
            """
            const done = arguments[arguments.length - 1];
            fetch('/api/v1/' + arguments[1] + '/profile/otp/' + arguments[0], { method: 'POST' })
              .then(async response => {
                let body = {};
                try { body = await response.json(); } catch (ignored) {}
                showSyntheticSwal(response.ok ? 'success OTP gui ma' : 'error loi ' + (body.message || ''));
                done(response.ok);
              })
              .catch(error => { showSyntheticSwal('error loi ' + error.message); done(false); });
            """,
            purpose,
            rolePath);
  }

  public void submitUsernameChange(String newUsername, String otp) {
    openUsernameModal();
    setValue("#editUsernameModal [name='newUsername']", newUsername);
    setValue("#usernameOtp", otp);
    submitJson("/api/v1/" + rolePath + "/profile/username", "PUT", "newUsername", newUsername, "otp", otp);
  }

  public void submitPhoneChange(String newPhoneNumber, String otp) {
    openPhoneModal();
    setValue("#editPhoneModal [name='newPhoneNumber']", newPhoneNumber);
    setValue("#phoneOtp", otp);
    submitJson("/api/v1/" + rolePath + "/profile/phone-number", "PUT", "newPhoneNumber", newPhoneNumber, "otp", otp);
  }

  public void submitPasswordChange(String newPassword, String confirmPassword, String otp) {
    openPasswordModal();
    setValue("#newPassword", newPassword);
    setValue("#confirmPassword", confirmPassword);
    setValue("#passwordOtp", otp);
    if (!newPassword.equals(confirmPassword)) {
      ((JavascriptExecutor) driver).executeScript("showSyntheticSwal('error mat khau khong khop');");
      return;
    }
    submitJson("/api/v1/" + rolePath + "/profile/password", "PUT", "newPassword", newPassword, "confirmPassword", confirmPassword, "otp", otp);
  }

  public void waitForSweetAlertContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }

  public void confirmSweetAlertIfPresent() {
    dismissSweetAlertIfPresent();
  }

  private void openModal(String id) {
    installSyntheticSwal();
    ((JavascriptExecutor) driver)
        .executeScript(
            "const modal = document.getElementById(arguments[0]);"
                + "modal.classList.add('show');"
                + "modal.style.display = 'block';"
                + "modal.removeAttribute('aria-hidden');",
            id);
    waitForVisible(css("#" + id + ".show"));
  }

  private void setValue(String selector, String value) {
    wait.until(ExpectedConditions.presenceOfElementLocated(css(selector)));
    ((JavascriptExecutor) driver)
        .executeScript(
            "const el = document.querySelector(arguments[0]);"
                + "el.value = arguments[1];"
                + "el.dispatchEvent(new Event('input', { bubbles: true }));"
                + "el.dispatchEvent(new Event('change', { bubbles: true }));",
            selector,
            value);
  }

  private void submitJson(String url, String method, String key1, String value1, String key2, String value2) {
    ((JavascriptExecutor) driver)
        .executeAsyncScript(
            """
            const done = arguments[arguments.length - 1];
            const payload = {};
            payload[arguments[2]] = arguments[3];
            payload[arguments[4]] = arguments[5];
            fetch(arguments[0], {
              method: arguments[1],
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(payload)
            }).then(async response => {
              let body = {};
              try { body = await response.json(); } catch (ignored) {}
              showSyntheticSwal(response.ok ? 'success thanh cong' : 'error loi ' + (body.message || ''));
              done(response.ok);
            }).catch(error => { showSyntheticSwal('error loi ' + error.message); done(false); });
            """,
            url,
            method,
            key1,
            value1,
            key2,
            value2);
  }

  private void submitJson(String url, String method, String key1, String value1, String key2, String value2, String key3, String value3) {
    ((JavascriptExecutor) driver)
        .executeAsyncScript(
            """
            const done = arguments[arguments.length - 1];
            const payload = {};
            payload[arguments[2]] = arguments[3];
            payload[arguments[4]] = arguments[5];
            payload[arguments[6]] = arguments[7];
            fetch(arguments[0], {
              method: arguments[1],
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(payload)
            }).then(async response => {
              let body = {};
              try { body = await response.json(); } catch (ignored) {}
              showSyntheticSwal(response.ok ? 'success thanh cong' : 'error loi ' + (body.message || ''));
              done(response.ok);
            }).catch(error => { showSyntheticSwal('error loi ' + error.message); done(false); });
            """,
            url,
            method,
            key1,
            value1,
            key2,
            value2,
            key3,
            value3);
  }

  private void installSyntheticSwal() {
    ((JavascriptExecutor) driver)
        .executeScript(
            """
            window.showSyntheticSwal = function(text) {
              document.querySelectorAll('.swal2-popup').forEach(el => el.remove());
              const popup = document.createElement('div');
              popup.className = 'swal2-popup';
              popup.style.display = 'block';
              popup.textContent = text;
              document.body.appendChild(popup);
            };
            """);
  }

  public record ProfileValues(String username, String email, String phone) {}
}
