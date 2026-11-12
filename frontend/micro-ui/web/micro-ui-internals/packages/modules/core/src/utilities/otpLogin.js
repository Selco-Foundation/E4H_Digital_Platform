/**
 * Pure helpers for the employee "Login with mobile number & OTP" flow.
 *
 * Nothing in this file renders UI or calls an API, so every function can be
 * unit tested on its own and reused by both the component and the service.
 */

// Fallbacks used only when the backend response leaves these values out
// (_send normally returns resendAfterSeconds / otpValiditySeconds and
// _verify returns loginTicketValiditySeconds).
export const RESEND_SECONDS = 30;
export const OTP_VALIDITY_SECONDS = 300;
export const LOGIN_TICKET_VALIDITY_SECONDS = 120;

// Upper bound for any timer value from the backend, so a typo such as 30000
// can never lock the resend button for hours.
const MAX_TIMER_SECONDS = 3600;

// Used until the backend confirms the real OTP length (see getOtpLength).
const DEFAULT_OTP_LENGTH = 6;

// Bounds that keep a mistyped config value from rendering 0 or 50 OTP boxes.
const MIN_OTP_LENGTH = 4;
const MAX_OTP_LENGTH = 8;

// Indian mobile number: exactly 10 digits, first digit 6-9.
const MOBILE_REGEX = /^[6-9]\d{9}$/;

// Enough room for "+91 98765-43210" while the user types in a readable format.
export const MOBILE_INPUT_MAX_LENGTH = 16;

/**
 * Reads a key from window.globalConfigs without throwing.
 * globalConfigs is loaded from the environment's assets and can be missing
 * (tests, a broken assets proxy), so the login page must never crash because of it.
 */
const readGlobalConfig = (key) => {
  try {
    return window?.globalConfigs?.getConfig?.(key);
  } catch (e) {
    return undefined;
  }
};

/**
 * OTP length shown in the OTP boxes.
 * Can be changed per environment with globalConfigs key EMPLOYEE_OTP_LENGTH
 * (no code change needed once the backend confirms 4 or 6).
 */
export const getOtpLength = () => {
  const configured = Number(readGlobalConfig("EMPLOYEE_OTP_LENGTH"));
  const isValid = Number.isInteger(configured) && configured >= MIN_OTP_LENGTH && configured <= MAX_OTP_LENGTH;
  return isValid ? configured : DEFAULT_OTP_LENGTH;
};

/**
 * Whether the "Login with mobile number & OTP" link is shown.
 * Visible by default; an environment hides it by setting globalConfigs key
 * EMPLOYEE_OTP_LOGIN_ENABLED to false (boolean) or "false" (string), which lets
 * this PR merge before the backend is live without showing a broken option.
 */
export const isOtpLoginEnabled = () => {
  const flag = readGlobalConfig("EMPLOYEE_OTP_LOGIN_ENABLED");
  return !(flag === false || String(flag).trim().toLowerCase() === "false");
};

/**
 * null/undefined -> "", anything else -> String(value).
 * Written out instead of `value ?? ""` on purpose: the example app's
 * react-scripts 4 (webpack 4) cannot parse `??`, and the core build leaves it
 * untouched, which breaks the app with "Module parse failed: Unexpected token".
 */
const toText = (value) => (value === null || value === undefined ? "" : String(value));

/**
 * Keeps only characters a person would type in a phone number
 * (digits, +, -, spaces, brackets) and caps the length.
 * Used on every keystroke, so letters never appear in the field.
 */
export const sanitizeMobileInput = (raw) =>
  toText(raw)
    .replace(/[^\d+\-\s()]/g, "")
    .slice(0, MOBILE_INPUT_MAX_LENGTH);

/**
 * Converts what the user typed into the 10-digit number sent to the backend.
 *   "+91 98765 43210" -> "9876543210"   (country code removed)
 *   "09876543210"     -> "9876543210"   (trunk 0 removed)
 *   "98765-43210"     -> "9876543210"
 * Anything else is returned as digits only and will fail isValidMobile.
 */
export const normalizeMobile = (raw) => {
  let digits = toText(raw).replace(/\D/g, "");

  if (digits.length === 12 && digits.startsWith("91")) {
    digits = digits.slice(2);
  } else if (digits.length === 11 && digits.startsWith("0")) {
    digits = digits.slice(1);
  }

  return digits;
};

/** True when the input normalizes to a valid 10-digit Indian mobile number. */
export const isValidMobile = (raw) => MOBILE_REGEX.test(normalizeMobile(raw));

/**
 * Hides all but the last 4 digits: "9876543210" -> "******3210".
 * The OTP screen confirms where the OTP went without showing the full
 * number to someone looking at the screen.
 */
