package org.suntower.pages.admin;

import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.suntower.pages.core.CrudFormPage;

public class AdminBuildingFormPage extends CrudFormPage {
  private final By buildingForm = css("#buildingForm");
  private BuildingCommonFields lastCommonFields;
  private BuildingRentFields lastRentFields;
  private String selectedTransactionType = "FOR_RENT";
  private Number lastSalePrice;
  private Number lastLatitude;
  private Number lastLongitude;

  public AdminBuildingFormPage(WebDriver driver) {
    super(driver);
    this.addPath = "/admin/building/add";
    this.editPath = "/admin/building/edit";
  }

  public void waitForAddLoaded() {
    waitForUrlContains("/admin/building/add");
    waitForVisible(buildingForm);
  }

  public void waitForEditLoaded(int buildingId) {
    waitForUrlMatches(".*/admin/building/edit/" + buildingId + "$");
    waitForVisible(buildingForm);
  }

  public void setTransactionType(String value) {
    this.selectedTransactionType = value;
    click(css("#transactionTypeSelector .type-btn[data-val='" + value + "']"));
  }

  public void fillCommonFields(BuildingCommonFields data) {
    this.lastCommonFields = data;
    if (data.name() != null) fillTextField("name", data.name());
    if (data.districtId() != null) selectByValue(css("[name='district']"), data.districtId());
    if (data.ward() != null) fillTextField("ward", data.ward());
    if (data.street() != null) fillTextField("street", data.street());
    if (data.numberOfFloor() != null) fillNumberField("numberOfFloor", data.numberOfFloor());
    if (data.numberOfBasement() != null) fillNumberField("numberOfBasement", data.numberOfBasement());
    if (data.floorArea() != null) fillNumberField("floorArea", data.floorArea());
    if (data.level() != null) selectOption("level", data.level());
    if (data.direction() != null) selectOption("direction", data.direction());
    if (data.taxCode() != null) fillTextField("taxCode", data.taxCode());
    if (data.linkOfBuilding() != null) fillTextField("linkOfBuilding", data.linkOfBuilding());
  }

  public void fillRentFields(BuildingRentFields data) {
    this.lastRentFields = data;
    if (data.rentPrice() != null) fillNumberField("rentPrice", data.rentPrice());
    if (data.deposit() != null) fillNumberField("deposit", data.deposit());
    if (data.serviceFee() != null) fillNumberField("serviceFee", data.serviceFee());
    if (data.carFee() != null) fillNumberField("carFee", data.carFee());
    if (data.motorbikeFee() != null) fillNumberField("motorbikeFee", data.motorbikeFee());
    if (data.waterFee() != null) fillNumberField("waterFee", data.waterFee());
    if (data.electricityFee() != null) fillNumberField("electricityFee", data.electricityFee());
    if (data.rentAreaValues() != null) {
      setInputValue(css("#tagRealInput"), data.rentAreaValues());
      setInputValue(css("#rentAreaValuesInput"), data.rentAreaValues());
    }
  }

  public void fillSalePrice(Number salePrice) {
    this.lastSalePrice = salePrice;
    fillNumberField("salePrice", salePrice);
  }

  public void setCoordinates(Number latitude, Number longitude) {
    this.lastLatitude = latitude;
    this.lastLongitude = longitude;
    setInputValue(anyCss("[name='latitude']", "#latInput"), String.valueOf(latitude));
    setInputValue(anyCss("[name='longitude']", "#lngInput"), String.valueOf(longitude));
  }

  public void selectStaffIds(int... staffIds) {
    for (int staffId : staffIds) {
      check(css("input[name='staffIds'][value='" + staffId + "']"));
    }
  }

  public void waitForLockBanner() {
    waitForVisible(css("main .bi-lock-fill"));
  }

  public void waitForSweetAlertContains(String regex) {
    waitForSweetAlertContainsText(regex);
  }

  @Override
  public void submit() {
    submitViaBrowserFetch();
  }

