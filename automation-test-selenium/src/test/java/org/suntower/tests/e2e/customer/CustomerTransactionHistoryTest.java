package org.suntower.tests.e2e.customer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.auth.AuthSessionHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedContract;
import org.suntower.fixtures.state.TestStateBuilder.InvoiceRecord;
import org.suntower.pages.customer.CustomerTransactionHistoryPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CustomerTransactionHistoryTest extends BaseTest {
  private final Set<Long> cleanupInvoiceIds = new LinkedHashSet<>();
  private final Set<Long> cleanupContractIds = new LinkedHashSet<>();
  private final Set<Integer> cleanupCustomerIds = new LinkedHashSet<>();
  private final Set<Long> cleanupBuildingIds = new LinkedHashSet<>();
  private final Set<Long> cleanupStaffIds = new LinkedHashSet<>();
  private CreatedContract contract;
  private InvoiceRecord invoice;
  private CustomerTransactionHistoryPage page;

  @BeforeMethod(alwaysRun = true)
  public void openHistory() {
    contract = TestStateBuilder.createContract();
    invoice = TestStateBuilder.createInvoice(contract);
    TestStateBuilder.markInvoicePaidByBankQr(invoice.id());
    cleanupInvoiceIds.add(invoice.id());
    cleanupContractIds.add(contract.id());
    cleanupStaffIds.add(contract.staff().id());
    cleanupCustomerIds.add(contract.customer().id());
    cleanupBuildingIds.add(contract.building().id());
    AuthSessionHelper.loginUiAndOpen(driver, contract.customer().username(), contract.customer().password(), "/customer/transaction/history");
    page = pageObjects.create(CustomerTransactionHistoryPage.class);
    page.waitForLoaded();
  }

  @AfterMethod(alwaysRun = true)
  public void cleanup() {
    cleanupInvoiceIds.forEach(TestStateBuilder::deleteInvoice);
    cleanupContractIds.forEach(TestStateBuilder::deleteContract);
    cleanupCustomerIds.forEach(TestStateBuilder::deleteCustomer);
    cleanupBuildingIds.forEach(TestStateBuilder::deleteBuilding);
    cleanupStaffIds.forEach(TestStateBuilder::deleteStaff);
  }

  @Test(groups = {"regression"}, description = "[E2E-CUS-TXN-001] should paid transaction summary and invoice detail display when transaction summary")
  public void shouldPaidTransactionSummaryAndDetailDisplay() {
    StepHelper.act(
        "perform transaction summary behavior",
        () -> {
          page.waitForSummaryVisible();
          page.waitForResultCountBanner(1);
          assertThat(page.rowByBuildingName(contract.building().name()).isDisplayed()).isTrue();
          page.openTransactionDetail(contract.building().name());
          page.waitForDetailModalContains(contract.building().name());
          page.closeDetailModal();
        });
    StepHelper.assertStep(
        "verify paid transaction summary and invoice detail display",
        () -> {
          TestStateBuilder.InvoicePaymentState state = TestStateBuilder.readInvoicePaymentState(invoice.id());
          assertThat(state.status()).isEqualTo("PAID");
          assertThat(state.paymentMethod()).isEqualTo("BANK_QR");
          assertThat(state.transactionCode()).isEqualTo("E2E-TX-" + invoice.id());
        });
  }

  @Test(groups = {"regression"}, description = "[E2E-CUS-TXN-002] should invoice period filtering when period filter")
  public void shouldFilterByInvoicePeriod() {
    StepHelper.act(
        "perform period filter behavior",
        () -> {
          page.filterByMonth(invoice.month());
          page.filterByYear(invoice.year());
          page.submitFilters();
          page.waitForResultCountBanner(1);
          assertThat(page.rowByBuildingName(contract.building().name()).isDisplayed()).isTrue();
        });
    StepHelper.assertStep("verify invoice period filtering", () -> assertThat(TestStateBuilder.paidInvoicePeriodCount(invoice.id(), invoice.month(), invoice.year())).isEqualTo(1));
  }

  @Test(groups = {"regression"}, description = "[E2E-CUS-TXN-003] should empty state and filter reset when filter reset")
  public void shouldShowEmptyStateAndResetFilters() {
    StepHelper.act(
        "perform filter reset behavior",
        () -> {
          page.filterByMonth(invoice.month() == 12 ? 1 : invoice.month() + 1);
          page.filterByYear(invoice.year());
          page.submitFilters();
          page.waitForEmptyState();
          page.waitForPaginationHidden();
          page.resetFilters();
          page.submitFilters();
          page.waitForResultCountBanner(1);
        });
    StepHelper.assertStep("verify empty state and filter reset", () -> assertThat(TestStateBuilder.invoiceExists(invoice.id())).isTrue());
  }
}
