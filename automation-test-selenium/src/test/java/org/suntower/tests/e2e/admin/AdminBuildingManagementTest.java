package org.suntower.tests.e2e.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.data.TestDataFactory;
import org.suntower.fixtures.data.TestDataFactory.RentalFields;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.BuildingEditState;
import org.suntower.fixtures.state.TestStateBuilder.CreatedBuilding;
import org.suntower.fixtures.state.TestStateBuilder.CreatedBuildingFromForm;
import org.suntower.fixtures.state.TestStateBuilder.CreatedContract;
import org.suntower.pages.admin.AdminBuildingFormPage;
import org.suntower.pages.admin.AdminBuildingFormPage.BuildingCommonFields;
import org.suntower.pages.admin.AdminBuildingFormPage.BuildingRentFields;
import org.suntower.pages.admin.AdminBuildingDetailPage;
import org.suntower.pages.admin.AdminBuildingListPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AdminBuildingManagementTest extends BaseTest {
  private final Set<Long> cleanupBuildingIds = new LinkedHashSet<>();
  private final Set<Long> cleanupContractIds = new LinkedHashSet<>();
  private final Set<Integer> cleanupCustomerIds = new LinkedHashSet<>();
  private final Set<Long> cleanupStaffIds = new LinkedHashSet<>();
  private AdminBuildingListPage listPage;

  @BeforeMethod(alwaysRun = true)
  public void openBuildingList() {
    adminSession.open("/admin/building/list");
    listPage = pageObjects.create(AdminBuildingListPage.class);
    listPage.waitForLoaded();
  }

  @AfterMethod(alwaysRun = true)
  public void cleanupBuildingScenarios() {
    for (Long contractId : cleanupContractIds) {
      TestStateBuilder.deleteContract(contractId);
    }
    cleanupContractIds.clear();
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
      groups = {"regression", "critical"},
      description = "[E2E-ADM-BLD-002] should rental building creation when building creation")
  public void shouldRentalBuildingCreation() {
    String buildingName = TestDataFactory.uniqueBuildingName("SEL Form Building");
    String taxCode = TestDataFactory.uniqueNumberCode(TestDataFactory.BUILDING_FORM.taxCodePrefix, 10);
    RentalFields createData = TestDataFactory.BUILDING_FORM.rentalCreate;

    StepHelper.act(
        "perform building creation behavior",
        () -> {
          AdminBuildingFormPage formPage = pageObjects.create(AdminBuildingFormPage.class);

          listPage.open();
          listPage.waitForLoaded();
          listPage.openAddForm();
          formPage.waitForAddLoaded();
          formPage.setTransactionType("FOR_RENT");
          formPage.fillCommonFields(
              new BuildingCommonFields(
                  buildingName,
                  TestDataFactory.BUILDING_FORM.defaultDistrictId,
                  TestDataFactory.BUILDING_FORM.defaultWard,
                  TestDataFactory.BUILDING_FORM.defaultStreet,
                  createData.numberOfFloor(),
                  createData.numberOfBasement(),
                  createData.floorArea(),
                  TestDataFactory.BUILDING_FORM.defaultLevel,
                  TestDataFactory.BUILDING_FORM.defaultDirection,
                  taxCode,
                  TestDataFactory.BUILDING_FORM.validLink));
          formPage.fillRentFields(
              new BuildingRentFields(
                  createData.rentPrice(),
                  createData.deposit(),
                  createData.serviceFee(),
                  createData.carFee(),
                  createData.motorbikeFee(),
                  createData.waterFee(),
                  createData.electricityFee(),
                  createData.rentAreaValues()));
          formPage.setCoordinates(
              TestDataFactory.BUILDING_FORM.defaultCoordinates.latitude(),
              TestDataFactory.BUILDING_FORM.defaultCoordinates.longitude());
          formPage.submit();
          formPage.waitForSweetAlertContains("thanh cong|success");
        });

    StepHelper.assertStep(
        "verify rental building creation",
        () -> {
          CreatedBuildingFromForm createdBuilding = TestStateBuilder.findBuildingCreatedFromForm(buildingName);
          assertThat(createdBuilding).isNotNull();
          cleanupBuildingIds.add(createdBuilding.id());
          assertThat(createdBuilding.transactionType()).isEqualTo("FOR_RENT");
          assertThat(createdBuilding.floorArea()).isEqualTo(createData.floorArea().longValue());
          assertThat(createdBuilding.taxCode()).isEqualTo(taxCode);
        });
  }

  @Test(
      groups = {"regression", "critical"},
      description = "[E2E-ADM-BLD-003] should unlocked building update when building edit")
  public void shouldUnlockedBuildingUpdate() {
    CreatedBuilding building =
        StepHelper.arrange(
            "prepare building edit context",
            () -> {
              CreatedBuilding created = TestStateBuilder.createBuilding("FOR_RENT");
              cleanupBuildingIds.add(created.id());
              return created;
            });
    RentalFields updateData = TestDataFactory.BUILDING_FORM.rentalUpdate;
    String updatedName = building.name() + " Updated";

    StepHelper.act(
        "perform building edit behavior",
        () -> {
          AdminBuildingFormPage formPage = pageObjects.create(AdminBuildingFormPage.class);

          formPage.openEdit(building.id().intValue());
          formPage.waitForEditLoaded(building.id().intValue());
          formPage.fillCommonFields(
              new BuildingCommonFields(
                  updatedName, null, null, null, updateData.numberOfFloor(), null, updateData.floorArea(), null, null, null, null));
          formPage.fillRentFields(
              new BuildingRentFields(
                  updateData.rentPrice(),
                  updateData.deposit(),
                  updateData.serviceFee(),
                  updateData.carFee(),
                  updateData.motorbikeFee(),
                  updateData.waterFee(),
                  updateData.electricityFee(),
                  updateData.rentAreaValues()));
          formPage.submit();
          formPage.waitForSweetAlertContains("thanh cong|success");
        });

    StepHelper.assertStep(
        "verify unlocked building update",
        () -> {
          BuildingEditState buildingState = TestStateBuilder.findBuildingEditState(building.id());
          assertThat(buildingState).isNotNull();
          assertThat(buildingState.name()).isEqualTo(updatedName);
          assertThat(buildingState.floorArea()).isEqualTo(updateData.floorArea().longValue());
          assertThat(buildingState.rentPrice()).isEqualTo(updateData.rentPrice().longValue());
        });
  }

  @Test(
      groups = {"regression", "critical"},
      description = "[E2E-ADM-BLD-001] should filter and detail view when building search")
  public void shouldFilterAndDetailViewWhenBuildingSearch() {
    CreatedBuilding building =
        StepHelper.arrange(
            "prepare building search context",
            () -> {
              CreatedBuilding created = TestStateBuilder.createBuilding("FOR_RENT");
              cleanupBuildingIds.add(created.id());
              return created;
            });

    StepHelper.act(
        "perform building search behavior",
        () -> {
          listPage.open();
          listPage.waitForLoaded();
          listPage.filterByName(building.name());
          listPage.filterByTransactionType("FOR_RENT");
          listPage.search();
          listPage.waitForTableData();

          assertThat(listPage.rowByBuildingName(building.name()).isDisplayed()).isTrue();
          listPage.openDetail(building.name());
          pageObjects.create(AdminBuildingDetailPage.class).waitForLoaded(building.id());
        });

    StepHelper.assertStep(
        "verify filter and detail view",
        () -> assertThat(TestStateBuilder.buildingNameMatches(building.id(), building.name())).isTrue());
  }

  @Test(
      groups = {"regression", "critical"},
      description = "[E2E-ADM-BLD-004] should active contract lock banner display when building edit lock")
  public void shouldActiveContractLockBannerDisplay() {
    CreatedContract contract =
        StepHelper.arrange(
            "prepare building edit lock context",
            () -> {
              CreatedContract created = TestStateBuilder.createContract();
              cleanupContractIds.add(created.id());
              cleanupCustomerIds.add(created.customer().id());
              cleanupBuildingIds.add(created.building().id());
              cleanupStaffIds.add(created.staff().id());
              return created;
            });

    StepHelper.act(
        "perform building edit lock behavior",
        () -> {
          AdminBuildingFormPage formPage = pageObjects.create(AdminBuildingFormPage.class);
          formPage.openEdit(contract.building().id().intValue());
          formPage.waitForEditLoaded(contract.building().id().intValue());
          formPage.waitForLockBanner();
        });

    StepHelper.assertStep("verify active contract lock banner display", () -> assertThat(TestStateBuilder.contractExists(contract.id())).isTrue());
  }

  @Test(
      groups = {"regression", "critical"},
      description = "[E2E-ADM-BLD-005] should unlocked building deletion from list when building deletion")
  public void shouldUnlockedBuildingDeletionFromList() {
    CreatedBuilding building =
        StepHelper.arrange(
            "prepare building deletion context",
            () -> {
              CreatedBuilding created = TestStateBuilder.createBuilding("FOR_SALE");
              cleanupBuildingIds.add(created.id());
              return created;
            });

    StepHelper.act(
        "perform building deletion behavior",
        () -> {
          listPage.open();
          listPage.waitForLoaded();
          listPage.filterByName(building.name());
          listPage.search();
          listPage.waitForTableData();
          listPage.deleteBuilding(building.name());
          listPage.confirmSweetAlert();
        });

    StepHelper.assertStep(
        "verify unlocked building deletion from list",
        () -> assertThat(TestStateBuilder.waitUntilBuildingDeleted(building.id())).isTrue());
  }
}
