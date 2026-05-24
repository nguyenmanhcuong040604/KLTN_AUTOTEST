package org.suntower.pages.customer;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class CustomerInvoicePage extends BasePage {
  private final By emptyState = css("[data-testid='customer-invoice-empty'], .empty-state");
  private final By statValues = css(".stat-value");
  private final By invoiceCards = css("[data-testid='customer-invoice-card'], .invoice-card");
  private final By paymentButtons = css("[data-testid='customer-pay-button'], .pay-btn, .btn-payment, [data-bs-target^='#paymentModal']");
  private final By visibleModal = css(".modal.show");

  public CustomerInvoicePage(WebDriver driver) {
    super(driver);
  }

  public void waitForLoaded() {
    waitForUrlContains("/customer/invoice/list");
    waitForVisible(anyCss("h1", "h2", ".empty-state", ".invoice-card"));
  }

  public InvoiceStats readStats() {
    return new InvoiceStats(driver.findElements(statValues).get(0).getText().trim(), driver.findElements(statValues).get(1).getText().trim());
  }

  public String firstInvoiceCardText() {
    return firstVisible(invoiceCards).getText();
  }

  public void openFirstPaymentModal() {
    click(firstVisible(paymentButtons));
  }

  public String visibleModalLooseText() {
    return normalizeLooseText(text(visibleModal));
  }

  public void confirmPaymentInModal() {
    click(firstVisible(css(".modal.show .btn-payment")));
  }

  public void continueSweetAlertRedirect() {
    if (isVisible(css(".swal2-confirm"))) {
      click(css(".swal2-confirm"));
    } else {
      ((JavascriptExecutor) driver).executeScript("window.location.href = document.querySelector('.modal.show .btn-payment').getAttribute('onclick').match(/\\d+/)[0];");
    }
  }

  public void waitForPaymentSuccessAlert() {
    waitForSweetAlertContainsText("thanh toan thanh cong|success");
  }

  public void waitForEmptyState() {
    waitForVisible(emptyState);
  }

  public record InvoiceStats(String unpaidCount, String totalPayable) {}
}
