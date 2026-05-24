package org.suntower.tests.e2e.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.suntower.fixtures.BaseTest;
import org.suntower.fixtures.StepHelper;
import org.suntower.fixtures.data.TestDataFactory;
import org.suntower.fixtures.state.TestStateBuilder;
import org.suntower.fixtures.state.TestStateBuilder.AmenityRecord;
import org.suntower.fixtures.state.TestStateBuilder.CreatedBuilding;
import org.suntower.fixtures.state.TestStateBuilder.LegalAuthorityRecord;
import org.suntower.fixtures.state.TestStateBuilder.PlanningMapRecord;
import org.suntower.fixtures.state.TestStateBuilder.SupplierRecord;
import org.suntower.pages.admin.AdminBuildingAdditionalInfoPage;
import org.suntower.pages.admin.AdminBuildingAdditionalInfoPage.AmenityForm;
import org.suntower.pages.admin.AdminBuildingAdditionalInfoPage.LegalAuthorityForm;
import org.suntower.pages.admin.AdminBuildingAdditionalInfoPage.PlanningMapForm;
import org.suntower.pages.admin.AdminBuildingAdditionalInfoPage.SupplierForm;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AdminBuildingAdditionalInformationTest extends BaseTest {
  private final List<AdditionalRecord> cleanupRecords = new ArrayList<>();
  private Long cleanupBuildingId;
  private CreatedBuilding building;
  private AdminBuildingAdditionalInfoPage additionalInfoPage;

  @BeforeMethod(alwaysRun = true)
  public void openAdditionalInfoPage() {
    building = TestStateBuilder.createBuilding("FOR_RENT");
    cleanupBuildingId = building.id();
    adminSession.open("/admin/building-additional-information/" + building.id());
    additionalInfoPage = pageObjects.create(AdminBuildingAdditionalInfoPage.class);
    additionalInfoPage.waitForLoaded(building.name());
  }

  @AfterMethod(alwaysRun = true)
  public void cleanupScenario() {
    for (int i = cleanupRecords.size() - 1; i >= 0; i--) {
      AdditionalRecord record = cleanupRecords.get(i);
      TestStateBuilder.deleteAdditionalInfoRecord(record.type(), record.id());
    }
    cleanupRecords.clear();
    TestStateBuilder.deleteBuilding(cleanupBuildingId);
    cleanupBuildingId = null;
  }

  @Test(
      groups = {"regression"},
      description = "[E2E-ADM-BAI-001] should additional information sections load when page navigation")
  public void shouldAdditionalInformationSectionsLoad() {
    StepHelper.act(
        "perform page navigation behavior",
        () -> {
          additionalInfoPage.waitForLoaded(building.name());
          additionalInfoPage.waitForAllSectionsVisible();
          additionalInfoPage.waitForCounterValue("legal", 0);
          additionalInfoPage.waitForCounterValue("amenity", 0);
          additionalInfoPage.waitForCounterValue("planning", 0);
          additionalInfoPage.waitForCounterValue("supplier", 0);
        });

    StepHelper.assertStep("verify additional information sections load", () -> assertThat(building.id()).isNotNull());
  }

  @Test(
      groups = {"regression"},
      description = "[E2E-ADM-BAI-002] should create and update flow when legal authority")
  public void shouldCreateAndUpdateLegalAuthority() {
    String authorityName = "E2E Legal " + TestDataFactory.uniqueCode("legal");
    String updatedName = authorityName + " Updated";
    String email = TestDataFactory.uniqueEmail("legal-e2e");
    String updatedEmail = TestDataFactory.uniqueEmail("legal-updated");

    StepHelper.act(
        "perform legal authority behavior",
        () -> {
          additionalInfoPage.addLegalAuthority(
              new LegalAuthorityForm(authorityName, "NOTARY", TestDataFactory.uniquePhoneNumber(), email, "123 Test Street", "Created by E2E"));
          additionalInfoPage.waitForLegalAuthorityVisible(authorityName);

          Long id = TestStateBuilder.findLegalAuthorityId(building.id(), authorityName);
          assertThat(id).isNotNull();
          cleanupRecords.add(new AdditionalRecord("legal", id));

          additionalInfoPage.editLegalAuthority(
              authorityName,
              new LegalAuthorityForm(updatedName, "LAW_FIRM", TestDataFactory.uniquePhoneNumber(), updatedEmail, "123 Test Street", ""));
          additionalInfoPage.waitForLegalAuthorityVisible(updatedName);
        });

    StepHelper.assertStep(
        "verify create and update flow",
        () -> {
          Long id = cleanupRecords.get(0).id();
          LegalAuthorityRecord record = TestStateBuilder.readLegalAuthority(id);
          assertThat(record).isNotNull();
          assertThat(record.authorityName()).isEqualTo(updatedName);
          assertThat(record.authorityType()).isEqualTo("LAW_FIRM");
          additionalInfoPage.waitForCounterValue("legal", 1);
        });
  }

  @Test(
      groups = {"regression"},
      description = "[E2E-ADM-BAI-003] should supplier email validation and entity creation when supplier and amenity")
  public void shouldValidateSupplierEmailAndCreateSupplierAndAmenity() {
    String amenityName = "E2E Park " + TestDataFactory.uniqueCode("park");
    String supplierName = "E2E Supplier " + TestDataFactory.uniqueCode("supplier");
    String supplierEmail = TestDataFactory.uniqueEmail("supplier-e2e");

    StepHelper.act(
        "perform supplier and amenity behavior",
        () -> {
          additionalInfoPage.addSupplier(
              new SupplierForm("Invalid Supplier", "CLEANING", TestDataFactory.uniquePhoneNumber(), "invalid-email", "", ""));
          additionalInfoPage.waitForValidationPopupContains("Email");
          additionalInfoPage.closeModal("supplier");

          additionalInfoPage.addAmenity(new AmenityForm(amenityName, "PARK", "456 Amenity Street", "10.7620000", "106.6600000", "500"));
          additionalInfoPage.waitForAmenityVisible(amenityName);
          Long amenityId = TestStateBuilder.findAmenityId(building.id(), amenityName);
          assertThat(amenityId).isNotNull();
          cleanupRecords.add(new AdditionalRecord("amenity", amenityId));

          additionalInfoPage.addSupplier(
              new SupplierForm(
                  supplierName, "CLEANING", TestDataFactory.uniquePhoneNumber(), supplierEmail, "789 Supplier Street", "Managed by E2E"));
          additionalInfoPage.waitForSupplierVisible(supplierName);
          Long supplierId = TestStateBuilder.findSupplierId(building.id(), supplierName);
          assertThat(supplierId).isNotNull();
          cleanupRecords.add(new AdditionalRecord("supplier", supplierId));
        });

    StepHelper.assertStep(
        "verify supplier email validation and entity creation",
        () -> {
          AmenityRecord amenity = TestStateBuilder.readAmenity(cleanupRecords.get(0).id());
          SupplierRecord supplier = TestStateBuilder.readSupplier(cleanupRecords.get(1).id());
          assertThat(amenity.name()).isEqualTo(amenityName);
          assertThat(amenity.amenityType()).isEqualTo("PARK");
          assertThat(amenity.distanceMeter()).isEqualTo(500);
          assertThat(supplier.name()).isEqualTo(supplierName);
          assertThat(supplier.serviceType()).isEqualTo("CLEANING");
          assertThat(supplier.email()).isEqualTo(supplierEmail);
          additionalInfoPage.waitForCounterValue("amenity", 1);
          additionalInfoPage.waitForCounterValue("supplier", 1);
        });
  }

  @Test(
      groups = {"regression"},
      description = "[E2E-ADM-BAI-004] should create and delete flow when planning map")
  public void shouldCreateAndDeletePlanningMap() {
    String mapType = "E2E Planning " + TestDataFactory.uniqueCode("planning");

    StepHelper.act(
        "perform planning map behavior",
        () -> {
          additionalInfoPage.addPlanningMap(
              new PlanningMapForm(
                  mapType,
                  "Construction Department",
                  "2025-01-01",
                  "2030-01-01",
                  "Planning map from E2E",
                  "/images/planning_map_img/map1.jpg"));
          additionalInfoPage.waitForPlanningMapVisible(mapType);

          Long planningMapId = TestStateBuilder.findPlanningMapId(building.id(), mapType);
          assertThat(planningMapId).isNotNull();
          cleanupRecords.add(new AdditionalRecord("planning", planningMapId));
          PlanningMapRecord created = TestStateBuilder.readPlanningMap(planningMapId);
          assertThat(created.mapType()).isEqualTo(mapType);
          assertThat(created.issuedBy()).isEqualTo("Construction Department");
          assertThat(created.imageUrl()).contains("map1.jpg");
          additionalInfoPage.waitForCounterValue("planning", 1);

          additionalInfoPage.deletePlanningMap(mapType);
          cleanupRecords.clear();
        });

    StepHelper.assertStep(
        "verify create and delete flow",
        () -> {
          Long deletedId = TestStateBuilder.findPlanningMapId(building.id(), mapType);
          assertThat(deletedId).isNull();
          assertThat(TestStateBuilder.planningMapCount(building.id())).isZero();
          additionalInfoPage.waitForCounterValue("planning", 0);
        });
  }

  private record AdditionalRecord(String type, Long id) {}
}
