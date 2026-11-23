/**
 * MH2-09 §4 / §5 - every label the org user form's password fields and the "Reset password" row
 * action use, with its English fallback.
 *
 * The localisation service stays the source of truth: the uploaded translation is shown when the
 * key exists, and this text only when it is missing, so nobody sees a raw key. All keys are new,
 * belong to `rainmaker-org` and are recorded in the E4H localization tracking repo - this list is
 * what goes there.
 *
 * Deliberately no message says the user was logged out: session invalidation is descoped (MH2-06)
 * and existing sessions keep working for up to 7 days after a password change.
 */
const ORG_USER_PASSWORD_LABELS = {
  // Row action (opens the edit form with the password fields focused)
  ORG_RESET_PWD_ACTION: "Reset password",

  // Form fields
  ORG_USER_PASSWORD: "Password",
  ORG_USER_CONFIRM_PASSWORD: "Confirm Password",
  ORG_USER_PWD_HINT_CREATE:
    "Optional. Leave blank and a password will be made from the user's name and mobile number and emailed to them.",
  ORG_USER_PWD_HINT_EDIT: "Leave blank to keep the current password.",
  ORG_USER_PWD_RULES: "7 to 15 characters, with at least one number and one special character (e.g. @ # $ % ! _ -) and no spaces.",

  // Validation
  ORG_USER_PWD_ERR_INVALID: "The password does not meet the password rules",
  ORG_USER_PWD_ERR_PASSWORD_REQUIRED: "Enter the password, or clear Confirm Password",
  ORG_USER_PWD_ERR_CONFIRM_REQUIRED: "Re-enter the password to confirm it",
  ORG_USER_PWD_ERR_MISMATCH: "Password and confirm password do not match",

  // Results - the backend says whether the credentials email went out (see orgUserPassword.js)
  ORG_USER_CREATED_EMAILED: "User created. Their login details have been emailed to them.",
  ORG_USER_CREATED_EMAIL_FAILED:
    "User created, but the email with their login details could not be sent. Share the login details with the user yourself, or use Reset password to set a new one.",
  ORG_USER_PWD_RESET_EMAILED: "User updated. They have been emailed their new login details.",
  ORG_USER_PWD_RESET_EMAIL_FAILED:
    "User updated and password reset, but the email with the new login details could not be sent. Share the new password with the user yourself.",
  // Used only when the backend does not say either way - a neutral success instead of a guess.
  ORG_USER_PWD_RESET_SUCCESS: "User updated and password reset.",
};

/** The uploaded translation when it exists, otherwise the English fallback - never a raw key. */
export const getOrgUserPasswordLabel = (t, key) => {
  const translated = typeof t === "function" ? t(key) : null;
  return translated && translated !== key ? translated : ORG_USER_PASSWORD_LABELS[key] || key;
};

export default ORG_USER_PASSWORD_LABELS;
