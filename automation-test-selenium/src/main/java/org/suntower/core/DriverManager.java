package org.suntower.core;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public final class DriverManager {
  private static final ThreadLocal<WebDriver> DRIVERS = new ThreadLocal<>();

  private DriverManager() {}

  public static WebDriver createDriver() {
    WebDriver existing = DRIVERS.get();
    if (existing != null) {
      return existing;
    }

    AppConfig config = AppConfig.get();
    WebDriver driver =
        switch (config.browser()) {
          case "firefox" -> new FirefoxDriver(firefoxOptions(config));
          case "edge" -> new EdgeDriver(edgeOptions(config));
          case "chrome" -> new ChromeDriver(chromeOptions(config));
          default -> throw new IllegalArgumentException("Unsupported browser: " + config.browser());
        };

    driver.manage().timeouts().pageLoadTimeout(config.navigationTimeout());
    driver.manage().timeouts().scriptTimeout(config.actionTimeout());
    driver.manage().window().maximize();
    DRIVERS.set(driver);
    return driver;
  }

  public static WebDriver getDriver() {
    WebDriver driver = DRIVERS.get();
    if (driver == null) {
      throw new IllegalStateException("WebDriver has not been created for this thread.");
    }
    return driver;
  }

  public static void quitDriver() {
    WebDriver driver = DRIVERS.get();
    if (driver != null) {
      try {
        driver.quit();
      } finally {
        DRIVERS.remove();
      }
    }
  }

  private static ChromeOptions chromeOptions(AppConfig config) {
    ChromeOptions options = new ChromeOptions();
    if (config.headless()) {
      options.addArguments("--headless=new");
    }
    options.addArguments("--disable-gpu", "--window-size=1440,1000", "--remote-allow-origins=*");
    return options;
  }

  private static FirefoxOptions firefoxOptions(AppConfig config) {
    FirefoxOptions options = new FirefoxOptions();
    if (config.headless()) {
      options.addArguments("-headless");
    }
    return options;
  }

  private static EdgeOptions edgeOptions(AppConfig config) {
    EdgeOptions options = new EdgeOptions();
    if (config.headless()) {
      options.addArguments("--headless=new");
    }
    options.addArguments("--disable-gpu", "--window-size=1440,1000");
    return options;
  }
}
