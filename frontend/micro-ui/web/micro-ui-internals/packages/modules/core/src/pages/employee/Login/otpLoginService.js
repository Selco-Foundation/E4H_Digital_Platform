import { Request } from "@egovernments/digit-ui-libraries";
import {
  LOGIN_TICKET_VALIDITY_SECONDS,
  OTP_VALIDITY_SECONDS,
  RESEND_SECONDS,
  filterPermittedAccounts,
  getInvalidRoles,
  normalizeAccounts,
  toTimerSeconds,
} from "./otpLoginUtils";

/**
 * The ONLY file that knows the backend contract for employee OTP login.
 *
 * Flow agreed with the backend team:
 *   1. POST /user/otp-login/v1/_send    { tenantId, mobileNumber }
 *        -> { resendAfterSeconds, otpValiditySeconds }
 *   2. POST /user/otp-login/v1/_verify  { tenantId, mobileNumber, otp }
 *        -> { loginTicket, loginTicketValiditySeconds, accounts: [{ userName, name, tenantId, roleCodes }] }
 *   3. POST /user/oauth/token (form)    username=<account.userName>, password=<loginTicket>,
 *        tenantId=<account.tenantId>, userType=EMPLOYEE, loginMethod=OTP
 *        -> same response as password login ({ access_token, ..., UserRequest })
 *
 * Every exported function throws a LoginError ({ i18nKey, fallback }) on failure,
 * so the UI always has a translatable key plus readable English text.
 *
 * The login ticket works like a short-lived password. It is passed straight
 * through and never stored or logged here.
 */

// Set to true ONLY to demo the UI while the APIs are not on your environment.
// It must be false in any commit you push.
export const USE_STUB = false;

// State-level tenant; the frontend does not know the employee's tenant until _verify answers.
const STATE_TENANT_ID = "in";

const SEND_OTP_URL = "/user/otp-login/v1/_send";
const VERIFY_OTP_URL = "/user/otp-login/v1/_verify";

/* ------------------------------------------------------------------ */
/* Errors                                                              */
/* ------------------------------------------------------------------ */

/** Error carrying a localization key and an English fallback for the UI. */
const createLoginError = (i18nKey, fallback, extra = {}) => {
  const error = new Error(fallback);
  error.i18nKey = i18nKey;
  error.fallback = fallback;
  // isNetworkError: the request never reached the server, so it is safe to retry
  // without asking for a new OTP (the login ticket was not used).
  error.isNetworkError = Boolean(extra.isNetworkError);
  return error;
};

/**
 * Turns any failure (axios error, OAuth error, thrown Error) into a LoginError.
 *
 * Order matters:
 *  - DIGIT services answer { Errors: [{ code, message }] }.
 *  - /user/oauth/token answers { error, error_description }.
 *  - UserService.authenticate throws Error("ES_ERROR_USER_NOT_PERMITTED") for blocked roles;
 *    it has no `response` but is NOT a network problem.
 *  - Anything else without a response means the request never reached the server.
 *
 * The original axios error is deliberately NOT attached: its config holds the
 * request body, which for the token call contains the login ticket.
 */
const toLoginError = (err, defaultKey, defaultFallback) => {
  if (err?.i18nKey) return err; // already converted

  const response = err?.response;

  if (!response) {
    if (typeof err?.message === "string" && err.message.startsWith("ES_")) {
      return createLoginError(err.message, "You are not permitted to log in to this application.");
    }
    return createLoginError("ERR_NETWORK_UNAVAILABLE", "Unable to reach the server. Check your connection and try again.", {
      isNetworkError: true,
    });
  }

  if (response.status >= 500) {
    return createLoginError("ERR_SERVER_UNAVAILABLE", "The server is not responding right now. Please try again in a few minutes.");
  }

  const digitError = response.data?.Errors?.[0];
  const backendMessage = digitError?.message || response.data?.error_description;

  // Prefer the backend's code as the key (it may already have a translation);
  // its message, or our default, is the readable fallback.
  return createLoginError(digitError?.code || defaultKey, backendMessage || defaultFallback);
};

/* ------------------------------------------------------------------ */
/* Response parsing (shared by real calls and the stub)                */
/* ------------------------------------------------------------------ */

const parseSendResponse = (data) => ({
  resendAfterSeconds: toTimerSeconds(data?.resendAfterSeconds, RESEND_SECONDS),
  otpValiditySeconds: toTimerSeconds(data?.otpValiditySeconds, OTP_VALIDITY_SECONDS),
});

/**
 * Checks the _verify response and returns
 *   { loginTicket, ticketValiditySeconds, accounts }
 * accounts are cleaned and exclude roles this portal blocks.
 */
const parseVerifyResponse = (data) => {
  const loginTicket = typeof data?.loginTicket === "string" ? data.loginTicket.trim() : "";
  if (!loginTicket) {
    throw createLoginError("ERR_OTP_LOGIN_FAILED", "Login could not be completed. Please request a new OTP.");
  }

  const allAccounts = normalizeAccounts(data?.accounts, STATE_TENANT_ID);
  if (allAccounts.length === 0) {
    throw createLoginError("ERR_NO_EMPLOYEE_ACCOUNT", "No employee account is linked to this mobile number.");
  }

  const accounts = filterPermittedAccounts(allAccounts, getInvalidRoles());
  if (accounts.length === 0) {
    throw createLoginError("ES_ERROR_USER_NOT_PERMITTED", "You are not permitted to log in to this application.");
  }

  return {
    loginTicket,
    ticketValiditySeconds: toTimerSeconds(data?.loginTicketValiditySeconds, LOGIN_TICKET_VALIDITY_SECONDS),
    accounts,
  };
};

