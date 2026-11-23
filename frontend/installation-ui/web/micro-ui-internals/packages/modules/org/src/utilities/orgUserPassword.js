import { isValidPassword } from "./passwordPolicy";
import { getOrgUserPasswordLabel } from "../components/OrganizationDetails/OrganizationUserTable/orgUserPasswordLabels";

/**
 * MH2-09 §4 / §5 - the optional Password + Confirm Password on the org user form.
 *
 * There is no reset endpoint (MH2-06): the password is `user.password` on the SAME
 * `/organisation/v1/user/_create` and `/_update` calls the form already makes. "Reset password" on
 * a row only opens the edit form with these fields focused - one form, one submit path, one set of
 * validation.
 *
 *   create, blank -> the backend makes one from the user's name + mobile number and emails it
 *   edit,   blank -> the password is left as it is (blank never clears a password)
 *   either typed  -> both must be filled, match, and pass the MH2-01 rule
 *
 * The password is never pre-filled from a fetched user and never echoed back after a save.
 */

export const PASSWORD_FIELD = "password";
export const CONFIRM_PASSWORD_FIELD = "confirmPassword";

// ---- CONTRACT (MH2-06) - update if the backend names it differently -----------------------------
// Where the _create / _update response says whether the credentials email went out.
// true = sent, false = the save worked but the mail failed. Missing = not reported.
const EMAIL_SENT_FIELD = "emailSent";
// -------------------------------------------------------------------------------------------------

/**
 * The error to show for the pair, or null when it is fine. Both empty is valid and must not block
 * submit. Values are checked exactly as typed - never trimmed - so a space is rejected by the rule
 * instead of silently changing the password the admin hands over.
 */
export const getPasswordPairError = (password, confirmPassword) => {
  const pwd = typeof password === "string" ? password : "";
  const confirm = typeof confirmPassword === "string" ? confirmPassword : "";
  if (!pwd && !confirm) return null;
  if (!pwd) return { field: PASSWORD_FIELD, key: "ORG_USER_PWD_ERR_PASSWORD_REQUIRED" };
  if (!isValidPassword(pwd)) return { field: PASSWORD_FIELD, key: "ORG_USER_PWD_ERR_INVALID" };
  if (!confirm) return { field: CONFIRM_PASSWORD_FIELD, key: "ORG_USER_PWD_ERR_CONFIRM_REQUIRED" };
  if (pwd !== confirm) return { field: CONFIRM_PASSWORD_FIELD, key: "ORG_USER_PWD_ERR_MISMATCH" };
  return null;
};

/**
 * FormComposerV2 field configs for the pair (appended to the user form's body).
 *
 * `getValues` returns the form's current values: react-hook-form 6 gives `validate` only the
 * field's own value, and each field has to look at the other one. Each validator reports only
 * errors that belong to its own field, so a message never shows under the wrong box.
 */
export const getPasswordFieldsConfig = ({ t, isEdit, getValues }) => {
  const label = (key) => getOrgUserPasswordLabel(t, key);
  const validateField = (field) => (value) => {
    const values = (typeof getValues === "function" && getValues()) || {};
    const error = getPasswordPairError(
      field === PASSWORD_FIELD ? value : values[PASSWORD_FIELD],
      field === CONFIRM_PASSWORD_FIELD ? value : values[CONFIRM_PASSWORD_FIELD]
    );
    return error && error.field === field ? label(error.key) : true;
  };

  return [
    {
      inline: true,
      // Already-resolved text (not a key): FormComposer runs t() on it, which returns it unchanged,
      // so the English fallback shows until the rainmaker-org keys are uploaded.
      label: label("ORG_USER_PASSWORD"),
      isMandatory: false,
      key: PASSWORD_FIELD,
      type: "password",
      description: `${label(isEdit ? "ORG_USER_PWD_HINT_EDIT" : "ORG_USER_PWD_HINT_CREATE")} ${label("ORG_USER_PWD_RULES")}`,
      populators: {
        name: PASSWORD_FIELD,
        validation: { customValidation: validateField(PASSWORD_FIELD) },
      },
    },
    {
      inline: true,
      label: label("ORG_USER_CONFIRM_PASSWORD"),
      isMandatory: false,
      key: CONFIRM_PASSWORD_FIELD,
      type: "password",
      populators: {
        name: CONFIRM_PASSWORD_FIELD,
        validation: { customValidation: validateField(CONFIRM_PASSWORD_FIELD) },
      },
    },
  ];
};

/** `{ password }` to spread into the request's `user` when one was typed, else nothing. */
export const getPasswordPayload = (formData) =>
  typeof formData?.[PASSWORD_FIELD] === "string" && formData[PASSWORD_FIELD] ? { password: formData[PASSWORD_FIELD] } : {};

/** true / false when the backend said whether the credentials email went out; null when it did not. */
const readEmailSent = (response) => {
  // No `??`: the app's webpack 4 cannot parse it (microbundle leaves it in the modern build).
  const candidates = [response?.[EMAIL_SENT_FIELD], response?.OrganizationUser?.[EMAIL_SENT_FIELD], response?.organizationUser?.[EMAIL_SENT_FIELD]];
  const value = candidates.find((candidate) => candidate !== undefined && candidate !== null);
  if (value === true || value === "true") return true;
  if (value === false || value === "false") return false;
  return null;
};

// A failed email leaves the admin something to do, so it stays up long enough to read.
const EMAIL_FAILED_TOAST_MS = 10000;

/**
 * The toast to show after a successful save, or null to keep the screen's existing success toast.
 *   create        -> emailed / email failed, only when the backend reports it
 *   edit + pwd    -> emailed / email failed, or a neutral "password reset" when not reported
 *   edit, no pwd  -> null (nothing about passwords happened)
 */
export const getPasswordResultToast = ({ t, isCreate, passwordSent, response }) => {
  if (!isCreate && !passwordSent) return null;
  const label = (key) => getOrgUserPasswordLabel(t, key);
  const emailSent = readEmailSent(response);
  if (emailSent === false) {
    return {
      key: "warning",
      label: label(isCreate ? "ORG_USER_CREATED_EMAIL_FAILED" : "ORG_USER_PWD_RESET_EMAIL_FAILED"),
      duration: EMAIL_FAILED_TOAST_MS,
    };
  }
  if (isCreate) return emailSent === true ? { key: "success", label: label("ORG_USER_CREATED_EMAILED") } : null;
  return { key: "success", label: label(emailSent === true ? "ORG_USER_PWD_RESET_EMAILED" : "ORG_USER_PWD_RESET_SUCCESS") };
};
