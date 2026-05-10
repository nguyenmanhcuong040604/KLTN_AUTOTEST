import crypto from "node:crypto";
import type { APIRequestContext } from "@playwright/test";
import { env } from "@helpers-runtime/env";
import { MySqlDbClient } from "@helpers-test-state/MySqlDbClient";

type OtpHookPayload = {
  email?: string;
  purpose?: string;
  otp?: string;
  status?: string;
};

type EmailVerificationRow = {
  id: number;
  email: string;
  purpose: string;
  status: string;
  expiresAt: string | Date | null;
  verifiedAt: string | Date | null;
  usedAt: string | Date | null;
};

class OtpDatabase {
  static hashOtp(otp: string): string {
    return crypto.createHash("sha256").update(otp).digest("hex");
  }

  static async latest(email: string, purpose: string): Promise<EmailVerificationRow | null> {
    const rows = await MySqlDbClient.query<EmailVerificationRow>(
      `
        SELECT
          id,
          email,
          purpose,
          status,
          expires_at AS expiresAt,
          verified_at AS verifiedAt,
          used_at AS usedAt
        FROM email_verification
        WHERE email = ? AND purpose = ?
        ORDER BY created_at DESC, id DESC
        LIMIT 1
      `,
      [email.trim().toLowerCase(), purpose]
    );

    return rows[0] ?? null;
  }

  static async latestVerificationId(email: string, purpose: string): Promise<number> {
    const rows = await MySqlDbClient.query<{ maxId: number | null }>(
      "SELECT COALESCE(MAX(id), 0) AS maxId FROM email_verification WHERE email = ? AND purpose = ?",
      [email.trim().toLowerCase(), purpose]
    );
    return Number(rows[0]?.maxId ?? 0);
  }

  static async deleteVerificationsAfter(email: string, purpose: string, minId: number): Promise<void> {
    await MySqlDbClient.execute("DELETE FROM email_verification WHERE email = ? AND purpose = ? AND id > ?", [
      email.trim().toLowerCase(),
      purpose,
      minId
    ]);
  }

  static async setLatestPendingOtp(email: string, purpose: string, otp: string): Promise<void> {
    const normalizedEmail = email.trim().toLowerCase();
    const latest = await this.latest(normalizedEmail, purpose);
    if (!latest) {
      throw new Error(`Khong tim thay OTP cho ${normalizedEmail} / ${purpose}.`);
    }

    await MySqlDbClient.execute(
      `
        UPDATE email_verification
        SET otp_hash = ?,
            status = 'PENDING',
            expires_at = DATE_ADD(NOW(), INTERVAL 10 MINUTE),
            verified_at = NULL,
            used_at = NULL
        WHERE id = ?
      `,
      [this.hashOtp(otp), latest.id]
    );
  }

  static async expireLatestPendingOtp(email: string, purpose: string): Promise<void> {
    const normalizedEmail = email.trim().toLowerCase();
    const latest = await this.latest(normalizedEmail, purpose);
    if (!latest) {
      throw new Error(`Khong tim thay OTP cho ${normalizedEmail} / ${purpose}.`);
    }

    await MySqlDbClient.execute(
      `
        UPDATE email_verification
        SET status = 'PENDING',
            expires_at = DATE_SUB(NOW(), INTERVAL 1 MINUTE),
            verified_at = NULL,
            used_at = NULL
        WHERE id = ?
      `,
      [latest.id]
    );
  }
}

export class OtpAccessHelper {
  private static readonly fallbackPinnedOtp = "246810";
  private static readonly pollingIntervalMs = 250;

  private static hookHeaders(): Record<string, string> {
    return env.testSupportOtpToken ? { "X-Test-Hook-Token": env.testSupportOtpToken } : {};
  }

  private static latestHookPath(email: string, purpose: string): string {
    const params = new URLSearchParams({ email: email.trim().toLowerCase(), purpose });
    return `/api/test-support/otp-support/latest?${params.toString()}`;
  }

  private static expireHookPath(email: string, purpose: string): string {
    const params = new URLSearchParams({ email: email.trim().toLowerCase(), purpose });
    return `/api/test-support/otp-support/expire?${params.toString()}`;
  }

  static async latestOtp(context: APIRequestContext, email: string, purpose: string): Promise<string> {
    const deadline = Date.now() + env.expectTimeout;

    while (Date.now() <= deadline) {
      if (env.testSupportOtpToken) {
        const response = await context.get(this.latestHookPath(email, purpose), {
          failOnStatusCode: false,
          headers: this.hookHeaders()
        });

        if (response.status() === 200) {
          try {
            const body = (await response.json()) as OtpHookPayload;
            if (body.otp) {
              return body.otp;
            }
          } catch {
            // Some environments return an HTML error page behind the same status.
          }
        }
      }

      const latest = await OtpDatabase.latest(email, purpose);
      if (latest) {
        await OtpDatabase.setLatestPendingOtp(email, purpose, this.fallbackPinnedOtp);
        return this.fallbackPinnedOtp;
      }

      await new Promise((resolve) => setTimeout(resolve, this.pollingIntervalMs));
    }

    throw new Error(`Khong doc duoc OTP qua test hook va cung khong tim thay OTP trong DB cho ${email}/${purpose}.`);
  }

  static async expireLatestOtp(context: APIRequestContext, email: string, purpose: string): Promise<void> {
    if (env.testSupportOtpToken) {
      const response = await context.post(this.expireHookPath(email, purpose), {
        failOnStatusCode: false,
        headers: this.hookHeaders()
      });

      if (response.status() === 200) {
        return;
      }
    }

    await OtpDatabase.expireLatestPendingOtp(email, purpose);
  }
}

export const OtpTestSupportHelper = OtpDatabase;
