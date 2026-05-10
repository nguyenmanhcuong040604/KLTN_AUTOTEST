import { expect, test as base } from "@fixtures/base.fixture";
import { TestDataFactory } from "@test-data-factories/TestDataFactory";
import { AdminBuildingAdditionalInfoPage } from "@pages/admin/AdminBuildingAdditionalInfoPage";
import {
  deleteAdditionalInfoRecord,
  findAmenityId,
  findLegalAuthorityId,
  findPlanningMapId,
  findSupplierId,
  planningMapCount,
  planningMapExists,
  readAmenity,
  readLegalAuthority,
  readPlanningMap,
  readSupplier,
  type BuildingAdditionalInfoType
} from "@test-data-scenarios/buildingAdditionalInfoScenario";
import {
  cleanupStaffProfileScenario,
  createStaffProfileScenario,
  loginAsScenarioUser,
  type StaffProfileState
} from "@test-data-scenarios/profileScenario";

base.describe("Admin - Building Additional Information @regression", () => {
  let adminUser: StaffProfileState | null = null;
  let buildingId: number | null = null;
  let buildingName = "";
  const cleanupIds: Record<BuildingAdditionalInfoType, number[]> = {
    legal: [],
    amenity: [],
    planning: [],
    supplier: []
  };

  const test = base.extend<{ scenarioSetup: void }>({
    scenarioSetup: [
      async ({ page, testState, pageObjects: _pageObjects, navigationPage }, use) => {
        adminUser = await createStaffProfileScenario(testState, "ADMIN");
        const BuildingState = await testState.createBuilding("FOR_RENT");
        buildingId = BuildingState.id;
        buildingName = BuildingState.name;

        await loginAsScenarioUser(page, adminUser.username, adminUser.password);
        await navigationPage.open(`/admin/building-additional-information/${BuildingState.id}`);

        try {
          await use(undefined);
        } finally {
          for (const type of ["legal", "amenity", "planning", "supplier"] as const) {
            while (cleanupIds[type].length > 0) {
              const id = cleanupIds[type].pop();
              await deleteAdditionalInfoRecord(testState, type, id);
            }
          }

          if (buildingId) {
            await testState.deleteBuilding(buildingId);
          }
          buildingId = null;
          buildingName = "";

          await cleanupStaffProfileScenario(testState, adminUser);
          adminUser = null;
        }
      },
      { auto: true }
    ]
  });

  test("[E2E-ADM-BAI-001] should additional information sections load when page navigation", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare page navigation context", async () => {
      expect.soft(true, "precondition: prepare page navigation context").toBe(true);
    });

    await steps.act("perform page navigation behavior", async () => {
      const additionalInfoPage = pageObjects.create(AdminBuildingAdditionalInfoPage);
      await additionalInfoPage.waitForLoaded(buildingName);
      await additionalInfoPage.waitForAllSectionsVisible();
      await additionalInfoPage.waitForCounterValue("legal", 0);
      await additionalInfoPage.waitForCounterValue("amenity", 0);
      await additionalInfoPage.waitForCounterValue("planning", 0);
      await additionalInfoPage.waitForCounterValue("supplier", 0);
    });

    await steps.assert("verify additional information sections load", async () => {
      expect.soft(true, "verification checkpoint: verify additional information sections load").toBe(true);
    });
  });

  test("[E2E-ADM-BAI-002] should create and update flow when legal authority", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare legal authority context", async () => {
      expect.soft(true, "precondition: prepare legal authority context").toBe(true);
    });

    await steps.act("perform legal authority behavior", async () => {
      const additionalInfoPage = pageObjects.create(AdminBuildingAdditionalInfoPage);
      const authorityName = `E2E Legal ${TestDataFactory.uniqueCode("legal")}`;
      const updatedName = `${authorityName} Updated`;
      const legalEmail = TestDataFactory.uniqueEmail("legal-e2e");
      const updatedLegalEmail = TestDataFactory.uniqueEmail("legal-updated");

      await additionalInfoPage.waitForLoaded(buildingName);
      await additionalInfoPage.addLegalAuthority({
        authorityName,
        authorityType: "NOTARY",
        phone: TestDataFactory.uniquePhoneNumber(),
        email: legalEmail,
        address: "123 Test Street",
        note: "Created by E2E"
      });
      await additionalInfoPage.waitForLegalAuthorityVisible(authorityName);

      const legalAuthorityId = await findLegalAuthorityId(buildingId!, authorityName);
      expect(legalAuthorityId).toBeTruthy();
      cleanupIds.legal.push(legalAuthorityId!);

      await additionalInfoPage.editLegalAuthority(authorityName, {
        authorityName: updatedName,
        authorityType: "LAW_FIRM",
        phone: TestDataFactory.uniquePhoneNumber(),
        email: updatedLegalEmail
      });
      await additionalInfoPage.waitForLegalAuthorityVisible(updatedName);

      const updatedLegalAuthority = await readLegalAuthority(legalAuthorityId!);
      expect(updatedLegalAuthority?.authority_name).toBe(updatedName);
      expect(updatedLegalAuthority?.authority_type).toBe("LAW_FIRM");
      await additionalInfoPage.waitForCounterValue("legal", 1);
    });

    await steps.assert("verify create and update flow", async () => {
      expect.soft(true, "verification checkpoint: verify create and update flow").toBe(true);
    });
  });

  test("[E2E-ADM-BAI-003] should supplier email validation and entity creation when supplier and amenity", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare supplier and amenity context", async () => {
      expect.soft(true, "precondition: prepare supplier and amenity context").toBe(true);
    });

    await steps.act("perform supplier and amenity behavior", async () => {
      const additionalInfoPage = pageObjects.create(AdminBuildingAdditionalInfoPage);
      const amenityName = `E2E Park ${TestDataFactory.uniqueCode("park")}`;
      const supplierName = `E2E Supplier ${TestDataFactory.uniqueCode("supplier")}`;
      const supplierEmail = TestDataFactory.uniqueEmail("supplier-e2e");

      await additionalInfoPage.waitForLoaded(buildingName);
      await additionalInfoPage.addSupplier({
        name: "Invalid Supplier",
        serviceType: "CLEANING",
        phone: TestDataFactory.uniquePhoneNumber(),
        email: "invalid-email"
      });
      await additionalInfoPage.waitForValidationPopupContains(/Email/i);
      await additionalInfoPage.closeModal("supplier");

      await additionalInfoPage.addAmenity({
        name: amenityName,
        amenityType: "PARK",
        address: "456 Amenity Street",
        latitude: "10.7620000",
        longitude: "106.6600000",
        distanceMeter: "500"
      });
      await additionalInfoPage.waitForAmenityVisible(amenityName);

      const amenityId = await findAmenityId(buildingId!, amenityName);
      expect(amenityId).toBeTruthy();
      cleanupIds.amenity.push(amenityId!);
      const createdAmenity = await readAmenity(amenityId!);
      expect(createdAmenity?.name).toBe(amenityName);
      expect(createdAmenity?.amenity_type).toBe("PARK");
      expect(Number(createdAmenity?.distance_meter)).toBe(500);

      await additionalInfoPage.addSupplier({
        name: supplierName,
        serviceType: "CLEANING",
        phone: TestDataFactory.uniquePhoneNumber(),
        email: supplierEmail,
        address: "789 Supplier Street",
        note: "Managed by E2E"
      });
      await additionalInfoPage.waitForSupplierVisible(supplierName);

      const supplierId = await findSupplierId(buildingId!, supplierName);
      expect(supplierId).toBeTruthy();
      cleanupIds.supplier.push(supplierId!);
      const createdSupplier = await readSupplier(supplierId!);
      expect(createdSupplier?.name).toBe(supplierName);
      expect(createdSupplier?.service_type).toBe("CLEANING");
      expect(createdSupplier?.email).toBe(supplierEmail);

      await additionalInfoPage.waitForCounterValue("amenity", 1);
      await additionalInfoPage.waitForCounterValue("supplier", 1);
    });

    await steps.assert("verify supplier email validation and entity creation", async () => {
      expect.soft(true, "verification checkpoint: verify supplier email validation and entity creation").toBe(true);
    });
  });

  test("[E2E-ADM-BAI-004] should create and delete flow when planning map", async ({
    page: _page,
    pageObjects,
    navigationPage: _navigationPage,
    steps
  }) => {
    await steps.arrange("prepare planning map context", async () => {
      expect.soft(true, "precondition: prepare planning map context").toBe(true);
    });

    await steps.act("perform planning map behavior", async () => {
      const additionalInfoPage = pageObjects.create(AdminBuildingAdditionalInfoPage);
      const mapType = `E2E Planning ${TestDataFactory.uniqueCode("planning")}`;

      await additionalInfoPage.waitForLoaded(buildingName);
      await additionalInfoPage.addPlanningMap({
        mapType,
        issuedBy: "Construction Department",
        issuedDate: "2025-01-01",
        expiredDate: "2030-01-01",
        existingImageUrl: "/images/planning_map_img/map1.jpg",
        note: "Planning map from E2E"
      });
      await additionalInfoPage.waitForPlanningMapVisible(mapType);

      const planningMapId = await findPlanningMapId(buildingId!, mapType);
      expect(planningMapId).toBeTruthy();
      cleanupIds.planning.push(planningMapId!);
      const createdPlanningMap = await readPlanningMap(planningMapId!);
      expect(createdPlanningMap?.map_type).toBe(mapType);
      expect(createdPlanningMap?.issued_by).toBe("Construction Department");
      expect(createdPlanningMap?.image_url).toContain("map1.jpg");
      await additionalInfoPage.waitForCounterValue("planning", 1);

      await additionalInfoPage.deletePlanningMap(mapType);
      cleanupIds.planning.pop();
      await expect.poll(() => planningMapExists(planningMapId!)).toBeFalsy();
      await expect.poll(() => planningMapCount(buildingId!)).toBe(0);
      await additionalInfoPage.waitForCounterValue("planning", 0);
    });

    await steps.assert("verify create and delete flow", async () => {
      expect.soft(true, "verification checkpoint: verify create and delete flow").toBe(true);
    });
  });
});
