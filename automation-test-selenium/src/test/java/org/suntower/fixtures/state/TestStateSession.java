package org.suntower.fixtures.state;

import io.restassured.RestAssured;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.suntower.core.AppConfig;

public final class TestStateSession {
  private TestStateSession() {}

  public static RequestSpecification request() {
    return RestAssured.given().baseUri(AppConfig.get().baseUrl()).accept("application/json");
  }

  public static RequestSpecification authenticatedAs(String username, String password) {
    Response response = request()
        .contentType("application/json")
        .body("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
        .post("/api/v1/auth/login");
    Cookies cookies = response.detailedCookies();
    return request().cookies(cookies);
  }
}
