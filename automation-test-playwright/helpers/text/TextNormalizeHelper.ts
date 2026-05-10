export class TextNormalizeHelper {
  private static readonly mojibakeMarkers = /[\u00c3\u00c2\u00c4\u00c6]/;

  private static repairMojibake(value: string): string {
    if (!this.mojibakeMarkers.test(value)) {
      return value;
    }

    try {
      return Buffer.from(value, "latin1").toString("utf8");
    } catch {
      return value;
    }
  }

  static normalizeLooseText(value: string): string {
    return this.repairMojibake(value)
      .normalize("NFD")
      .replace(/\p{Diacritic}/gu, "")
      .replace(/\u0111/g, "d")
      .replace(/\u0110/g, "D")
      .replace(/\s+/g, " ")
      .trim()
      .toLowerCase();
  }
}
