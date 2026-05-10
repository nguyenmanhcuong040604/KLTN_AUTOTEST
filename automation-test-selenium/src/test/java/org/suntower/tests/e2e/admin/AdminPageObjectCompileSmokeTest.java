package org.suntower.tests.e2e.admin;

import static org.assertj.core.api.Assertions.assertThat;

import org.suntower.fixtures.BaseTest;
import org.suntower.pages.admin.AdminBuildingFormPage;
import org.suntower.pages.admin.AdminBuildingListPage;
import org.suntower.pages.admin.AdminContractListPage;
import org.suntower.pages.admin.AdminCustomerFormPage;
import org.suntower.pages.admin.AdminCustomerListPage;
import org.suntower.pages.admin.AdminDashboardPage;
import org.suntower.pages.admin.AdminStaffFormPage;
import org.suntower.pages.admin.AdminStaffListPage;
import org.testng.annotations.Test;

public class AdminPageObjectCompileSmokeTest extends BaseTest {
  @Test(
      groups = {"framework"},
      description = "[FRAMEWORK-ADMIN-001] should instantiate foundational admin page objects")
  public void shouldInstantiateFoundationalAdminPageObjects() {
    assertThat(pageObjects.create(AdminDashboardPage.class)).isNotNull();
    assertThat(pageObjects.create(AdminBuildingListPage.class)).isNotNull();
    assertThat(pageObjects.create(AdminBuildingFormPage.class)).isNotNull();
    assertThat(pageObjects.create(AdminCustomerListPage.class)).isNotNull();
    assertThat(pageObjects.create(AdminCustomerFormPage.class)).isNotNull();
    assertThat(pageObjects.create(AdminStaffListPage.class)).isNotNull();
    assertThat(pageObjects.create(AdminStaffFormPage.class)).isNotNull();
    assertThat(pageObjects.create(AdminContractListPage.class)).isNotNull();
  }
}