/* ------------------------------------------------------------------ */
/* Real API calls                                                      */
/* ------------------------------------------------------------------ */

/*
 * Request (the same helper UserAccessReportService uses) adds
 * RequestInfo { apiId: "Rainmaker", ... } to the body automatically.
 * auth/userService are false because the user is not logged in yet.
 * useCache is false so a resend is a real new request, never a cached answer.
 */
const postOtpLogin = (url, body) =>
  Request({
    url,
    method: "POST",
    data: body,
    params: { tenantId: STATE_TENANT_ID },
    auth: false,
    userService: false,
    useCache: false,
    setTimeParam: false,
  });

const realSendLoginOtp = async (mobileNumber) => {
  let data;
  try {
    data = await postOtpLogin(SEND_OTP_URL, { tenantId: STATE_TENANT_ID, mobileNumber });
  } catch (err) {
    throw toLoginError(err, "ERR_OTP_SEND_FAILED", "Could not send the OTP. Please check the number and try again.");
  }
  return parseSendResponse(data);
};

const realVerifyLoginOtp = async (mobileNumber, otp) => {
  let data;
  try {
    // TODO(OTP-LOGIN-BE-CONFIRM): the backend sample used tenantId "rj" here and "in" for _send.
    // We send the state tenant for both; change only if the backend confirms otherwise.
    data = await postOtpLogin(VERIFY_OTP_URL, { tenantId: STATE_TENANT_ID, mobileNumber, otp });
  } catch (err) {
    throw toLoginError(err, "ERR_INVALID_OTP", "The OTP is incorrect or has expired.");
  }
  return parseVerifyResponse(data);
};

const realLoginWithTicket = async (account, loginTicket) => {
  try {
    // authenticate() adds grant_type=password, scope=read and the Basic auth header,
    // and posts every key below as a form field. Matches the backend's curl exactly.
    return await Digit.UserService.authenticate({
      username: account.userName,
      password: loginTicket,
      tenantId: account.tenantId,
      userType: "EMPLOYEE",
      loginMethod: "OTP",
    });
  } catch (err) {
    throw toLoginError(err, "ERR_OTP_LOGIN_FAILED", "Login could not be completed. Please request a new OTP.");
  }
};

/* ------------------------------------------------------------------ */
/* Stub (only when USE_STUB = true)                                    */
/* ------------------------------------------------------------------ */
/*
 * Test inputs:
 *   Send OTP   number ending 0000 -> "not registered" error
 *              number ending 9999 -> server error
 *              any other valid    -> OTP "sent"
 *   Verify     111111 -> success (number ending 2222 returns 3 accounts, others 1)
 *              000000 -> "expired" error
 *              other  -> "incorrect OTP" error
 *   Login      always shows a "test mode" notice; no real session is created.
 */
const STUB_DELAY_MS = 800;
const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const stubSendLoginOtp = async (mobileNumber) => {
  await wait(STUB_DELAY_MS);
  if (mobileNumber.endsWith("0000")) {
    throw createLoginError("ERR_MOBILE_NOT_REGISTERED", "This mobile number is not registered with any employee account.");
  }
  if (mobileNumber.endsWith("9999")) {
    throw toLoginError({ response: { status: 503 } });
  }
  return parseSendResponse({ resendAfterSeconds: 30, otpValiditySeconds: 300 });
};

const stubVerifyLoginOtp = async (mobileNumber, otp) => {
  await wait(STUB_DELAY_MS);
  if (/^0+$/.test(otp)) {
    throw createLoginError("ERR_OTP_EXPIRED", "This OTP has expired. Please request a new one.");
  }
  if (!/^1+$/.test(otp)) {
    throw createLoginError("ERR_INVALID_OTP", "The OTP is incorrect or has expired.");
  }
  const accounts = [{ userName: "FAC_JAIPUR_PHC_01", name: "Ramesh Kumar", tenantId: "rj", roleCodes: ["FACILITY_USER"] }];
  if (mobileNumber.endsWith("2222")) {
    accounts.push(
      { userName: "FAC_JAIPUR_CHC_07", name: "Ramesh Kumar", tenantId: "rj", roleCodes: ["FACILITY_USER"] },
      { userName: "CRM_JAIPUR_02", name: "Ramesh Kumar", tenantId: "rj", roleCodes: ["CRM_AGENT", "EMPLOYEE"] }
    );
  }
  return parseVerifyResponse({ loginTicket: "stub-ticket", loginTicketValiditySeconds: 120, accounts });
};

const stubLoginWithTicket = async () => {
  await wait(STUB_DELAY_MS);
  return { isStub: true };
};

/* ------------------------------------------------------------------ */
/* Public API                                                          */
/* ------------------------------------------------------------------ */

/** Step 1. Sends an OTP. Resolves with { resendAfterSeconds, otpValiditySeconds }. */
export const sendLoginOtp = (mobileNumber) => (USE_STUB ? stubSendLoginOtp(mobileNumber) : realSendLoginOtp(mobileNumber));

/** Step 2. Verifies the OTP. Resolves with { loginTicket, ticketValiditySeconds, accounts }. */
export const verifyLoginOtp = (mobileNumber, otp) =>
  USE_STUB ? stubVerifyLoginOtp(mobileNumber, otp) : realVerifyLoginOtp(mobileNumber, otp);

/** Step 3. Logs in as the chosen account. Resolves with the OAuth response used by login.js. */
export const loginWithTicket = (account, loginTicket) =>
  USE_STUB ? stubLoginWithTicket() : realLoginWithTicket(account, loginTicket);
