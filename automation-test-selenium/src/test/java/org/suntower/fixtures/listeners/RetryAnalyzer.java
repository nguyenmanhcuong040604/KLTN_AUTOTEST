package org.suntower.fixtures.listeners;

import org.suntower.core.AppConfig;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {
  private int attempts;

  @Override
  public boolean retry(ITestResult result) {
    int maxRetries = AppConfig.get().retries();
    if (attempts < maxRetries) {
      attempts++;
      return true;
    }
    return false;
  }
}
