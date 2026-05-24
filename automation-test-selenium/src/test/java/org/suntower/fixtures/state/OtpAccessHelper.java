package org.suntower.fixtures.state;

import io.restassured.response.Response;
import org.suntower.core.AppConfig;

public final class OtpAccessHelper {
  private OtpAccessHelper() {}

  public static String latestOtp(String email, String purpose) {
    Response response =
        TestStateSession.request()
            .header("X-Test-Hook-Token", AppConfig.get().testSupportOtpToken())
            .queryParam("email", email)
            .queryParam("purpose", purpose)
            .get("/api/test-support/otp/latest");

    if (response.statusCode() >= 400) {
      throw new IllegalStateException("Cannot fetch latest OTP. HTTP " + response.statusCode());
    }
    String nestedOtp = response.jsonPath().getString("data.otp");
    return nestedOtp != null ? nestedOtp : response.jsonPath().getString("otp");
  }
}
