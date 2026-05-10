type LogLevel = "debug" | "info" | "warn" | "error" | "silent";

const severityByLevel: Record<LogLevel, number> = {
  debug: 10,
  info: 20,
  warn: 30,
  error: 40,
  silent: 50
};

const normalizeLogLevel = (value: string | undefined): LogLevel => {
  const normalized = value?.trim().toLowerCase();
  if (
    normalized === "debug" ||
    normalized === "info" ||
    normalized === "warn" ||
    normalized === "error" ||
    normalized === "silent"
  ) {
    return normalized;
  }

  return process.env.CI === "true" ? "info" : "warn";
};

export class Logger {
  private static readonly configuredLevel = normalizeLogLevel(process.env.LOG_LEVEL);

  private static shouldLog(level: LogLevel): boolean {
    return severityByLevel[level] >= severityByLevel[this.configuredLevel];
  }

  private static format(scope: string, message: string): string {
    return scope ? `[${scope}] ${message}` : message;
  }

  static debug(scope: string, message: string, ...args: unknown[]): void {
    if (this.shouldLog("debug")) {
      console.debug(this.format(scope, message), ...args);
    }
  }

  static info(scope: string, message: string, ...args: unknown[]): void {
    if (this.shouldLog("info")) {
      console.info(this.format(scope, message), ...args);
    }
  }

  static warn(scope: string, message: string, ...args: unknown[]): void {
    if (this.shouldLog("warn")) {
      console.warn(this.format(scope, message), ...args);
    }
  }

  static error(scope: string, message: string, ...args: unknown[]): void {
    if (this.shouldLog("error")) {
      console.error(this.format(scope, message), ...args);
    }
  }
}
