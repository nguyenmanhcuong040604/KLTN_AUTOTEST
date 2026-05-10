package org.suntower.fixtures.listeners;

import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.suntower.core.DriverManager;
import org.testng.IAnnotationTransformer;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;

public class TestLifecycleListener implements ITestListener, IAnnotationTransformer {
  @Override
  public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    if (annotation.getRetryAnalyzerClass() == null) {
      annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
  }

  @Override
  public void onTestFailure(ITestResult result) {
    attachBrowserState();
  }

  private void attachBrowserState() {
    try {
      WebDriver driver = DriverManager.getDriver();
      Allure.addAttachment("current-url", "text/plain", driver.getCurrentUrl());
      Allure.addAttachment("page-title", "text/plain", driver.getTitle());
      if (driver instanceof TakesScreenshot screenshotDriver) {
        byte[] screenshot = screenshotDriver.getScreenshotAs(OutputType.BYTES);
        Allure.addAttachment("screenshot", "image/png", new ByteArrayInputStream(screenshot), ".png");
      }
    } catch (RuntimeException ignored) {
      // Failure attachments should not mask the original test error.
    }
  }
}
