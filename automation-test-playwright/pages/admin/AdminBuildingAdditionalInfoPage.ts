import { type Locator, type Page } from "@playwright/test";
import { BasePage } from "../core/BasePage";

type LegalAuthorityForm = {
  authorityName: string;
  authorityType: string;
  phone: string;
  email: string;
  address: string;
  note?: string;
};

type AmenityForm = {
  name: string;
  amenityType: string;
  address: string;
  latitude: string;
  longitude: string;
  distanceMeter?: string;
};

type PlanningMapForm = {
  mapType: string;
  issuedBy: string;
  issuedDate: string;
  expiredDate: string;
  note?: string;
  existingImageUrl: string;
};

type SupplierForm = {
  name: string;
  serviceType: string;
  phone: string;
  email: string;
  address?: string;
  note?: string;
};

export class AdminBuildingAdditionalInfoPage extends BasePage {
  /**
   * Initializes this page object.
   */
  constructor(page: Page) {
    super(page);
  }

  /**
   * Opens the page.
   */
  async open(buildingId: number): Promise<void> {
    await this.visit(`/admin/building-additional-information/${buildingId}`);
  }

  async waitForLoaded(buildingName?: string): Promise<void> {
    await this.page.waitForURL(/\/admin\/building-additional-information\/\d+/);
    await this.waitForVisible(this.page.locator("#cnt-legal"));
    await this.waitForVisible(this.page.locator("#cnt-amenity"));
    await this.waitForVisible(this.page.locator("#cnt-planning"));
    await this.waitForVisible(this.page.locator("#cnt-supplier"));
    if (buildingName) {
      await this.waitForLocatorText(this.page.locator(".building-name-badge"), buildingName);
    }
  }

  async waitForAllSectionsVisible(): Promise<void> {
    await this.waitForVisible(this.page.locator("#section-legal"));
    await this.waitForVisible(this.page.locator("#section-amenity"));
    await this.waitForVisible(this.page.locator("#section-planning"));
    await this.waitForVisible(this.page.locator("#section-supplier"));
  }

  async waitForCounterValue(type: "legal" | "amenity" | "planning" | "supplier", count: number): Promise<void> {
    await this.waitForLocatorText(this.page.locator(`#cnt-${type}`), String(count));
  }

  private section(type: "legal" | "amenity" | "planning" | "supplier"): Locator {
    return this.page.locator(`#section-${type}`);
  }

  private modal(type: "legal" | "amenity" | "planning" | "supplier"): Locator {
    const capitalized = type.charAt(0).toUpperCase() + type.slice(1);
    return this.page.locator(`#modal${capitalized}`);
  }

  private rowByName(type: "legal" | "amenity" | "planning" | "supplier", name: string): Locator {
    return this.firstVisible(this.section(type).locator("tbody tr").filter({ hasText: name }));
  }

  async openCreateModal(type: "legal" | "amenity" | "planning" | "supplier"): Promise<void> {
    await this.section(type)
      .getByRole("button", { name: /Thêm mới|add/i })
      .click();
    await this.waitForVisible(this.modal(type));
  }

  /**
   * Closes the modal UI element.
   */
  async closeModal(type: "legal" | "amenity" | "planning" | "supplier"): Promise<void> {
    const modal = this.modal(type);
    await this.firstVisible(modal.locator(".btn-modal-cancel, .btn-close")).click();
    await this.waitForHidden(modal);
  }

  /**
   * Saves modal changes.
   */
  private async saveModal(type: "legal" | "amenity" | "planning" | "supplier"): Promise<void> {
    await this.modal(type).locator(".btn-modal-save").click();
  }

  async addLegalAuthority(data: LegalAuthorityForm): Promise<void> {
    await this.openCreateModal("legal");
    await this.page.locator("#legal-authorityName").fill(data.authorityName);
    await this.page.locator("#legal-authorityType").selectOption(data.authorityType);
    await this.page.locator("#legal-phone").fill(data.phone);
    await this.page.locator("#legal-email").fill(data.email);
    await this.page.locator("#legal-address").fill(data.address);
    await this.page.locator("#legal-note").fill(data.note ?? "");
    await this.saveModal("legal");
  }

