package org.suntower.helpers.text;

import java.text.Normalizer;
import java.util.Locale;

public final class TextNormalizeHelper {
  private TextNormalizeHelper() {}

  public static String normalizeLooseText(String value) {
    if (value == null) {
      return "";
    }
    return stripDiacritics(value).strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
  }

  public static String stripDiacritics(String value) {
    return Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace("đ", "d")
        .replace("Đ", "D")
        .replaceAll("\\p{M}", "");
  }
}
