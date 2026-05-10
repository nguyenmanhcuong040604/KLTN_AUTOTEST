export class Invariant {
  /**
   * Throws when a required condition is not met.
   */
  static ensure(condition: boolean, message: string): void {
    if (!condition) {
      throw new Error(message);
    }
  }

  /**
   * Throws when an actual value is different from the expected value.
   */
  static equal<T>(actual: T, expected: T, message: string): void {
    if (actual !== expected) {
      throw new Error(`${message} Expected: ${String(expected)}. Actual: ${String(actual)}.`);
    }
  }
}
