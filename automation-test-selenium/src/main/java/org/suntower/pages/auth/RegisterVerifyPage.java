package org.suntower.pages.auth;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class RegisterVerifyPage extends BasePage {
  private final By otpInput = css("input[name='otp']");
  private final By verifyButton =
      anyCss(
          "[data-testid='register-verify-submit']",
          "form[action='/auth/register/verify'] button[type='submit']",
          "button.submit",
          "button[type='submit']");
  private final By body = css("body");

  public RegisterVerifyPage(WebDriver driver) {
    super(driver);
  }

  public void open(String email) {
    visit("/register/verify?email=" + urlEncode(email));
  }

  public void waitForLoaded() {
    waitForLoaded(null);
  }

  public void waitForLoaded(String email) {
    waitForUrlContains("/register/verify");
    waitForVisible(otpInput);
    waitForVisible(verifyButton);
    dismissSweetAlertIfPresent();
    if (email != null && !email.isBlank()) {
      waitForText(body, email);
    }
  }

  public void verifyOtp(String otp) {
    dismissSweetAlertIfPresent();
    fill(otpInput, otp);
    click(verifyButton);
  }

  public void waitForPopupContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }
}
