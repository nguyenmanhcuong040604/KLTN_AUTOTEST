package org.suntower.fixtures.state;

import io.restassured.response.Response;
import java.util.Map;
import org.suntower.core.AppConfig;
import org.suntower.fixtures.auth.TestAccountResolver;
import org.suntower.fixtures.data.TestDataFactory;

public final class TestStateBuilder {
  private TestStateBuilder() {}

  public static CreatedCustomer createCustomer() {
    TestDataFactory.CustomerPayload payload = TestDataFactory.buildCustomerPayload();
    Response response =
        TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
            .contentType("application/json")
            .body(
                Map.of(
                    "username", payload.username(),
                    "password", payload.password(),
                    "fullName", payload.fullName(),
                    "phone", payload.phone(),
                    "email", payload.email(),
                    "staffIds", java.util.List.of()))
            .post("/api/v1/admin/customers");

    if (response.statusCode() >= 400) {
      throw new IllegalStateException("Cannot create customer fixture. HTTP " + response.statusCode() + ": " + response.asString());
    }

    Integer id = response.jsonPath().getInt("id");
    return new CreatedCustomer(id, payload.username(), payload.password(), payload.fullName(), payload.email());
  }

  public static void deleteCustomer(Integer id) {
    if (id == null) {
      return;
    }
    TestStateSession.authenticatedAs(TestAccountResolver.rememberedOrDefault("admin"), AppConfig.get().defaultPassword())
        .delete("/api/v1/admin/customers/" + id);
  }

  public record CreatedCustomer(Integer id, String username, String password, String fullName, String email) {}
}
