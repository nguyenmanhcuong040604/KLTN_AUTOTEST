package org.suntower.tests.e2e.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;
import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.data.TestDataFactory;
import org.suntower.fixtures.data.TestDataFactory.StaffPayload;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.CreatedBuilding;
import org.suntower.fixtures.state.TestStateBuilder.CreatedCustomer;
import org.suntower.fixtures.state.TestStateBuilder.CreatedStaff;
import org.suntower.fixtures.state.TestStateBuilder.CreatedStaffRecord;
import org.suntower.pages.admin.AdminStaffDetailPage;
import org.suntower.pages.admin.AdminStaffFormPage;
import org.suntower.pages.admin.AdminStaffFormPage.StaffBasics;
import org.suntower.pages.admin.AdminStaffListPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AdminStaffManagementTest extends BaseTest {
  private final Set<Long> cleanupStaffIds = new LinkedHashSet<>();
  private final Set<Long> cleanupBuildingIds = new LinkedHashSet<>();
  private final Set<Integer> cleanupCustomerIds = new LinkedHashSet<>();
  private AdminStaffListPage listPage;

  @BeforeMethod(alwaysRun = true)
  public void openStaffList() {
    adminSession.open("/admin/staff/list");
    listPage = pageObjects.create(AdminStaffListPage.class);
    listPage.waitForLoaded();
  }

  @AfterMethod(alwaysRun = true)
  public void cleanupStaffScenarios() {
    for (Integer customerId : cleanupCustomerIds) {
      TestStateBuilder.deleteCustomer(customerId);
    }
    cleanupCustomerIds.clear();

    for (Long buildingId : cleanupBuildingIds) {
      TestStateBuilder.deleteBuilding(buildingId);
    }
    cleanupBuildingIds.clear();

    for (Long staffId : cleanupStaffIds) {
      TestStateBuilder.deleteStaff(staffId);
    }
    cleanupStaffIds.clear();
  }

  @Test(
      groups = {"regression"},
      description = "[E2E-ADM-STF-001] should create staff account from add form when staff creation")
  public void shouldCreateStaffAccountFromAddForm() {
    StaffPayload payload = TestDataFactory.buildStaffPayload("STAFF");

    StepHelper.act(
        "perform staff creation behavior",
        () -> {
          AdminStaffFormPage formPage = pageObjects.create(AdminStaffFormPage.class);

          listPage.open();
          listPage.waitForLoaded();
          listPage.openAddForm();
          formPage.waitForLoaded();
          formPage.fillStaffBasics(
              new StaffBasics(payload.fullName(), payload.email(), payload.phone(), payload.username(), payload.password()));
          formPage.selectRole("STAFF");
          formPage.submit();
          formPage.waitForSweetAlertContains("them nhan vien|thanh cong|success");
        });

    StepHelper.assertStep(
        "verify create staff account from add form",
        () -> {
          CreatedStaffRecord createdStaff = TestStateBuilder.findCreatedStaff(payload.username());
          assertThat(createdStaff).isNotNull();
          cleanupStaffIds.add(createdStaff.id());
          assertThat(createdStaff.role()).isEqualTo("STAFF");
        });
  }

  @Test(
      groups = {"regression"},
      description = "[E2E-ADM-STF-002] should search and detail view when staff search")
  public void shouldSearchAndDetailViewWhenStaffSearch() {
    CreatedStaff staff =
        StepHelper.arrange(
            "prepare staff search context",
            () -> {
              CreatedStaff created = TestStateBuilder.createStaff("STAFF");
              cleanupStaffIds.add(created.id());
              return created;
            });

    StepHelper.act(
        "perform staff search behavior",
        () -> {
          adminSession.open("/admin/staff/search?role=STAFF&fullName=" + encode(staff.fullName()));
          listPage.waitForLoaded();
          listPage.waitForSearchTableData();
          assertThat(listPage.rowByStaffName(staff.fullName()).isDisplayed()).isTrue();
          listPage.openDetail(staff.fullName());
          pageObjects.create(AdminStaffDetailPage.class).waitForLoaded(staff.id());
        });

    StepHelper.assertStep("verify search and detail view", () -> assertThat(TestStateBuilder.staffExists(staff.id())).isTrue());
  }

  @Test(
      groups = {"regression"},
      description = "[E2E-ADM-STF-003] should customer and building assignment update when staff assignment")
  public void shouldCustomerAndBuildingAssignmentUpdate() {
    CreatedStaff targetStaff =
        StepHelper.arrange(
            "prepare target staff context",
            () -> {
              CreatedStaff created = TestStateBuilder.createStaff("STAFF");
              cleanupStaffIds.add(created.id());
              return created;
            });
    CreatedStaff manager =
        StepHelper.arrange(
            "prepare manager staff context",
            () -> {
              CreatedStaff created = TestStateBuilder.createStaff("STAFF");
              cleanupStaffIds.add(created.id());
              return created;
            });
    CreatedBuilding building =
        StepHelper.arrange(
            "prepare building assignment context",
            () -> {
              CreatedBuilding created = TestStateBuilder.createBuilding("FOR_RENT");
              cleanupBuildingIds.add(created.id());
              return created;
            });
    CreatedCustomer customer =
        StepHelper.arrange(
            "prepare customer assignment context",
            () -> {
              CreatedCustomer created = TestStateBuilder.createCustomer(manager.id().intValue());
              cleanupCustomerIds.add(created.id());
              return created;
            });

    StepHelper.act(
        "perform staff assignment behavior",
        () -> {
          AdminStaffDetailPage detailPage = pageObjects.create(AdminStaffDetailPage.class);

          detailPage.open(targetStaff.id().intValue());
          detailPage.waitForLoaded(targetStaff.id());
          detailPage.openBuildingAssignments();
          detailPage.setBuildingAssignment(building.id(), true);
          detailPage.saveBuildingAssignments();
          detailPage.waitForSweetAlertContains("cap nhat phan cong toa nha|thanh cong|success");

          detailPage.open(targetStaff.id().intValue());
          detailPage.waitForLoaded(targetStaff.id());
          detailPage.openCustomerAssignments();
          detailPage.setCustomerAssignment(customer.id(), true);
          detailPage.saveCustomerAssignments();
          detailPage.waitForSweetAlertContains("cap nhat phan cong khach hang|thanh cong|success");
        });

    StepHelper.assertStep(
        "verify customer and building assignment update",
        () -> {
          assertThat(TestStateBuilder.staffBuildingAssignmentExists(targetStaff.id(), building.id())).isTrue();
          assertThat(TestStateBuilder.staffCustomerAssignmentExists(targetStaff.id(), customer.id())).isTrue();
        });
  }

  @Test(
      groups = {"regression"},
      description = "[E2E-ADM-STF-004] should search result deletion when staff deletion")
  public void shouldSearchResultDeletionWhenStaffDeletion() {
    CreatedStaff staff =
        StepHelper.arrange(
            "prepare staff deletion context",
            () -> {
              CreatedStaff created = TestStateBuilder.createStaff("STAFF");
              cleanupStaffIds.add(created.id());
              return created;
            });

    StepHelper.act(
        "perform staff deletion behavior",
        () -> {
          adminSession.open("/admin/staff/search?role=STAFF&fullName=" + encode(staff.fullName()));
          listPage.waitForLoaded();
          listPage.waitForSearchTableData();
          listPage.deleteStaff(staff.fullName());
          listPage.confirmSweetAlert();
          listPage.waitForSweetAlertContains("xoa nhan vien|thanh cong|success");
        });

    StepHelper.assertStep("verify search result deletion", () -> assertThat(TestStateBuilder.waitUntilStaffDeleted(staff.id())).isTrue());
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
