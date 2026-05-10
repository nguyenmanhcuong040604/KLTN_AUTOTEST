package org.suntower.fixtures;

import org.openqa.selenium.WebDriver;
import org.suntower.core.AppConfig;
import org.suntower.core.DriverManager;
import org.suntower.core.PageObjectFactory;
import org.suntower.fixtures.auth.RoleSession;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTest {
  protected WebDriver driver;
  protected AppConfig config;
  protected PageObjectFactory pageObjects;
  protected RoleSession adminSession;
  protected RoleSession staffSession;
  protected RoleSession customerSession;

  @BeforeMethod(alwaysRun = true)
  public void setUp() {
    config = AppConfig.get();
    driver = DriverManager.createDriver();
    pageObjects = new PageObjectFactory(driver);
    adminSession = new RoleSession(driver, "admin");
    staffSession = new RoleSession(driver, "staff");
    customerSession = new RoleSession(driver, "customer");
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() {
    DriverManager.quitDriver();
  }
}
