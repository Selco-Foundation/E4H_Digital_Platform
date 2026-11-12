import { Request } from "@egovernments/digit-ui-libraries";
import { filterPermittedAccounts, getInvalidRoles, normalizeAccounts, toTimerSeconds } from "../utilities/otpLogin";

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

// State-level tenant (STATE_LEVEL_TENANT_ID in each environment's globalConfigs);
// the frontend does not know the employee's own tenant until _verify answers.
const getStateTenantId = () => Digit.ULBService.getStateId();

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
/* Response parsing                                                    */
/* ------------------------------------------------------------------ */

const parseSendResponse = (data) => ({
  resendAfterSeconds: toTimerSeconds(data?.resendAfterSeconds),
  otpValiditySeconds: toTimerSeconds(data?.otpValiditySeconds),
});

/**
 * Checks the _verify response and returns
 *   { loginTicket, ticketValiditySeconds, accounts }
 * accounts are shaped for the UI and exclude roles this portal blocks.
 */
const parseVerifyResponse = (data) => {
  const loginTicket = typeof data?.loginTicket === "string" ? data.loginTicket.trim() : "";
  if (!loginTicket) {
    throw createLoginError("ERR_OTP_LOGIN_FAILED", "Login could not be completed. Please request a new OTP.");
  }

  const allAccounts = normalizeAccounts(data?.accounts);
  if (allAccounts.length === 0) {
    throw createLoginError("ERR_NO_EMPLOYEE_ACCOUNT", "No employee account is linked to this mobile number.");
  }

  const accounts = filterPermittedAccounts(allAccounts, getInvalidRoles());
  if (accounts.length === 0) {
    throw createLoginError("ES_ERROR_USER_NOT_PERMITTED", "You are not permitted to log in to this application.");
  }

  return {
    loginTicket,
    ticketValiditySeconds: toTimerSeconds(data?.loginTicketValiditySeconds),
    accounts,
  };
};

/* ------------------------------------------------------------------ */
/* API calls                                                           */
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
    params: { tenantId: getStateTenantId() },
    auth: false,
    userService: false,
    useCache: false,
    setTimeParam: false,
  });

const sendLoginOtp = async (mobileNumber) => {
  let data;
  try {
    data = await postOtpLogin(SEND_OTP_URL, { tenantId: getStateTenantId(), mobileNumber });
  } catch (err) {
    throw toLoginError(err, "ERR_OTP_SEND_FAILED", "Could not send the OTP. Please check the number and try again.");
  }
  return parseSendResponse(data);
};

const verifyLoginOtp = async (mobileNumber, otp) => {
  let data;
  try {
    data = await postOtpLogin(VERIFY_OTP_URL, { tenantId: getStateTenantId(), mobileNumber, otp });
  } catch (err) {
    throw toLoginError(err, "ERR_INVALID_OTP", "The OTP is incorrect or has expired.");
  }
  return parseVerifyResponse(data);
};

const loginWithTicket = async (account, loginTicket) => {
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
/* Public API                                                          */
/* ------------------------------------------------------------------ */

// Same shape as UserAccessReportService: one object holding the API calls.
export const OtpLoginService = {
  /** Step 1. Sends an OTP. Resolves with { resendAfterSeconds, otpValiditySeconds }. */
  sendLoginOtp,

  /** Step 2. Verifies the OTP. Resolves with { loginTicket, ticketValiditySeconds, accounts }. */
  verifyLoginOtp,

  /** Step 3. Logs in as the chosen account. Resolves with the OAuth response used by login.js. */
  loginWithTicket,
};
