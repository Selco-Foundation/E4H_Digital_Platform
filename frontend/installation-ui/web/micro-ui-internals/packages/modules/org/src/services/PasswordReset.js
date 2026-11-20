import { Request } from "@egovernments/digit-ui-libraries";

/**
 * an admin sets a new password for a user of their organisation.
 *
 * Proposed contract :
 *   POST /vendor/organisation/v1/user/password/_reset
 *   { RequestInfo, PasswordReset: { organizationId, orgUserId, userId, tenantId, newPassword } }
 *   200 -> { ResponseInfo, PasswordReset: { userId, emailSent: true | false } }
 *          emailSent=false: the password WAS reset but the credentials email failed.
 *   4xx -> { Errors: [{ code, message }] }
 *
 * The password is never logged here, and the original axios error is not passed on: its `config`
 * holds the request body, i.e. the new password in plain text.
 */

// ---- CONTRACT ------------------------------------
const RESET_PASSWORD_URL = "/vendor/organisation/v1/user/password/_reset";
const REQUEST_WRAPPER = "PasswordReset";
const RESPONSE_WRAPPER = "PasswordReset";
const EMAIL_SENT_FIELD = "emailSent";
// egov-user validatePassword() codes 
const POLICY_ERROR_CODES = ["INVALID_PWD_LENGTH", "INVALID_PWD_PATTERN"];
// -------------------------------------------------------------------------------------------------

/** Error carrying a localisation key for the modal, plus the backend's own text if it sent one. */
const createResetError = (i18nKey, backendMessage) => {
  const error = new Error(backendMessage || i18nKey);
  error.i18nKey = i18nKey;
  error.backendMessage = backendMessage || null;
  return error;
};

/**
 * Turns any failure into a ResetError. The API answers in the tracer shape { Errors: [{ code, message }] }
 * (legacy { error: { code, message } } handled too, as in the rest of the platform).
 */
const toResetError = (err) => {
  const response = err?.response;
  const data = response?.data;
  if (!data) {
    // No response: the request never reached the server (offline, timeout, CORS...).
    return createResetError("ORG_RESET_PWD_ERR_GENERIC");
  }

  const tracerError = data?.Errors?.[0];
  const legacyError = data?.error?.fields?.[0] || data?.error;
  const code = tracerError?.code || legacyError?.code || "";
  const message = tracerError?.message || tracerError?.description || legacyError?.message || "";

  // Policy failure: show our own text, which describes the rule the form checks.
  if (POLICY_ERROR_CODES.includes(code)) return createResetError("ORG_RESET_PWD_ERR_INVALID");

  // Out of scope The UI already hides the action in these cases, so this only
  // happens if roles changed since the page loaded - show the backend's reason when it gave one.
  if (response?.status === 403) return createResetError("ORG_RESET_PWD_ERR_NOT_ALLOWED", message);

  return createResetError("ORG_RESET_PWD_ERR_GENERIC", message);
};

/**
 * true / false when the backend said whether the credentials email went out; null when it did not
 * say (field missing or unexpected) - the modal then shows a neutral success instead of guessing.
 */
const readEmailSent = (body) => {
  // No `??` here: the app's webpack 4 cannot parse it (microbundle leaves it in the modern build).
  const wrapped = body?.[RESPONSE_WRAPPER]?.[EMAIL_SENT_FIELD];
  const value = wrapped !== undefined && wrapped !== null ? wrapped : body?.[EMAIL_SENT_FIELD];
  if (value === true || value === "true") return true;
  if (value === false || value === "false") return false;
  return null;
};

export const PasswordResetService = {
  /**
   * Resets `targetUser`'s password (a row of the org user list) to `newPassword`.
   * Resolves with { emailSent: true | false | null }; rejects with an Error that has `i18nKey`
   * (and maybe `backendMessage`).
   */
  resetOrganizationUserPassword: async ({ organizationId, targetUser, newPassword, tenantId }) => {
    try {
      const body = await Request({
        url: RESET_PASSWORD_URL,
        method: "POST",
        auth: true,
        userService: true, // the API resolves the caller (and their org) from RequestInfo.userInfo
        params: { tenantId },
        data: {
          [REQUEST_WRAPPER]: {
            organizationId,
            orgUserId: targetUser?.orgUserId,
            userId: targetUser?.uuid,
            tenantId,
            newPassword,
          },
        },
      });
      return { emailSent: readEmailSent(body) };
    } catch (err) {
      throw toResetError(err);
    }
  },
};
