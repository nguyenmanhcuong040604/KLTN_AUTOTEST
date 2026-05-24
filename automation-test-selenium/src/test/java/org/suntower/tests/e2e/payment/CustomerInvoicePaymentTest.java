package org.suntower.tests.e2e.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.auth.AuthSessionHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedContract;
import org.suntower.fixtures.state.TestStateBuilder.InvoiceRecord;
import org.suntower.pages.customer.CustomerInvoicePage;
import org.suntower.pages.customer.CustomerPaymentQrPage;
import org.suntower.pages.core.NavigationPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CustomerInvoicePaymentTest extends BaseTest {
  private final Set<Long> cleanupInvoiceIds = new LinkedHashSet<>();
  private final Set<Long> cleanupContractIds = new LinkedHashSet<>();
  private final Set<Integer> cleanupCustomerIds = new LinkedHashSet<>();
  private final Set<Long> cleanupBuildingIds = new LinkedHashSet<>();
  private final Set<Long> cleanupStaffIds = new LinkedHashSet<>();
  private CreatedContract contract;
  private CustomerInvoicePage invoicePage;

  @BeforeMethod(alwaysRun = true)
  public void openInvoiceList() {
    contract = TestStateBuilder.createContract();
    cleanupContractIds.add(contract.id());
    cleanupStaffIds.add(contract.staff().id());
    cleanupCustomerIds.add(contract.customer().id());
    cleanupBuildingIds.add(contract.building().id());
    AuthSessionHelper.loginUiAndOpen(driver, contract.customer().username(), contract.customer().password(), "/customer/invoice/list");
    invoicePage = pageObjects.create(CustomerInvoicePage.class);
  }

  @AfterMethod(alwaysRun = true)
  public void cleanup() {
    cleanupInvoiceIds.forEach(TestStateBuilder::deleteInvoice);
    cleanupContractIds.forEach(TestStateBuilder::deleteContract);
    cleanupCustomerIds.forEach(TestStateBuilder::deleteCustomer);
    cleanupBuildingIds.forEach(TestStateBuilder::deleteBuilding);
    cleanupStaffIds.forEach(TestStateBuilder::deleteStaff);
  }

  @Test(groups = {"regression", "critical"}, description = "[E2E-CUS-PAY-001] should unpaid summary and payment details modal display when invoice list")
  public void shouldUnpaidSummaryAndPaymentDetailsModalDisplay() {
    InvoiceRecord invoice = arrangeInvoice();
    StepHelper.act(
        "perform invoice list behavior",
        () -> {
          new NavigationPage(driver).open("/customer/invoice/list");
          invoicePage.waitForLoaded();
          assertThat(invoicePage.readStats().unpaidCount()).isEqualTo("1");
          assertThat(invoicePage.firstInvoiceCardText()).contains(String.valueOf(invoice.id()), contract.building().name());
          invoicePage.openFirstPaymentModal();
          assertThat(invoicePage.visibleModalLooseText()).contains(normalized(contract.building().name()));
        });
    StepHelper.assertStep("verify unpaid summary and payment details modal display", () -> assertThat(TestStateBuilder.readInvoiceStatus(invoice.id())).isEqualTo("PENDING"));
  }

  @Test(groups = {"regression", "critical"}, description = "[E2E-CUS-PAY-002] should QR payment page redirection when payment modal")
  public void shouldRedirectToQrPaymentPage() {
    InvoiceRecord invoice = arrangeInvoice();
    CustomerPaymentQrPage qrPage = pageObjects.create(CustomerPaymentQrPage.class);
    StepHelper.act(
        "perform payment modal behavior",
        () -> {
          qrPage.open(invoice.id());
          qrPage.waitForLoaded(invoice.id());
          qrPage.waitForMetaContains("SUNTOWER INV " + invoice.id());
        });
    StepHelper.assertStep("verify QR payment page redirection", () -> assertThat(TestStateBuilder.readInvoiceStatus(invoice.id())).isEqualTo("PENDING"));
  }

  @Test(groups = {"regression", "critical"}, description = "[E2E-CUS-PAY-003] should invoice status update to paid when payment confirmation")
  public void shouldUpdateInvoiceStatusToPaid() {
    InvoiceRecord invoice = arrangeInvoice();
    CustomerPaymentQrPage qrPage = pageObjects.create(CustomerPaymentQrPage.class);
    StepHelper.act(
        "perform payment confirmation behavior",
        () -> {
          qrPage.open(invoice.id());
          qrPage.waitForLoaded(invoice.id());
          qrPage.confirmPayment();
        });
    StepHelper.assertStep("verify invoice status update to paid", () -> assertThat(TestStateBuilder.readInvoicePaymentState(invoice.id()).status()).isEqualTo("PAID"));
  }

  @Test(groups = {"regression", "critical"}, description = "[E2E-CUS-PAY-004] should no unpaid invoices display when empty state")
  public void shouldNoUnpaidInvoicesDisplay() {
    StepHelper.act(
        "perform empty state behavior",
        () -> {
          new NavigationPage(driver).open("/customer/invoice/list");
          invoicePage.waitForLoaded();
          invoicePage.waitForEmptyState();
        });
    StepHelper.assertStep("verify no unpaid invoices display", () -> assertThat(TestStateBuilder.contractExists(contract.id())).isTrue());
  }

  private InvoiceRecord arrangeInvoice() {
    InvoiceRecord invoice = TestStateBuilder.createInvoice(contract);
    cleanupInvoiceIds.add(invoice.id());
    return invoice;
  }

  private String normalized(String value) {
    return value.toLowerCase(java.util.Locale.ROOT);
  }
}
