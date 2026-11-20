/**
 * English fallbacks for every label the forced password change screen (MH2-10) uses.
 *
 * The localisation service stays the source of truth: useLabel() shows the uploaded translation
 * when the key exists and falls back to these only when it is missing, so users never see raw keys.
 * New keys belong to the `rainmaker-common` module (shared by core and org) and are recorded in the
 * E4H localization tracking repo - never added to this project.
 *
 * The CORE_LOGIN_* / CORE_COMMON_* keys already exist and are reused; they are listed only so the
 * screen still reads correctly if a locale is missing them.
 */ 
const FORCED_CHANGE_LABELS = {
  // Reused keys (already in rainmaker-common)
  CORE_LOGIN_NEW_PASSWORD: "New Password",
  CORE_LOGIN_CONFIRM_NEW_PASSWORD: "Confirm New Password",
  CORE_COMMON_CHANGE_PASSWORD: "Change Password",
  CORE_COMMON_LOGOUT: "Logout",
  CORE_COMMON_PROFILE_PASSWORD_MISMATCH: "New password and confirm password do not match",

  // New keys (MH2-10)
  CORE_FORCE_PWD_HEADER: "Set a new password",
  CORE_FORCE_PWD_INFO: "You are signing in with a temporary password. Choose a new password to continue.",
  CORE_FORCE_PWD_CURRENT_PASSWORD: "Current Password",
  CORE_FORCE_PWD_RULES_TITLE: "Your new password must have:",
  CORE_FORCE_PWD_RULE_LENGTH: "7 to 15 characters",
  CORE_FORCE_PWD_RULE_DIGIT: "At least one number",
  CORE_FORCE_PWD_RULE_SPECIAL: "At least one special character, e.g. @ # $ % ! _ -",
  CORE_FORCE_PWD_RULE_NO_SPACES: "No spaces",
  CORE_FORCE_PWD_ERR_REQUIRED: "Please fill in all the fields",
  CORE_FORCE_PWD_ERR_INVALID: "The new password does not meet the password rules",
  CORE_FORCE_PWD_ERR_SAME: "The new password must be different from your current password",
  CORE_FORCE_PWD_ERR_CURRENT_WRONG: "Your current password is incorrect",
  CORE_FORCE_PWD_ERR_GENERIC: "Could not change your password. Please try again",
  CORE_FORCE_PWD_SUCCESS: "Password changed. Please log in with your new password.",
};

export default FORCED_CHANGE_LABELS;
