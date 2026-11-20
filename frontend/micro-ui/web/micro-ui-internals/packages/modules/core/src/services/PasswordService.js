import { Request } from "@egovernments/digit-ui-libraries";

/**
 * MH2-10 - the logged-in "change my password" call.
 *
 * Why not Digit.UserService.changePassword: that picks its endpoint by guessing from the session
 * (`_update` when a user is stored, `nologin/_update` otherwise). `nologin/_update` is the OTP
 * forgot-password flow and fails for a user who has just logged in without an OTP. This service
 * always calls the logged-in endpoint, so the forced change can never end up on the wrong one.
 *
 * Backend contract (egov-user PasswordController `/password/_update`):
 *   POST /user/password/_update?tenantId=<tenant>
 *   { RequestInfo: { authToken, userInfo }, existingPassword, newPassword, tenantId, type }
 *   - The username is NOT in the body: egov-user reads it from RequestInfo.userInfo.userName,
 *     which is why `userService: true` is required below.
 *
 * Passwords are never logged here, and the original axios error is not passed on: its `config`
 * holds the request body, i.e. both passwords in plain text.
 */

// Same value as Urls.ChangePassword1 in packages/libraries/src/services/atoms/urls.js.
const UPDATE_PASSWORD_URL = "/user/password/_update";

// egov-user validatePassword() error codes (UserService.validatePassword, MH2-01 §2).
const POLICY_ERROR_CODES = ["INVALID_PWD_LENGTH", "INVALID_PWD_PATTERN"];

/** Error carrying a localisation key for the screen, plus the backend's own text if it sent one. */
const createPasswordError = (i18nKey, backendMessage) => {
  const error = new Error(backendMessage || i18nKey);
  error.i18nKey = i18nKey;
  error.backendMessage = backendMessage || null;
  return error;
};

/**
 * Turns any failure into a PasswordError.
 * egov-user answers in one of two shapes:
 *   - tracer:  { Errors: [{ code, message, description }] }
 *   - legacy:  { error: { code, message, fields: [{ code, message }] } }
 */
const toPasswordError = (err) => {
  const data = err?.response?.data;
  if (!data) {
    // No response: the request never reached the server (offline, timeout, CORS...).
    return createPasswordError("CORE_FORCE_PWD_ERR_GENERIC");
  }

  const tracerError = data?.Errors?.[0];
  const legacyError = data?.error?.fields?.[0] || data?.error;
  const code = tracerError?.code || legacyError?.code || "";
  const message = tracerError?.message || tracerError?.description || legacyError?.message || "";

  // Policy failure. We show our own text: the backend's message is hardcoded and, until MH2-01 §2
  // lands, still describes the OLD rule (upper case, lower case, only @#$%).
  if (POLICY_ERROR_CODES.includes(code)) return createPasswordError("CORE_FORCE_PWD_ERR_INVALID");

  // Wrong current password: egov-user throws PasswordMismatchException("Invalid username or password").
  if (code.includes("PasswordMismatch") || /invalid username or password/i.test(message)) {
    return createPasswordError("CORE_FORCE_PWD_ERR_CURRENT_WRONG");
  }

  // Anything else (e.g. a "must differ from the emailed password" rule MH2-01 may add):
  // show the backend's text when it sent one, otherwise the generic message.
  return createPasswordError("CORE_FORCE_PWD_ERR_GENERIC", message);
};

export const PasswordService = {
  /**
   * Changes the signed-in user's password.
   * Resolves on success; rejects with an Error that has `i18nKey` (and maybe `backendMessage`).
   */
  updatePassword: async ({ existingPassword, newPassword, tenantId, type = "EMPLOYEE" }) => {
    try {
      return await Request({
        url: UPDATE_PASSWORD_URL,
        method: "POST",
        auth: true,
        userService: true,
        params: { tenantId },
        data: { existingPassword, newPassword, tenantId, type },
      });
    } catch (err) {
      throw toPasswordError(err);
    }
  },
};
