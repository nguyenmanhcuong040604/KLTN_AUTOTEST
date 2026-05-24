package org.suntower.pages.customer;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class CustomerPaymentQrPage extends BasePage {
  private final By invoicePill = css(".invoice-pill");
  private final By qrImage = css(".qr-box img");
  private final By metaGrid = css(".meta-grid");

  public CustomerPaymentQrPage(WebDriver driver) {
    super(driver);
  }

  public void open(long invoiceId) {
    visit("/payment-demo/qr/" + invoiceId);
  }

  public void waitForLoaded(long invoiceId) {
    waitForUrlContains("/payment-demo/qr/" + invoiceId);
    waitForText(invoicePill, String.valueOf(invoiceId));
    waitForVisible(qrImage);
  }

  public void waitForMetaContains(String text) {
    waitForText(metaGrid, text);
  }

  public void confirmPayment() {
    click(firstVisible(css("button.btn-confirm, button.btn-primary, button[type='submit']")));
  }
}
