/**
 * MH2-09 §4 - every label the admin "Reset password" action uses, with its English fallback.
 *
 * The localisation service stays the source of truth: the modal shows the uploaded translation when
 * the key exists and falls back to this text only when it is missing, so nobody sees a raw key.
 * New keys belong to `rainmaker-org` and are recorded in the E4H localization tracking repo - this
 * list is what goes there.
 *
 * CORE_COMMON_CANCEL / CORE_COMMON_CLOSE already exist and are reused (listed only as fallbacks).
 */
const RESET_PASSWORD_LABELS = {
  // Reused keys
  CORE_COMMON_CANCEL: "Cancel",
  CORE_COMMON_CLOSE: "Close",

  // New keys (MH2-09) - rainmaker-org
  ORG_RESET_PWD_ACTION: "Reset password",
  ORG_RESET_PWD_HEADER: "Reset password",
  ORG_RESET_PWD_FOR_USER: "Set a new password for",
  ORG_RESET_PWD_NEW_PASSWORD: "New Password",
  ORG_RESET_PWD_CONFIRM_PASSWORD: "Confirm New Password",
  ORG_RESET_PWD_SUBMIT: "Reset password",
  ORG_RESET_PWD_RULES_TITLE: "The new password must have:",
  ORG_RESET_PWD_RULE_LENGTH: "7 to 15 characters",
  ORG_RESET_PWD_RULE_DIGIT: "At least one number",
  ORG_RESET_PWD_RULE_SPECIAL: "At least one special character, e.g. @ # $ % ! _ -",
  ORG_RESET_PWD_RULE_NO_SPACES: "No spaces",
  ORG_RESET_PWD_ERR_REQUIRED: "Please fill in both fields",
  ORG_RESET_PWD_ERR_INVALID: "The new password does not meet the password rules",
  ORG_RESET_PWD_ERR_MISMATCH: "New password and confirm password do not match",
  ORG_RESET_PWD_ERR_NOT_ALLOWED: "You are not allowed to reset this user's password",
  ORG_RESET_PWD_ERR_GENERIC: "Could not reset the password. Please try again",
  // The two email results the ticket requires (plus a neutral one if the backend does not say).
  // Deliberately none of them says the user was logged out: session invalidation is descoped
  // (MH2-06) and existing sessions keep working for up to 7 days.
  ORG_RESET_PWD_SUCCESS_EMAILED: "Password reset. The user has been emailed their new login details.",
  ORG_RESET_PWD_SUCCESS_EMAIL_FAILED:
    "Password reset, but the email with the new login details could not be sent. Please share the new password with the user yourself.",
  ORG_RESET_PWD_SUCCESS: "Password reset.",
};

/** The uploaded translation when it exists, otherwise the English fallback - never a raw key. */
export const getResetPasswordLabel = (t, key) => {
  const translated = typeof t === "function" ? t(key) : null;
  return translated && translated !== key ? translated : RESET_PASSWORD_LABELS[key] || key;
};

export default RESET_PASSWORD_LABELS;
