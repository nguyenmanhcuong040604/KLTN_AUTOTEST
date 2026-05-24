package org.suntower.tests.e2e.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedContract;
import org.suntower.fixtures.state.TestStateBuilder.InvoicePeriod;
import org.suntower.fixtures.state.TestStateBuilder.InvoiceRecord;
import org.suntower.pages.admin.AdminInvoiceDetailPage;
import org.suntower.pages.admin.AdminInvoiceFormPage;
import org.suntower.pages.admin.AdminInvoiceListPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AdminInvoiceManagementTest extends BaseTest {
  private final Set<Long> cleanupInvoiceIds = new LinkedHashSet<>();
  private CreatedContract contract;
  private AdminInvoiceListPage listPage;

  @BeforeMethod(alwaysRun = true)
  public void openInvoiceList() {
    contract = TestStateBuilder.createContract();
    adminSession.open("/admin/invoice/list");
    listPage = pageObjects.create(AdminInvoiceListPage.class);
    listPage.waitForLoaded();
  }

  @AfterMethod(alwaysRun = true)
  public void cleanup() {
    cleanupInvoiceIds.forEach(TestStateBuilder::deleteInvoice);
    cleanupInvoiceIds.clear();
    if (contract != null) {
      TestStateBuilder.deleteContract(contract.id());
      TestStateBuilder.deleteCustomer(contract.customer().id());
      TestStateBuilder.deleteBuilding(contract.building().id());
      TestStateBuilder.deleteStaff(contract.staff().id());
      contract = null;
    }
  }

  @Test(groups = {"regression", "critical"}, description = "[E2E-ADM-INV-001] should customer filtering and data display when invoice list")
  public void shouldCustomerFilteringAndDataDisplay() {
    InvoiceRecord invoice = arrangeInvoice();
    StepHelper.act(
        "perform invoice list behavior",
        () -> {
          listPage.filterByCustomer(contract.customer().id());
          listPage.filterByMonth(invoice.month());
          listPage.filterByStatus("PENDING");
          assertThat(TestStateBuilder.invoiceExists(invoice.id())).isTrue();
        });
    StepHelper.assertStep("verify customer filtering and data display", () -> assertThat(TestStateBuilder.invoiceExists(invoice.id())).isTrue());
  }

  @Test(groups = {"regression", "critical"}, description = "[E2E-ADM-INV-002] should create invoice from add form when invoice creation")
  public void shouldCreateInvoiceFromAddForm() {
    InvoicePeriod period = TestStateBuilder.previousInvoicePeriod();
    StepHelper.act(
        "perform invoice creation behavior",
        () -> {
          AdminInvoiceFormPage formPage = pageObjects.create(AdminInvoiceFormPage.class);
          listPage.openAddForm();
          formPage.waitForAddLoaded();
          formPage.fillAddForm(contract.customer().id(), contract.id(), period.month(), period.year(), period.dueDate(), 21, 8);
          formPage.submitInvoice();
          formPage.waitForSweetAlertContains("thanh cong|success");
        });
    StepHelper.assertStep(
        "verify create invoice from add form",
        () -> {
          InvoiceRecord created = TestStateBuilder.findCreatedInvoiceForPeriod(contract, period);
          assertThat(created).isNotNull();
          cleanupInvoiceIds.add(created.id());
          assertThat(created.status()).isEqualTo("PENDING");
        });
  }

  @Test(groups = {"regression", "critical"}, description = "[E2E-ADM-INV-003] should pending invoice update when invoice edit")
  public void shouldPendingInvoiceUpdate() {
    InvoiceRecord invoice = arrangeInvoice();
    String updatedDueDate = nextMonthDueDate(invoice.month(), invoice.year());
    StepHelper.act(
        "perform invoice edit behavior",
        () -> {
          AdminInvoiceFormPage formPage = pageObjects.create(AdminInvoiceFormPage.class);
          formPage.openEdit(invoice.id().intValue());
          formPage.waitForEditLoaded(invoice.id());
          formPage.fillEditForm(updatedDueDate, 40, 10);
          formPage.submitInvoice();
          formPage.waitForSweetAlertContains("thanh cong|success");
        });
    StepHelper.assertStep("verify pending invoice update", () -> assertThat(TestStateBuilder.readInvoiceDueDate(invoice.id())).isEqualTo(updatedDueDate));
  }

  @Test(groups = {"regression", "critical"}, description = "[E2E-ADM-INV-004] should non-pending warning display when invoice edit lock")
  public void shouldNonPendingWarningDisplay() {
    InvoiceRecord invoice = arrangeInvoice();
    TestStateBuilder.markInvoicePaid(invoice.id());
    StepHelper.act(
        "perform invoice edit lock behavior",
        () -> {
          AdminInvoiceFormPage formPage = pageObjects.create(AdminInvoiceFormPage.class);
          formPage.openEdit(invoice.id().intValue());
          formPage.waitForEditLoaded(invoice.id());
          formPage.waitForWarningVisible();
        });
    StepHelper.assertStep("verify non-pending warning display", () -> assertThat(TestStateBuilder.readInvoiceStatus(invoice.id())).isEqualTo("PAID"));
  }

  @Test(groups = {"regression", "critical"}, description = "[E2E-ADM-INV-005] should invoice payment confirmation when payment confirmation")
  public void shouldInvoicePaymentConfirmation() {
    InvoiceRecord invoice = arrangeInvoice();
    StepHelper.act(
        "perform payment confirmation behavior",
        () -> {
          AdminInvoiceDetailPage detailPage = pageObjects.create(AdminInvoiceDetailPage.class);
          detailPage.open(invoice.id().intValue());
          detailPage.waitForLoaded(invoice.id());
          detailPage.confirmInvoicePaid();
          detailPage.confirmSweetAlert();
          detailPage.waitForSweetAlertContains("thanh cong|success");
        });
    StepHelper.assertStep("verify invoice payment confirmation", () -> assertThat(TestStateBuilder.readInvoiceStatus(invoice.id())).isEqualTo("PAID"));
  }

  @Test(groups = {"regression", "critical"}, description = "[E2E-ADM-INV-006] should delete invoice from list when invoice deletion")
  public void shouldDeleteInvoiceFromList() {
    InvoiceRecord invoice = arrangeInvoice();
    StepHelper.act(
        "perform invoice deletion behavior",
        () -> {
          listPage.filterByCustomer(contract.customer().id());
          listPage.filterByMonth(invoice.month());
          listPage.filterByStatus("PENDING");
          listPage.deleteInvoice(invoice.id());
          listPage.confirmSweetAlert();
          listPage.waitForSweetAlertContains("thanh cong|success");
        });
    StepHelper.assertStep(
        "verify delete invoice from list",
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

  private String nextMonthDueDate(int month, int year) {
    LocalDate date = LocalDate.of(year, month, 1).plusMonths(1).withDayOfMonth(20);
    return date.toString();
  }
}
