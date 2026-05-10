package org.suntower.fixtures;

import io.qameta.allure.Allure;
import java.util.function.Supplier;

public final class StepHelper {
  private StepHelper() {}

  public static <T> T arrange(String description, Supplier<T> action) {
    return Allure.step("Arrange: " + description, action::get);
  }

  public static void arrange(String description, Runnable action) {
    Allure.step("Arrange: " + description, action::run);
  }

  public static <T> T act(String description, Supplier<T> action) {
    return Allure.step("Act: " + description, action::get);
  }

  public static void act(String description, Runnable action) {
    Allure.step("Act: " + description, action::run);
  }

  public static <T> T assertStep(String description, Supplier<T> action) {
    return Allure.step("Assert: " + description, action::get);
  }

  public static void assertStep(String description, Runnable action) {
    Allure.step("Assert: " + description, action::run);
  }
}
