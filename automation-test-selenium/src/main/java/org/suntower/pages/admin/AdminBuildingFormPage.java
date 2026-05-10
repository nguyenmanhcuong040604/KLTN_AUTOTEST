package org.suntower.pages.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.suntower.pages.core.CrudFormPage;

public class AdminBuildingFormPage extends CrudFormPage {
  private final By buildingForm = css("#buildingForm");

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
    click(css("#transactionTypeSelector .type-btn[data-val='" + value + "']"));
  }

  public void fillCommonFields(BuildingCommonFields data) {
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
    fillNumberField("salePrice", salePrice);
  }

  public void setCoordinates(Number latitude, Number longitude) {
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
