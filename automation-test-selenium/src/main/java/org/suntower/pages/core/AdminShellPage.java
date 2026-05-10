package org.suntower.pages.core;

import org.openqa.selenium.WebDriver;
import org.suntower.core.BasePage;

public class AdminShellPage extends BasePage {
  protected String path;

  public AdminShellPage(WebDriver driver) {
    super(driver);
  }

  public void open() {
    if (path == null || path.isBlank()) {
      throw new IllegalStateException("This admin page does not define a path.");
    }
    visit(path);
  }

  public void goToBuildings() {
    click(linkByHref("/admin/building/list"));
  }

  public void goToCustomers() {
    click(linkByHref("/admin/customer/list"));
  }

  public void goToContracts() {
    click(linkByHref("/admin/contract/list"));
  }

  public void goToSaleContracts() {
    click(linkByHref("/admin/sale-contract/list"));
  }

  public void goToInvoices() {
    click(linkByHref("/admin/invoice/list"));
  }

  public void goToReports() {
    click(linkByHref("/admin/report"));
  }

  public void goToStaffs() {
    click(linkByHref("/admin/staff/list"));
  }

  public void goToProfile() {
    click(linkByHref("/admin/profile"));
  }
}
