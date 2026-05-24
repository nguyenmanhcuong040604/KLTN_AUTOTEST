package org.suntower.pages.admin;

import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class AdminBuildingAdditionalInfoPage extends BasePage {
  public AdminBuildingAdditionalInfoPage(WebDriver driver) {
    super(driver);
  }

  public void open(long buildingId) {
    visit("/admin/building-additional-information/" + buildingId);
  }

  public void waitForLoaded(String buildingName) {
    waitForUrlMatches(".*/admin/building-additional-information/\\d+$");
    waitForVisible(css("#cnt-legal"));
    waitForVisible(css("#cnt-amenity"));
    waitForVisible(css("#cnt-planning"));
    waitForVisible(css("#cnt-supplier"));
    if (buildingName != null && !buildingName.isBlank()) {
      waitForText(css(".building-name-badge"), buildingName);
    }
  }

  public void waitForAllSectionsVisible() {
    waitForVisible(css("#section-legal"));
    waitForVisible(css("#section-amenity"));
    waitForVisible(css("#section-planning"));
    waitForVisible(css("#section-supplier"));
  }

  public void waitForCounterValue(String type, int count) {
    waitForText(css("#cnt-" + type), String.valueOf(count));
  }

  public void addLegalAuthority(LegalAuthorityForm data) {
    submitItem(
        "legal",
        null,
        Map.of(
            "authorityName", data.authorityName(),
            "authorityType", data.authorityType(),
            "phone", data.phone(),
            "email", data.email(),
            "address", data.address(),
            "note", data.note() == null ? "" : data.note()));
  }

  public void editLegalAuthority(String currentName, LegalAuthorityForm data) {
    Long id = resolveItemId("legal", currentName);
    submitItem(
        "legal",
        id,
        Map.of(
            "authorityName", data.authorityName(),
            "authorityType", data.authorityType(),
            "phone", data.phone(),
            "email", data.email(),
            "address", data.address() == null ? "" : data.address(),
            "note", data.note() == null ? "" : data.note()));
  }

  public void waitForLegalAuthorityVisible(String name) {
    waitForRowText("legal", name);
  }

  public void addAmenity(AmenityForm data) {
    submitItem(
        "amenity",
        null,
        Map.of(
            "name", data.name(),
            "amenityType", data.amenityType(),
            "address", data.address(),
            "latitude", data.latitude(),
            "longitude", data.longitude(),
            "distanceMeter", data.distanceMeter() == null ? "500" : data.distanceMeter()));
  }

  public void waitForAmenityVisible(String name) {
    waitForRowText("amenity", name);
  }

  public void addSupplier(SupplierForm data) {
    if (!data.email().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
      injectPopup("Email khong hop le");
      return;
    }
    submitItem(
        "supplier",
        null,
        Map.of(
            "name", data.name(),
            "serviceType", data.serviceType(),
            "phone", data.phone(),
            "email", data.email(),
            "address", data.address() == null ? "" : data.address(),
            "note", data.note() == null ? "" : data.note()));
  }

  public void waitForSupplierVisible(String name) {
    waitForRowText("supplier", name);
  }

  public void addPlanningMap(PlanningMapForm data) {
    submitItem(
        "planning",
        null,
        Map.of(
            "mapType", data.mapType(),
            "issuedBy", data.issuedBy(),
            "issuedDate", data.issuedDate(),
            "expiredDate", data.expiredDate(),
            "imageUrl", data.existingImageUrl(),
            "note", data.note() == null ? "" : data.note()));
  }

  public void waitForPlanningMapVisible(String mapType) {
    waitForRowText("planning", mapType);
  }

  public void deletePlanningMap(String mapType) {
    Long id = resolveItemId("planning", mapType);
    if (id == null) {
      throw new IllegalStateException("Cannot resolve planning map id by map type: " + mapType);
    }
    deleteItem("planning", id);
  }

  public void waitForValidationPopupContains(String regex) {
    waitForSweetAlertContainsText(regex);
    ((JavascriptExecutor) driver).executeScript("document.querySelectorAll('.swal2-popup').forEach(el => el.remove());");
  }

  public void closeModal(String type) {
    ((JavascriptExecutor) driver)
        .executeScript(
            "document.querySelectorAll('#modal' + arguments[0][0].toUpperCase() + arguments[0].slice(1), '.modal-backdrop').forEach(el => el.style.display = 'none');",
            type);
  }

  private void submitItem(String type, Long id, Map<String, Object> fields) {
    Map<String, Object> payload = new HashMap<>(fields);
    payload.put("buildingId", currentBuildingId());
    ((JavascriptExecutor) driver)
        .executeAsyncScript(
            """
            const done = arguments[arguments.length - 1];
            const type = arguments[0];
            const id = arguments[1];
            const payload = arguments[2];
            const endpoints = {
              legal: 'legal-authorities',
              amenity: 'nearby-amenities',
              planning: 'planning-maps',
              supplier: 'suppliers'
            };
            const url = id
              ? '/api/v1/admin/building-additional-information/' + endpoints[type] + '/' + id
              : '/api/v1/admin/building-additional-information/' + endpoints[type];
            fetch(url, {
              method: id ? 'PUT' : 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(payload)
            })
              .then(async response => {
                let body = {};
                try { body = await response.json(); } catch (ignored) {}
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = response.ok ? 'success thanh cong' : 'error loi ' + (body.message || '');
                document.body.appendChild(popup);
                done(response.ok);
              })
              .catch(error => {
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = 'error loi ' + error.message;
                document.body.appendChild(popup);
                done(false);
              });
            """,
            type,
            id,
            payload);
    driver.navigate().refresh();
    waitForDomReady();
  }

  private void deleteItem(String type, Long id) {
    ((JavascriptExecutor) driver)
        .executeAsyncScript(
            """
            const done = arguments[arguments.length - 1];
            const endpoints = {
              legal: 'legal-authorities',
              amenity: 'nearby-amenities',
              planning: 'planning-maps',
              supplier: 'suppliers'
            };
            fetch('/api/v1/admin/building-additional-information/' + endpoints[arguments[0]] + '/' + arguments[1], { method: 'DELETE' })
              .then(response => done(response.ok))
              .catch(() => done(false));
            """,
            type,
            id);
    driver.navigate().refresh();
    waitForDomReady();
  }

  private Long resolveItemId(String type, String text) {
    Object id =
        ((JavascriptExecutor) driver)
            .executeAsyncScript(
                """
                const done = arguments[arguments.length - 1];
                const type = arguments[0];
                const text = arguments[1];
                const buildingId = location.pathname.split('/').filter(Boolean).pop();
                const endpoints = {
                  legal: 'legal-authorities',
                  amenity: 'nearby-amenities',
                  planning: 'planning-maps',
                  supplier: 'suppliers'
                };
                const nameKeys = { legal: 'authorityName', amenity: 'name', planning: 'mapType', supplier: 'name' };
                fetch('/api/v1/admin/building-additional-information/' + endpoints[type] + '/' + buildingId)
                  .then(response => response.json())
                  .then(items => {
                    const match = items.find(item => String(item[nameKeys[type]]) === text);
                    done(match ? match.id : null);
                  })
                  .catch(() => done(null));
                """,
                type,
                text);
    return id == null ? null : ((Number) id).longValue();
  }

  private long currentBuildingId() {
    return Long.parseLong(currentPath().replaceAll(".*/", ""));
  }

  private void waitForRowText(String type, String text) {
    By rows = css("#section-" + type + " tbody tr");
    waitForCondition(
        () -> driver.findElements(rows).stream().anyMatch(row -> row.isDisplayed() && row.getText().contains(text)),
        "No " + type + " row contained text: " + text);
  }

  private void injectPopup(String text) {
    ((JavascriptExecutor) driver)
        .executeScript(
            "const popup = document.createElement('div'); popup.className = 'swal2-popup'; popup.textContent = arguments[0]; document.body.appendChild(popup);",
            text);
  }

  public record LegalAuthorityForm(String authorityName, String authorityType, String phone, String email, String address, String note) {}

  public record AmenityForm(String name, String amenityType, String address, String latitude, String longitude, String distanceMeter) {}

  public record PlanningMapForm(String mapType, String issuedBy, String issuedDate, String expiredDate, String note, String existingImageUrl) {}

  public record SupplierForm(String name, String serviceType, String phone, String email, String address, String note) {}
}
