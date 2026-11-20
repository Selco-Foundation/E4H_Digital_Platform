/**
 * The platform password rule (MH2-01 §1), mirrored from egov-user so the reset form can never
 * accept a password the API will reject (MH2-09 §4).
 *
 * Same rule as micro-ui core `utilities/passwordPolicy.js` (MH2-10). It is a copy because the two
 * apps are separate builds and cannot import from each other - if one changes, change both.
 *
 * Source of truth - egov-user application.properties:
 *   egov.user.pwd.pattern=((?=.*\d)(?=.*[^A-Za-z0-9\s])(?=\S+$).*$)
 *   egov.user.pwd.pattern.min.length=7
 *   egov.user.pwd.pattern.max.length=15
 *
 * Do NOT rewrite the rule from prose: "special character" means ANY non-alphanumeric character,
 * not a fixed list like @#$%.
 *  - The pattern is anchored (^...$) so it must hold for the WHOLE password. Java's `find()` only
 *    needs a match from some offset, which is the "spaces slip through" bug MH2-01 §2 fixes.
 *  - JavaScript's \s also covers Unicode spaces (e.g. a non-breaking space) where Java's does not,
 *    so this check is at most slightly stricter than the backend - never looser.
 */

export const PASSWORD_MIN_LENGTH = 7;
export const PASSWORD_MAX_LENGTH = 15;

// Copied character-for-character from egov.user.pwd.pattern (Java string escaping removed).
const BACKEND_PASSWORD_PATTERN = "((?=.*\\d)(?=.*[^A-Za-z0-9\\s])(?=\\S+$).*$)";
const PASSWORD_REGEX = new RegExp(`^${BACKEND_PASSWORD_PATTERN}$`);

// Individual parts of the same rule, used only to tell the admin WHICH part is not met yet.
const HAS_DIGIT = /\d/;
const HAS_SPECIAL = /[^A-Za-z0-9\s]/;
const HAS_WHITESPACE = /\s/;

/**
 * One entry per rule, in the order the modal lists them.
 * `labelKey` is a localisation key (see components/.../OrganizationUserTable/resetPasswordLabels.js).
 */
export const getPasswordRuleChecks = (password) => {
  const value = typeof password === "string" ? password : "";
  return [
    {
      key: "length",
      labelKey: "ORG_RESET_PWD_RULE_LENGTH",
      passed: value.length >= PASSWORD_MIN_LENGTH && value.length <= PASSWORD_MAX_LENGTH,
    },
    { key: "digit", labelKey: "ORG_RESET_PWD_RULE_DIGIT", passed: HAS_DIGIT.test(value) },
    { key: "special", labelKey: "ORG_RESET_PWD_RULE_SPECIAL", passed: HAS_SPECIAL.test(value) },
    // An empty value has no spaces, but it must not show as "met" before anything is typed.
    { key: "noSpaces", labelKey: "ORG_RESET_PWD_RULE_NO_SPACES", passed: value.length > 0 && !HAS_WHITESPACE.test(value) },
  ];
};

/** True only when the password satisfies the full backend rule. */
export const isValidPassword = (password) =>
  typeof password === "string" &&
  password.length >= PASSWORD_MIN_LENGTH &&
  password.length <= PASSWORD_MAX_LENGTH &&
  PASSWORD_REGEX.test(password);
