package org.suntower.core;

import java.lang.reflect.Constructor;
import org.openqa.selenium.WebDriver;

public class PageObjectFactory {
  private final WebDriver driver;

  public PageObjectFactory(WebDriver driver) {
    this.driver = driver;
  }

  public <T> T create(Class<T> pageObjectClass) {
    try {
      Constructor<T> constructor = pageObjectClass.getConstructor(WebDriver.class);
      return constructor.newInstance(driver);
    } catch (ReflectiveOperationException error) {
      throw new IllegalArgumentException("Cannot create page object: " + pageObjectClass.getName(), error);
    }
  }
}
