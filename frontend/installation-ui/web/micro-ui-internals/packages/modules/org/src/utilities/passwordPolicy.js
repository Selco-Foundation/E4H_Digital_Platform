/**
 * The platform password rule (MH2-01 §1), mirrored from egov-user so the org user form can never
 * accept a password the API will reject (MH2-09 §4 / §5).
 *
 * Same rule as core `utilities/passwordPolicy.js` (MH2-10) in both apps. It is a copy because the
 * modules are separate builds - if one changes, change all of them.
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

/** True only when the password satisfies the full backend rule. */
export const isValidPassword = (password) =>
  typeof password === "string" &&
  password.length >= PASSWORD_MIN_LENGTH &&
  password.length <= PASSWORD_MAX_LENGTH &&
  PASSWORD_REGEX.test(password);
