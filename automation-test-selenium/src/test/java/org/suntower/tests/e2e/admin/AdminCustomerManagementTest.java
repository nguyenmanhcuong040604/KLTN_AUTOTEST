package org.suntower.tests.e2e.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;
import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.data.TestDataFactory;
import org.suntower.fixtures.data.TestDataFactory.CustomerPayload;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedCustomer;
import org.suntower.fixtures.state.TestStateBuilder.CreatedCustomerRecord;
import org.suntower.fixtures.state.TestStateBuilder.CreatedStaff;
import org.suntower.pages.admin.AdminCustomerFormPage;
import org.suntower.pages.admin.AdminCustomerFormPage.CustomerBasics;
import org.suntower.pages.admin.AdminCustomerDetailPage;
import org.suntower.pages.admin.AdminCustomerListPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AdminCustomerManagementTest extends BaseTest {
  private final Set<Integer> cleanupCustomerIds = new LinkedHashSet<>();
  private final Set<Long> cleanupStaffIds = new LinkedHashSet<>();
  private AdminCustomerListPage listPage;

  @BeforeMethod(alwaysRun = true)
  public void openCustomerList() {
    adminSession.open("/admin/customer/list");
    listPage = pageObjects.create(AdminCustomerListPage.class);
    listPage.waitForLoaded();
  }

  @AfterMethod(alwaysRun = true)
  public void cleanupCustomerScenarios() {
    for (Integer customerId : cleanupCustomerIds) {
      TestStateBuilder.deleteCustomer(customerId);
    }
    cleanupCustomerIds.clear();

    for (Long staffId : cleanupStaffIds) {
      TestStateBuilder.deleteStaff(staffId);
    }
    cleanupStaffIds.clear();
  }

  @Test(
      groups = {"regression"},
      description = "[E2E-ADM-CUS-001] should create customer from add form when customer creation")
  public void shouldCreateCustomerFromAddForm() {
    CreatedStaff manager =
        StepHelper.arrange(
            "prepare customer creation staff context",
            () -> {
              CreatedStaff created = TestStateBuilder.createStaff("STAFF");
              cleanupStaffIds.add(created.id());
              return created;
            });
    CustomerPayload payload = TestDataFactory.buildCustomerPayload();

    StepHelper.act(
        "perform customer creation behavior",
        () -> {
          AdminCustomerFormPage formPage = pageObjects.create(AdminCustomerFormPage.class);

          listPage.open();
          listPage.waitForLoaded();
          listPage.openAddForm();
          formPage.waitForLoaded();
          formPage.fillCustomerBasics(
              new CustomerBasics(payload.fullName(), payload.email(), payload.phone(), payload.username(), payload.password()));
          formPage.selectStaffIds(manager.id().intValue());
          formPage.submit();
          formPage.waitForSweetAlertContains("them khach hang|thanh cong|success");
        });

    StepHelper.assertStep(
        "verify create customer from add form",
        () -> {
          CreatedCustomerRecord createdCustomer = TestStateBuilder.findCreatedCustomer(payload.username());
          assertThat(createdCustomer).isNotNull();
          cleanupCustomerIds.add(createdCustomer.id());
          assertThat(TestStateBuilder.staffCustomerAssignmentExists(manager.id(), createdCustomer.id())).isTrue();
        });
  }

  @Test(
      groups = {"regression"},
      description = "[E2E-ADM-CUS-002] should search and detail view when customer search")
  public void shouldSearchAndDetailViewWhenCustomerSearch() {
    CreatedStaff manager =
        StepHelper.arrange(
            "prepare customer search staff context",
            () -> {
              CreatedStaff created = TestStateBuilder.createStaff("STAFF");
              cleanupStaffIds.add(created.id());
              return created;
            });
    CreatedCustomer customer =
        StepHelper.arrange(
            "prepare customer search context",
            () -> {
              CreatedCustomer created = TestStateBuilder.createCustomer(manager.id().intValue());
              cleanupCustomerIds.add(created.id());
              return created;
            });

    StepHelper.act(
        "perform customer search behavior",
        () -> {
          adminSession.open("/admin/customer/search?fullName=" + encode(customer.fullName()));
          listPage.waitForLoaded();
          listPage.waitForTableData();
          assertThat(listPage.rowByCustomerName(customer.fullName()).isDisplayed()).isTrue();
          listPage.openDetail(customer.fullName());
          pageObjects.create(AdminCustomerDetailPage.class).waitForLoaded(customer.id());
        });

    StepHelper.assertStep(
        "verify search and detail view",
        () -> assertThat(TestStateBuilder.staffCustomerAssignmentExists(manager.id(), customer.id())).isTrue());
  }

  @Test(
      groups = {"regression"},
      description = "[E2E-ADM-CUS-003] should no staff selected validation when staff assignment")
  public void shouldNoStaffSelectedValidation() {
    CustomerPayload payload = TestDataFactory.buildCustomerPayload();

    StepHelper.act(
        "perform staff assignment behavior",
        () -> {
          AdminCustomerFormPage formPage = pageObjects.create(AdminCustomerFormPage.class);

          formPage.openAdd();
          formPage.waitForLoaded();
          formPage.fillCustomerBasics(
              new CustomerBasics(payload.fullName(), payload.email(), payload.phone(), payload.username(), payload.password()));
          formPage.submit();
          formPage.waitForSweetAlertContains("loi|error|nhan vien");
        });

    StepHelper.assertStep(
        "verify no staff selected validation",
        () -> assertThat(TestStateBuilder.customerAccountExists(payload.username(), payload.email())).isFalse());
  }

  @Test(
      groups = {"regression"},
      description = "[E2E-ADM-CUS-004] should search result deletion when customer deletion")
  public void shouldSearchResultDeletionWhenCustomerDeletion() {
    CreatedStaff manager =
        StepHelper.arrange(
            "prepare customer deletion staff context",
            () -> {
              CreatedStaff created = TestStateBuilder.createStaff("STAFF");
              cleanupStaffIds.add(created.id());
              return created;
            });
    CreatedCustomer customer =
        StepHelper.arrange(
            "prepare customer deletion context",
            () -> {
              CreatedCustomer created = TestStateBuilder.createCustomer(manager.id().intValue());
              cleanupCustomerIds.add(created.id());
              return created;
            });

    StepHelper.act(
        "perform customer deletion behavior",
        () -> {
          adminSession.open("/admin/customer/search?fullName=" + encode(customer.fullName()));
          listPage.waitForLoaded();
          listPage.waitForTableData();
          listPage.deleteCustomer(customer.fullName());
          listPage.confirmSweetAlert();
          listPage.waitForSweetAlertContains("xoa khach hang|thanh cong|success");
        });

    StepHelper.assertStep(
        "verify search result deletion",
        () -> assertThat(TestStateBuilder.waitUntilCustomerDeleted(customer.id())).isTrue());
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
