package org.suntower.tests.e2e.framework;

import static org.assertj.core.api.Assertions.assertThat;

import org.suntower.fixtures.BaseTest;
import org.suntower.pages.components.BootstrapModalComponent;
import org.suntower.pages.components.TableComponent;
import org.suntower.pages.core.AdminShellPage;
import org.suntower.pages.core.CrudDetailPage;
import org.suntower.pages.core.CrudFormPage;
import org.suntower.pages.core.CrudListPage;
import org.suntower.pages.core.CustomerShellPage;
import org.suntower.pages.core.StaffShellPage;
import org.testng.annotations.Test;

public class FrameworkCompileSmokeTest extends BaseTest {
  @Test(
      groups = {"framework"},
      description = "[FRAMEWORK-001] should instantiate shared page object infrastructure")
  public void shouldInstantiateSharedInfrastructure() {
    assertThat(pageObjects.create(TableComponentHarness.class)).isNotNull();
    assertThat(pageObjects.create(BootstrapModalComponent.class)).isNotNull();
    assertThat(pageObjects.create(CrudListPage.class)).isNotNull();
    assertThat(pageObjects.create(CrudFormPage.class)).isNotNull();
    assertThat(pageObjects.create(CrudDetailPage.class)).isNotNull();
    assertThat(pageObjects.create(AdminShellPage.class)).isNotNull();
    assertThat(pageObjects.create(CustomerShellPage.class)).isNotNull();
    assertThat(pageObjects.create(StaffShellPage.class)).isNotNull();
  }

  public static class TableComponentHarness extends TableComponent {
    public TableComponentHarness(org.openqa.selenium.WebDriver driver) {
      super(driver, "tbody");
    }
  }
}