export const maskMobile = (raw) => {
  const digits = normalizeMobile(raw);
  if (digits.length <= 4) return digits;
  return `${"*".repeat(digits.length - 4)}${digits.slice(-4)}`;
};

/** Digits only, never longer than the OTP length. */
export const sanitizeOtp = (raw, length) =>
  toText(raw)
    .replace(/\D/g, "")
    .slice(0, length);

/** True only when the OTP has exactly `length` digits. */
export const isCompleteOtp = (otp, length) => typeof otp === "string" && otp.length === length && /^\d+$/.test(otp);

/**
 * Returns a whole number of seconds between 0 and MAX_TIMER_SECONDS,
 * or `fallback` when the value is missing, negative or not a number.
 *   toTimerSeconds(30, 99)    -> 30
 *   toTimerSeconds("45", 99)  -> 45
 *   toTimerSeconds(null, 99)  -> 99
 *   toTimerSeconds(-5, 99)    -> 99
 *   toTimerSeconds(99999, 99) -> 3600
 */
export const toTimerSeconds = (value, fallback) => {
  if (value === null || value === undefined || value === "") return fallback;
  const seconds = Math.floor(Number(value));
  if (!Number.isFinite(seconds) || seconds < 0) return fallback;
  return Math.min(seconds, MAX_TIMER_SECONDS);
};

/** 105 -> "1:45", 9 -> "0:09". Used for the account-selection countdown. */
export const formatCountdown = (totalSeconds) => {
  const safe = Math.max(0, Math.floor(Number(totalSeconds) || 0));
  const minutes = Math.floor(safe / 60);
  const seconds = String(safe % 60).padStart(2, "0");
  return `${minutes}:${seconds}`;
};

const cleanString = (value) => (typeof value === "string" || typeof value === "number" ? String(value).trim() : "");

/**
 * Cleans the `accounts` array returned by /user/otp-login/v1/_verify into
 *   [{ key, userName, name, tenantId, roleCodes }]
 *
 *  - Entries without a userName are dropped: /user/oauth/token needs it.
 *  - Missing tenantId falls back to `fallbackTenantId` (the state tenant).
 *  - roleCodes is always an array of non-empty strings.
 *  - The same userName + tenant listed twice is shown once.
 *  - Backend order is kept, so the backend decides which account comes first.
 */
export const normalizeAccounts = (rawAccounts, fallbackTenantId) => {
  if (!Array.isArray(rawAccounts)) return [];

  const seen = new Set();
  const accounts = [];

  rawAccounts.forEach((raw) => {
    if (!raw || typeof raw !== "object") return;

    const userName = cleanString(raw.userName);
    if (!userName) return;

    const tenantId = cleanString(raw.tenantId) || fallbackTenantId;
    const key = `${tenantId}:${userName}`;
    if (seen.has(key)) return;
    seen.add(key);

    accounts.push({
      key,
      userName,
      name: cleanString(raw.name),
      tenantId,
      roleCodes: (Array.isArray(raw.roleCodes) ? raw.roleCodes : []).map(cleanString).filter(Boolean),
    });
  });

  return accounts;
};

/**
 * Role codes this portal refuses to log in (globalConfigs INVALIDROLES).
 * Digit.UserService.authenticate rejects such users only AFTER the token call,
 * so these accounts are hidden from the picker up front.
 */
export const getInvalidRoles = () => {
  const roles = readGlobalConfig("INVALIDROLES");
  return Array.isArray(roles) ? roles : [];
};

/**
 * Readable fallback for a role code that has no translation yet.
 * Short words (3 letters or fewer) are treated as acronyms.
 *   "AMC_REVIEWER"       -> "AMC Reviewer"
 *   "ORG_PLATFORM_ADMIN" -> "ORG Platform Admin"
 *   "EMPLOYEE"           -> "Employee"
 */
export const formatRoleCode = (code) =>
  String(code || "")
    .split("_")
    .filter(Boolean)
    .map((word) => (word.length <= 3 ? word.toUpperCase() : word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()))
    .join(" ");

/**
 * Short role summary for one account row: the first `max` labels, then "+N more".
 * Keeps every row to one line however many roles an account has.
 *   (["A", "B", "C", "D"], 2) -> "A, B +2 more"
 */
export const summarizeRoles = (labels, max) => {
  if (!Array.isArray(labels) || labels.length === 0) return "";
  const shown = labels.slice(0, max).join(", ");
  const hidden = labels.length - max;
  return hidden > 0 ? `${shown} +${hidden} more` : shown;
};

/** Removes accounts holding any role in `invalidRoles`. */
export const filterPermittedAccounts = (accounts, invalidRoles) => {
  if (!Array.isArray(invalidRoles) || invalidRoles.length === 0) return accounts;
  return accounts.filter((account) => !account.roleCodes.some((code) => invalidRoles.includes(code)));
};