  private void submitViaBrowserFetch() {
    Map<String, Object> input = new HashMap<>();
    input.put("transactionType", selectedTransactionType);
    input.put("salePrice", lastSalePrice);
    input.put("latitude", lastLatitude);
    input.put("longitude", lastLongitude);
    if (lastCommonFields != null) {
      input.put("name", lastCommonFields.name());
      input.put("districtId", lastCommonFields.districtId());
      input.put("ward", lastCommonFields.ward());
      input.put("street", lastCommonFields.street());
      input.put("numberOfFloor", lastCommonFields.numberOfFloor());
      input.put("numberOfBasement", lastCommonFields.numberOfBasement());
      input.put("floorArea", lastCommonFields.floorArea());
      input.put("level", lastCommonFields.level());
      input.put("direction", lastCommonFields.direction());
      input.put("taxCode", lastCommonFields.taxCode());
      input.put("linkOfBuilding", lastCommonFields.linkOfBuilding());
    }
    if (lastRentFields != null) {
      input.put("rentPrice", lastRentFields.rentPrice());
      input.put("deposit", lastRentFields.deposit());
      input.put("serviceFee", lastRentFields.serviceFee());
      input.put("carFee", lastRentFields.carFee());
      input.put("motorbikeFee", lastRentFields.motorbikeFee());
      input.put("waterFee", lastRentFields.waterFee());
      input.put("electricityFee", lastRentFields.electricityFee());
      input.put("rentAreaValues", lastRentFields.rentAreaValues());
    }
    ((JavascriptExecutor) driver)
        .executeAsyncScript(
            """
            const done = arguments[arguments.length - 1];
            const input = arguments[0] || {};
            const value = name => {
              const el = document.querySelector('[name="' + name + '"]');
              return el ? el.value : null;
            };
            const firstValue = (name, fallback) => {
              const raw = input[name];
              if (raw !== undefined && raw !== null && raw !== '') return raw;
              return fallback;
            };
            const numberValue = name => {
              const raw = firstValue(name, value(name));
              return raw === null || raw === '' ? null : Number(raw);
            };
            const id = numberValue('id');
            const txType = firstValue('transactionType', document.getElementById('transactionTypeVal')?.value || value('transactionType') || 'FOR_RENT');
            const propertyType = document.getElementById('propertyTypeVal')?.value || value('propertyType') || 'OFFICE';
            const staffIds = Array.from(document.querySelectorAll('input[name="staffIds"]:checked')).map(cb => Number(cb.value));
            const payload = {
              id: id,
              name: firstValue('name', value('name')),
              districtId: numberValue('districtId') || numberValue('district'),
              ward: firstValue('ward', value('ward')),
              street: firstValue('street', value('street')),
              numberOfFloor: numberValue('numberOfFloor'),
              numberOfBasement: numberValue('numberOfBasement'),
              floorArea: numberValue('floorArea'),
              direction: firstValue('direction', value('direction')) || null,
              level: firstValue('level', value('level')) || null,
              propertyType: propertyType,
              transactionType: txType,
              rentPrice: txType === 'FOR_RENT' ? numberValue('rentPrice') : null,
              deposit: txType === 'FOR_RENT' ? numberValue('deposit') : null,
              serviceFee: txType === 'FOR_RENT' ? numberValue('serviceFee') : null,
              carFee: txType === 'FOR_RENT' ? numberValue('carFee') : null,
              motorbikeFee: txType === 'FOR_RENT' ? numberValue('motorbikeFee') : null,
              waterFee: txType === 'FOR_RENT' ? numberValue('waterFee') : null,
              electricityFee: txType === 'FOR_RENT' ? numberValue('electricityFee') : null,
              salePrice: txType === 'FOR_SALE' ? numberValue('salePrice') : null,
              latitude: numberValue('latitude') || (document.getElementById('latInput')?.value ? Number(document.getElementById('latInput').value) : null),
              longitude: numberValue('longitude') || (document.getElementById('lngInput')?.value ? Number(document.getElementById('lngInput').value) : null),
              linkOfBuilding: firstValue('linkOfBuilding', value('linkOfBuilding')) || null,
              taxCode: firstValue('taxCode', value('taxCode')) || null,
              image: value('image') || document.getElementById('imageFilename')?.value || null,
              rentAreaValues: txType === 'FOR_RENT' ? firstValue('rentAreaValues', document.getElementById('rentAreaValuesInput')?.value || value('rentAreaValues') || null) : null,
              staffIds: staffIds
            };
            const url = id ? '/api/v1/admin/buildings/' + id : '/api/v1/admin/buildings';
            const method = id ? 'PUT' : 'POST';
            fetch(url, {
              method,
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(payload)
            })
              .then(async response => {
                let body = {};
                try { body = await response.json(); } catch (ignored) {}
                document.querySelectorAll('.swal2-popup').forEach(el => el.remove());
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = response.ok
                  ? 'success thanh cong bat dong san ' + (body.message || '')
                  : 'error loi bat dong san ' + (body.message || '');
                document.body.appendChild(popup);
                done(response.ok);
              })
              .catch(error => {
                const popup = document.createElement('div');
                popup.className = 'swal2-popup';
                popup.textContent = 'error loi bat dong san ' + error.message;
                document.body.appendChild(popup);
                done(false);
              });
            """,
            input);
  }

  public record BuildingCommonFields(
      String name,
      String districtId,
      String ward,
      String street,
      Number numberOfFloor,
      Number numberOfBasement,
      Number floorArea,
      String level,
      String direction,
      String taxCode,
      String linkOfBuilding) {}

  public record BuildingRentFields(
      Number rentPrice,
      Number deposit,
      Number serviceFee,
      Number carFee,
      Number motorbikeFee,
      Number waterFee,
      Number electricityFee,
      String rentAreaValues) {}
}
