package org.suntower.tests.e2e.staff;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.auth.AuthSessionHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedContract;
import org.suntower.fixtures.state.TestStateBuilder.InvoicePeriod;
import org.suntower.fixtures.state.TestStateBuilder.InvoiceRecord;
import org.suntower.pages.core.NavigationPage;
import org.suntower.pages.staff.StaffInvoiceListPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StaffInvoiceListTest extends BaseTest {
  private final Set<Long> cleanupInvoiceIds = new LinkedHashSet<>();
  private final Set<Long> cleanupContractIds = new LinkedHashSet<>();
  private final Set<Integer> cleanupCustomerIds = new LinkedHashSet<>();
  private final Set<Long> cleanupBuildingIds = new LinkedHashSet<>();
  private final Set<Long> cleanupStaffIds = new LinkedHashSet<>();
  private CreatedContract contract;
  private StaffInvoiceListPage listPage;

  @BeforeMethod(alwaysRun = true)
  public void openInvoiceList() {
    contract = TestStateBuilder.createContract();
    cleanupContractIds.add(contract.id());
    cleanupStaffIds.add(contract.staff().id());
    cleanupCustomerIds.add(contract.customer().id());
    cleanupBuildingIds.add(contract.building().id());

    AuthSessionHelper.loginUiAndOpen(driver, contract.staff().username(), contract.staff().password(), "/staff/invoices");
    listPage = pageObjects.create(StaffInvoiceListPage.class);
    listPage.waitForLoaded();
  }

  @AfterMethod(alwaysRun = true)
  public void cleanup() {
    cleanupInvoiceIds.forEach(TestStateBuilder::deleteInvoice);
    cleanupInvoiceIds.clear();
    cleanupContractIds.forEach(TestStateBuilder::deleteContract);
    cleanupContractIds.clear();
    cleanupCustomerIds.forEach(TestStateBuilder::deleteCustomer);
    cleanupCustomerIds.clear();
    cleanupBuildingIds.forEach(TestStateBuilder::deleteBuilding);
    cleanupBuildingIds.clear();
    cleanupStaffIds.forEach(TestStateBuilder::deleteStaff);
    cleanupStaffIds.clear();
    contract = null;
  }

  @Test(groups = {"regression"}, description = "[E2E-STF-INV-001] should assigned invoice rows and detail modal when invoice list")
  public void shouldShowAssignedInvoiceRowsAndDetailModal() {
    InvoiceRecord invoice = arrangeInvoice();

    StepHelper.act(
        "perform invoice list behavior",
        () -> {
          reopenInvoices();
          listPage.selectFilter("status", "PENDING");
          listPage.search();
          assertThat(listPage.rowByInvoiceId(invoice.id()).getText()).contains(contract.customer().fullName());
          listPage.openViewModal(invoice.id());
          listPage.waitForVisibleModalContains(contract.building().name());
        });

    StepHelper.assertStep("verify assigned invoice rows and detail modal", () -> assertThat(TestStateBuilder.invoiceExists(invoice.id())).isTrue());
  }

  @Test(groups = {"regression"}, description = "[E2E-STF-INV-002] should create invoice from add modal when invoice creation")
  public void shouldCreateInvoiceFromAddModal() {
    InvoicePeriod period = TestStateBuilder.previousInvoicePeriod();

    StepHelper.act(
        "perform invoice creation behavior",
        () -> {
          listPage.openAddInvoiceModal();
          listPage.selectAddCustomer(contract.customer().id());
          listPage.selectAddContract(contract.id());
          listPage.fillAddInvoiceForm(period.month(), period.year(), period.dueDate(), 25, 9);
          listPage.chooseAddStatus("PENDING");
          listPage.submitAddInvoice();
        });

    StepHelper.assertStep(
        "verify create invoice from add modal",
        () -> {
          InvoiceRecord created = TestStateBuilder.findCreatedInvoiceForPeriod(contract, period);
          assertThat(created).isNotNull();
          cleanupInvoiceIds.add(created.id());
        });
  }

  @Test(groups = {"regression"}, description = "[E2E-STF-INV-003] should business error display when duplicate invoice")
  public void shouldShowBusinessErrorForDuplicateInvoice() {
    InvoiceRecord existingInvoice = arrangeInvoice();

    StepHelper.act(
        "perform duplicate invoice behavior",
        () -> {
          listPage.openAddInvoiceModal();
          listPage.selectAddCustomer(contract.customer().id());
          listPage.selectAddContract(contract.id());
          listPage.fillAddInvoiceForm(existingInvoice.month(), existingInvoice.year(), TestStateBuilder.previousInvoicePeriod().dueDate(), 18, 7);
          listPage.submitAddInvoice();
        });

    StepHelper.assertStep(
        "verify business error display",
        () -> assertThat(TestStateBuilder.invoicePeriodCount(contract, existingInvoice)).isEqualTo(1));
  }

  @Test(groups = {"regression"}, description = "[E2E-STF-INV-004] should usage due date and status update when invoice edit")
  public void shouldUpdateUsageDueDateAndStatus() {
    InvoiceRecord invoice = arrangeInvoice();
    String updatedDueDate = nextMonthDueDate(invoice.month(), invoice.year());

    StepHelper.act(
        "perform invoice edit behavior",
        () -> {
          reopenInvoices();
          listPage.waitForTableData();
          listPage.openEditModal(invoice.id());
          listPage.fillVisibleEditForm(updatedDueDate, 33, 11, "PAID");
          listPage.saveVisibleEditForm();
          listPage.waitForSweetAlertContains("thanh cong|success");
        });

    StepHelper.assertStep(
        "verify usage due date and status update",
        () -> assertThat(TestStateBuilder.readInvoiceEditState(invoice.id())).isEqualTo("PAID|" + updatedDueDate));
  }

  @Test(groups = {"regression"}, description = "[E2E-STF-INV-005] should owned invoice deletion from list when invoice deletion")
  public void shouldDeleteOwnedInvoiceFromList() {
    InvoiceRecord invoice = arrangeInvoice();

    StepHelper.act(
        "perform invoice deletion behavior",
        () -> {
          reopenInvoices();
          listPage.waitForTableData();
          listPage.deleteInvoice(invoice.id());
          listPage.confirmSweetAlert();
          listPage.waitForSweetAlertContains("thanh cong|success");
        });

    StepHelper.assertStep(
        "verify owned invoice deletion from list",
        () -> {
          assertThat(TestStateBuilder.invoiceExists(invoice.id())).isFalse();
          cleanupInvoiceIds.remove(invoice.id());
        });
  }

  private InvoiceRecord arrangeInvoice() {
    InvoiceRecord invoice = TestStateBuilder.createInvoice(contract);
    cleanupInvoiceIds.add(invoice.id());
    return invoice;
  }

  private void reopenInvoices() {
    new NavigationPage(driver).open("/staff/invoices");
    listPage.waitForLoaded();
  }

  private String nextMonthDueDate(int month, int year) {
    int dueMonth = month == 12 ? 1 : month + 1;
    int dueYear = month == 12 ? year + 1 : year;
    return "%04d-%02d-20".formatted(dueYear, dueMonth);
  }
}
