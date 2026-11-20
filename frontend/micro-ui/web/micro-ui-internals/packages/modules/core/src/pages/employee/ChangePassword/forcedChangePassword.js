import { FormComposer, Toast } from "@selco/digit-ui-react-components";
import React, { useEffect, useRef, useState } from "react";
import { Redirect } from "react-router-dom";
import Header from "../../../components/Header";
import { useLabel } from "../../../hooks/useLabel";
import { PasswordService } from "../../../services/PasswordService";
import { isPasswordChangeRequired } from "../../../utilities/passwordChangeRequired";
import { getPasswordRuleChecks, isValidPassword } from "../../../utilities/passwordPolicy";
import { config } from "./config";
import FORCED_CHANGE_LABELS from "./forcedChangeLabels";

/**
 * MH2-10 - forced password change on first login.
 *
 * Reached only through the route gate in pages/employee/index.js, which sends every employee
 * route here while the backend's flag is set (see utilities/passwordChangeRequired.js).
 *
 * Reuses the existing change password FORM (the new / confirm password inputs from ./config), but
 * not its submit path: the existing screen is the OTP forgot-password flow (`nologin/_update`),
 * while a logged-in user changes their password through `_update` with their current password.
 *
 * On success the user is logged out and sent to the login page to sign in with the new password.
 * That also asks the server for a fresh flag, so nothing stale is carried over.
 * There is deliberately no Back button: the only ways out are a successful change or Logout.
 */

const EXISTING_PASSWORD = "existingPassword";
const TOAST_DURATION_MS = 5000;
const LOGOUT_DELAY_MS = 2000; // long enough to read the success message, same as UserProfile

const EMPTY_VALUES = { [EXISTING_PASSWORD]: "", newPassword: "", confirmPassword: "" };

// The new / confirm password inputs of the existing change password form (index 0 is username,
// which the logged-in endpoint does not take - egov-user reads it from the session).
const [, NEW_PASSWORD_INPUT, CONFIRM_PASSWORD_INPUT] = config[0].inputs;

const getPageStyle = (bgImageUrl) => ({
  position: "fixed",
  top: 0,
  right: 0,
  bottom: 0,
  left: 0,
  zIndex: 100,
  overflowY: "auto",
  display: "flex",
  flexDirection: "column",
  padding: "12px 16px",
  boxSizing: "border-box",
  backgroundColor: "#225670",
  backgroundImage: bgImageUrl ? `linear-gradient(rgba(34, 86, 112, 0.78), rgba(34, 86, 112, 0.78)), url(${bgImageUrl})` : undefined,
  backgroundSize: "cover",
  backgroundPosition: "center",
});

const PAGE_CSS = `
.forcedPwdPage .label-field-pair { margin-bottom: 10px !important; }
.forcedPwdPage .card-label,
.forcedPwdPage .card-label-smaller { margin-bottom: 4px !important; line-height: 20px !important; }
.forcedPwdPage input { height: 36px !important; margin-bottom: 0 !important; }
@media (max-height: 640px) {
  .forcedPwdPage .forcedPwdInfo { display: none; }
}
`;