  async editLegalAuthority(currentName: string, data: Partial<LegalAuthorityForm>): Promise<void> {
    await this.actionButton(this.rowByName("legal", currentName), "edit").click();
    await this.waitForVisible(this.modal("legal"));
    if (data.authorityName !== undefined) {
      await this.page.locator("#legal-authorityName").fill(data.authorityName);
    }
    if (data.authorityType !== undefined) {
      await this.page.locator("#legal-authorityType").selectOption(data.authorityType);
    }
    if (data.phone !== undefined) {
      await this.page.locator("#legal-phone").fill(data.phone);
    }
    if (data.email !== undefined) {
      await this.page.locator("#legal-email").fill(data.email);
    }
    if (data.address !== undefined) {
      await this.page.locator("#legal-address").fill(data.address);
    }
    if (data.note !== undefined) {
      await this.page.locator("#legal-note").fill(data.note);
    }
    await this.saveModal("legal");
  }

  /**
   * Deletes legal authority through the UI.
   */
  async deleteLegalAuthority(name: string): Promise<void> {
    await this.actionButton(this.rowByName("legal", name), "delete").click();
    await this.confirmSweetAlert();
  }

  async waitForLegalAuthorityVisible(name: string): Promise<void> {
    await this.waitForVisible(this.rowByName("legal", name));
  }

  async addAmenity(data: AmenityForm): Promise<void> {
    await this.openCreateModal("amenity");
    await this.page.locator("#amenity-name").fill(data.name);
    await this.page.locator("#amenity-amenityType").selectOption(data.amenityType);
    await this.page.locator("#amenity-address").fill(data.address);
    await this.setInputValue(this.page.locator("#amenity-latitude"), data.latitude);
    await this.setInputValue(this.page.locator("#amenity-longitude"), data.longitude);
    await this.setInputValue(this.page.locator("#amenity-distanceMeter"), data.distanceMeter ?? "500");
    await this.saveModal("amenity");
  }

  async waitForAmenityVisible(name: string): Promise<void> {
    await this.waitForVisible(this.rowByName("amenity", name));
  }

  async addPlanningMap(data: PlanningMapForm): Promise<void> {
    await this.openCreateModal("planning");
    await this.page.locator("#planning-mapType").fill(data.mapType);
    await this.page.locator("#planning-issuedBy").fill(data.issuedBy);
    await this.page.locator("#planning-issuedDate").fill(data.issuedDate);
    await this.page.locator("#planning-expiredDate").fill(data.expiredDate);
    await this.page.locator("#planning-note").fill(data.note ?? "");
    await this.setInputValue(this.page.locator("#planning-imageUrl"), data.existingImageUrl);
    await this.saveModal("planning");
  }

  async waitForPlanningMapVisible(mapType: string): Promise<void> {
    await this.waitForVisible(this.rowByName("planning", mapType));
  }

  /**
   * Deletes planning map through the UI.
   */
  async deletePlanningMap(mapType: string): Promise<void> {
    await this.actionButton(this.rowByName("planning", mapType), "delete").click();
    await this.confirmSweetAlert();
  }

  async addSupplier(data: SupplierForm): Promise<void> {
    await this.openCreateModal("supplier");
    await this.page.locator("#supplier-name").fill(data.name);
    await this.page.locator("#supplier-serviceType").fill(data.serviceType);
    await this.page.locator("#supplier-phone").fill(data.phone);
    await this.page.locator("#supplier-email").fill(data.email);
    await this.page.locator("#supplier-address").fill(data.address ?? "");
    await this.page.locator("#supplier-note").fill(data.note ?? "");
    await this.saveModal("supplier");
  }

  async waitForSupplierVisible(name: string): Promise<void> {
    await this.waitForVisible(this.rowByName("supplier", name));
  }

  async waitForValidationPopupContains(text: string | RegExp): Promise<void> {
    const popup = this.toastPopup();
    await this.waitForVisible(popup);
    await this.waitForLocatorText(popup, text);
    await this.confirmSweetAlert();
    await this.waitForHidden(popup);
  }
}