const ForcedChangePassword = () => {
  const label = useLabel(FORCED_CHANGE_LABELS);
  const [values, setValues] = useState(EMPTY_VALUES);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [hasSucceeded, setHasSucceeded] = useState(false);
  const [toast, setToast] = useState(null);
  const toastTimer = useRef(null);
  const logoutTimer = useRef(null);

  // Clear the toast timer if the screen goes away. The logout timer is deliberately NOT cleared:
  // once the password has changed, the old session must end even if this screen unmounts first,
  // otherwise the user could be left signed in with a flag that no longer matches their password.
  useEffect(() => () => clearTimeout(toastTimer.current), []);

  const user = Digit.UserService.getUser();

  // --- Who may be here --------------------------------------------------------------------------
  // Not signed in: nothing to change. Signed in but not flagged (or the flag was cleared): go home.
  // `hasSucceeded` keeps the success message on screen until the logout below kicks in.
  if (!user?.access_token || !user?.info) {
    return <Redirect to={`/${window.contextPath}/employee/user/login`} />;
  }
  if (!hasSucceeded && !isPasswordChangeRequired()) {
    return <Redirect to={`/${window.contextPath}/employee`} />;
  }

  // --- Helpers ----------------------------------------------------------------------------------
  const closeToast = () => setToast(null);

  const showError = (labelKey, text) => {
    clearTimeout(toastTimer.current);
    setToast({ type: "error", text: text || label(labelKey) });
    toastTimer.current = setTimeout(closeToast, TOAST_DURATION_MS);
  };

  const logout = () => {
    clearTimeout(logoutTimer.current);
    // Clears sessionStorage + localStorage (including the remembered flag) and leaves the app;
    // the employee app then lands on the login page.
    Digit.UserService.logout();
  };

  // FormComposer calls this on every render with a fresh object, so only update state when a value
  // actually changed - otherwise React would re-render forever.
  const onFormValueChange = (_setValue, formData) => {
    const next = {
      [EXISTING_PASSWORD]: formData?.[EXISTING_PASSWORD] || "",
      newPassword: formData?.[NEW_PASSWORD_INPUT.name] || "",
      confirmPassword: formData?.[CONFIRM_PASSWORD_INPUT.name] || "",
    };
    setValues((prev) =>
      prev[EXISTING_PASSWORD] === next[EXISTING_PASSWORD] &&
      prev.newPassword === next.newPassword &&
      prev.confirmPassword === next.confirmPassword
        ? prev
        : next
    );
  };

  // --- Submit -----------------------------------------------------------------------------------
  const onSubmit = async (formData) => {
    if (isSubmitting || hasSucceeded) return; // no double submits

    // Passwords are compared and sent exactly as typed - never trimmed. Spaces are not allowed in a
    // new password, so the rule check rejects them instead of silently changing what was typed.
    const existingPassword = formData?.[EXISTING_PASSWORD] || "";
    const newPassword = formData?.[NEW_PASSWORD_INPUT.name] || "";
    const confirmPassword = formData?.[CONFIRM_PASSWORD_INPUT.name] || "";

    if (!existingPassword || !newPassword || !confirmPassword) return showError("CORE_FORCE_PWD_ERR_REQUIRED");
    if (!isValidPassword(newPassword)) return showError("CORE_FORCE_PWD_ERR_INVALID");
    if (newPassword !== confirmPassword) return showError("CORE_COMMON_PROFILE_PASSWORD_MISMATCH");
    if (newPassword === existingPassword) return showError("CORE_FORCE_PWD_ERR_SAME");

    setIsSubmitting(true);
    try {
      await PasswordService.updatePassword({
        existingPassword,
        newPassword,
        tenantId: user.info.tenantId || Digit.ULBService.getCurrentTenantId(),
        type: user.info.type || "EMPLOYEE",
      });
      setHasSucceeded(true);
      clearTimeout(toastTimer.current);
      setToast({ type: "success", text: label("CORE_FORCE_PWD_SUCCESS") });
      logoutTimer.current = setTimeout(logout, LOGOUT_DELAY_MS);
    } catch (err) {
      // err comes from PasswordService and never contains the passwords - safe to inspect.
      showError(err?.i18nKey || "CORE_FORCE_PWD_ERR_GENERIC", err?.backendMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  // --- Form -------------------------------------------------------------------------------------
  const fieldStyle = { marginBottom: "0px" };

  const formConfig = [
    {
      body: [
        {
          label: label("CORE_FORCE_PWD_CURRENT_PASSWORD"),
          type: "password",
          populators: { name: EXISTING_PASSWORD, style: fieldStyle },
          isMandatory: true,
        },
        {
          label: label(NEW_PASSWORD_INPUT.label),
          type: NEW_PASSWORD_INPUT.type,
          populators: { name: NEW_PASSWORD_INPUT.name, style: fieldStyle },
          isMandatory: true,
        },
        {
          label: label(CONFIRM_PASSWORD_INPUT.label),
          type: CONFIRM_PASSWORD_INPUT.type,
          populators: { name: CONFIRM_PASSWORD_INPUT.name, style: fieldStyle },
          isMandatory: true,
        },
      ],
    },
  ];

  const ruleChecks = getPasswordRuleChecks(values.newPassword);
  const canSubmit = !!values[EXISTING_PASSWORD] && isValidPassword(values.newPassword) && !!values.confirmPassword;
  const bgImageUrl = window?.globalConfigs?.getConfig?.("BG_IMAGE");

  return (
    <div className="forcedPwdPage" style={getPageStyle(bgImageUrl)}>
      <style>{PAGE_CSS}</style>
      {/* margin:auto centres the card when it fits and lets the page scroll when it does not */}
      <div style={{ width: "100%", maxWidth: "440px", margin: "auto" }}>
        <FormComposer
          onSubmit={onSubmit}
          onFormValueChange={onFormValueChange}
          isDisabled={!canSubmit || isSubmitting || hasSucceeded}
          noBoxShadow
          inline
          submitInForm
          config={formConfig}
          label={label(config[0].texts.submitButtonLabel)}
          secondaryActionLabel={label("CORE_COMMON_LOGOUT")}
          onSecondayActionClick={() => !isSubmitting && logout()}
          cardStyle={{ width: "100%", maxWidth: "440px", margin: "0 auto", padding: "16px 24px", boxSizing: "border-box" }}
          buttonStyle={{ maxWidth: "100%", width: "100%", backgroundColor: "#7a2829" }}
          className="employeeChangePassword"
        >
          <Header />
          <h2 style={{ textAlign: "center", margin: "4px 0 6px", fontSize: "22px", lineHeight: "28px", fontWeight: 700 }}>
            {label("CORE_FORCE_PWD_HEADER")}
          </h2>
          <p className="forcedPwdInfo" style={{ textAlign: "center", margin: "0 0 10px", fontSize: "14px", lineHeight: "20px", color: "#505A5F" }}>
            {label("CORE_FORCE_PWD_INFO")}
          </p>

          {/* Live checklist of the MH2-01 rule, ticked as the new password is typed. */}
          <div style={{ background: "#F6F6F6", borderRadius: "4px", padding: "8px 12px", marginBottom: "12px" }}>
            <div style={{ margin: "0 0 2px", fontSize: "13px", lineHeight: "18px", fontWeight: 600, color: "#505A5F" }}>
              {label("CORE_FORCE_PWD_RULES_TITLE")}
            </div>
            <ul aria-live="polite" style={{ listStyle: "none", margin: 0, padding: 0 }}>
              {ruleChecks.map((rule) => (
                <li key={rule.key} style={{ color: rule.passed ? "#00703C" : "#505A5F", fontSize: "12.5px", lineHeight: "18px" }}>
                  <span aria-hidden="true">{rule.passed ? "✔ " : "• "}</span>
                  {label(rule.labelKey)}
                </li>
              ))}
            </ul>
          </div>
        </FormComposer>

        {/* Padding lives on the wrapper, not the img, so the image keeps its full height. */}
        <div style={{ display: "flex", justifyContent: "center", marginTop: "12px" }}>
          <div style={{ background: "#fff", borderRadius: "4px", padding: "6px 12px", display: "inline-flex" }}>
            <img
              alt="Powered by DIGIT"
              src={window?.globalConfigs?.getConfig?.("DIGIT_FOOTER_BW")}
              style={{ cursor: "pointer", height: "1.1rem", width: "auto", display: "block" }}
              onClick={() => {
                window.open(window?.globalConfigs?.getConfig?.("DIGIT_HOME_URL"), "_blank").focus();
              }}
            />
          </div>
        </div>
      </div>

      {toast && <Toast error={toast.type === "error"} isDleteBtn={true} label={toast.text} onClose={closeToast} />}
    </div>
  );
};

export default ForcedChangePassword;
