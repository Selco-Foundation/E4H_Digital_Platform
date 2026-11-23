import { FormComposerV2, Loader, Toast } from "@egovernments/digit-ui-components";
import React, { useCallback, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { Redirect } from "react-router-dom";
import Background from "../../../components/Background";
import Header from "../../../components/Header";
import ImageComponent from "../../../components/ImageComponent";
import { useLoginConfig } from "../../../hooks/useLoginConfig";
import { PasswordService } from "../../../services/PasswordService";
import { isPasswordChangeRequired } from "../../../utilities/passwordChangeRequired";
import { getPasswordRuleChecks, isValidPassword } from "../../../utilities/passwordPolicy";
import Carousel from "../Login/Carousel/Carousel";
import { config } from "./config";
import { getForcedChangeLabel } from "./forcedChangeLabels";

/**
 * MH2-10 - forced password change on first login.
 *
 * Looks exactly like the login page (Login/login.js): the same layout, chosen the same way - the
 * MDMS banner carousel with the form panel beside it when LoginConfig has `bannerImages`, otherwise
 * the plain banner background - and the same FormComposerV2, header and footer. Login/login.js is
 * not touched; its layout is mirrored here.
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
const LOGOUT_DELAY_MS = 2000; // long enough to read the success message, same as UserProfile

const EMPTY_VALUES = { [EXISTING_PASSWORD]: "", newPassword: "", confirmPassword: "" };

// The new / confirm password inputs of the existing change password form (index 0 is username,
// which the logged-in endpoint does not take - egov-user reads it from the session).
const [, NEW_PASSWORD_INPUT, CONFIRM_PASSWORD_INPUT] = config[0].inputs;

/**
 * Scoped to this page; the login page is not affected.
 *
 * This form is taller than the login form (three fields, the info line and the rule checklist), and
 * both login layouts are fixed 100vh boxes that hide overflow, so on a short window the top of the
 * form would be cut off. These rules only let the page scroll in that case:
 *  - carousel layout: the form panel scrolls; the column starts at the top and centres the form
 *    with auto margins (which, unlike `align-items: center`, never push content above the top);
 *  - banner layout: the banner grows with its content instead of staying 100vh.
 * The footer sits under the form rather than pinned to the bottom, so it can never cover a field.
 */
const PAGE_CSS = `
.forcedPwdLogin .login-form-container { overflow-y: auto; }
.forcedPwdLogin .forcedPwdColumn { align-self: flex-start; display: flex; flex-direction: column; width: 100%; min-height: 100%; }
.forcedPwdLogin .forcedPwdForm { width: 100%; max-width: 400px; margin: auto; }
.forcedPwdLogin .forcedPwdColumn .EmployeeLoginFooter { position: static; margin: 16px auto 0; }
.forcedPwdLogin .banner { height: auto !important; min-height: 100vh; padding: 24px 0; }
@media (max-height: 640px) {
  .forcedPwdLogin .forcedPwdInfo { display: none; }
}
`;

// The login CSS styles every `.digit-header-content` in the form panel as the big blue title. The
// heading keeps that look; the info line and the checklist set their own text style.
const DESCRIPTION_STYLES = { fontSize: "14px", lineHeight: "20px", fontWeight: 400, color: "#505A5F", margin: "4px 0 12px" };

const ForcedChangePassword = () => {
  const { t } = useTranslation();
  const label = (key) => getForcedChangeLabel(t, key);
  const [values, setValues] = useState(EMPTY_VALUES);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [hasSucceeded, setHasSucceeded] = useState(false);
  const [toast, setToast] = useState(null); // { id, type: "error" | "success", text }
  const toastId = useRef(0);
  const isSubmittingRef = useRef(false); // state updates are async; this blocks a fast double submit
  const logoutTimer = useRef(null);

  // The Toast closes itself after 5s and restarts that timer whenever `onClose` changes, so the
  // handler must keep the same identity across renders (typing re-renders this screen).
  const closeToast = useCallback(() => setToast(null), []);

  // Logo, title and banner images of the state, from the same MDMS LoginConfig the login page uses
  // (react-query usually has it cached from the login page).
  const stateCode = window?.globalConfigs?.getConfig?.("STATE_LEVEL_TENANT_ID") || "in";
  const { data: loginConfig, isLoading: isLoginConfigLoading } = useLoginConfig(stateCode);
  const loginHeader = loginConfig?.config?.[0]?.header;
  const bannerImages = loginConfig?.config?.[0]?.bannerImages;

  // The logout timer is deliberately NOT cleared on unmount: once the password has changed, the
  // old session must end even if this screen unmounts first, otherwise the user could be left
  // signed in with a flag that no longer matches their password.

  const user = Digit.UserService.getUser();

  // --- Who may be here --------------------------------------------------------------------------
  // Not signed in: nothing to change. Signed in but not flagged (or the flag was cleared): go home.
  // `hasSucceeded` keeps the success message on screen until the logout below kicks in.
  if (!user?.access_token || !user?.info) {
    return <Redirect to={`/${window?.contextPath}/employee/user/login`} />;
  }
  if (!hasSucceeded && !isPasswordChangeRequired()) {
    return <Redirect to={`/${window?.contextPath}/employee`} />;
  }
  // Same as the login page: wait for LoginConfig, so the layout does not switch once it arrives.
  if (isLoginConfigLoading) return <Loader />;

  // --- Helpers ----------------------------------------------------------------------------------
  // A new id per message remounts the Toast, so every message gets its own full 5 seconds.
  const showToast = (type, text) => {
    toastId.current += 1;
    setToast({ id: toastId.current, type, text });
  };

  const showError = (labelKey, text) => showToast("error", text || label(labelKey));

  const logout = () => {
    clearTimeout(logoutTimer.current);
    // Clears sessionStorage + localStorage (including the remembered flag) and leaves the app;
    // the employee app then lands on the login page.
    Digit.UserService.logout();
  };

  // FormComposerV2 calls this on every render with a fresh object, so only update state when a
  // value actually changed - otherwise React would re-render forever.
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
    if (isSubmittingRef.current || hasSucceeded) return; // no double submits

    // Passwords are compared and sent exactly as typed - never trimmed. Spaces are not allowed in a
    // new password, so the rule check rejects them instead of silently changing what was typed.
    const existingPassword = formData?.[EXISTING_PASSWORD] || "";
    const newPassword = formData?.[NEW_PASSWORD_INPUT.name] || "";
    const confirmPassword = formData?.[CONFIRM_PASSWORD_INPUT.name] || "";

    if (!existingPassword || !newPassword || !confirmPassword) return showError("CORE_FORCE_PWD_ERR_REQUIRED");
    if (!isValidPassword(newPassword)) return showError("CORE_FORCE_PWD_ERR_INVALID");
    if (newPassword !== confirmPassword) return showError("CORE_COMMON_PROFILE_PASSWORD_MISMATCH");
    if (newPassword === existingPassword) return showError("CORE_FORCE_PWD_ERR_SAME");

    isSubmittingRef.current = true;
    setIsSubmitting(true);
    try {
      await PasswordService.updatePassword({
        existingPassword,
        newPassword,
        tenantId: user.info.tenantId || Digit.ULBService.getCurrentTenantId(),
        type: user.info.type || "EMPLOYEE",
      });
      setHasSucceeded(true);
      showToast("success", label("CORE_FORCE_PWD_SUCCESS"));
      logoutTimer.current = setTimeout(logout, LOGOUT_DELAY_MS);
    } catch (err) {
      // err comes from PasswordService and never contains the passwords - safe to inspect.
      showError(err?.i18nKey || "CORE_FORCE_PWD_ERR_GENERIC", err?.backendMessage);
    } finally {
      isSubmittingRef.current = false;
      setIsSubmitting(false);
    }
  };

  // --- Form -------------------------------------------------------------------------------------
  // Same field shape as the login config (Login/config.js). An empty field is caught by the form
  // itself, like on the login page, and shown under the field.
  const requiredError = label(NEW_PASSWORD_INPUT.error);
  const passwordField = (name, labelText) => ({
    label: labelText,
    type: "password",
    key: name,
    isMandatory: true,
    populators: { name, validation: { required: true }, error: requiredError },
  });

  const formConfig = [
    {
      body: [
        passwordField(EXISTING_PASSWORD, label("CORE_FORCE_PWD_CURRENT_PASSWORD")),
        passwordField(NEW_PASSWORD_INPUT.name, label(NEW_PASSWORD_INPUT.label)),
        passwordField(CONFIRM_PASSWORD_INPUT.name, label(CONFIRM_PASSWORD_INPUT.label)),
      ],
    },
  ];

  const ruleChecks = getPasswordRuleChecks(values.newPassword);
  const canSubmit = !!values[EXISTING_PASSWORD] && isValidPassword(values.newPassword) && !!values.confirmPassword;

  // Live checklist of the MH2-01 rule, ticked as the new password is typed.
  const ruleChecklist = (
    <div style={{ textAlign: "left", fontWeight: 400, background: "#F6F6F6", borderRadius: "4px", padding: "8px 12px", margin: "0 0 12px" }}>
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
  );

  // Same props as the login page's renderLoginForm, with this screen's texts.
  const renderForm = (cardClassName) => (
    <FormComposerV2
      onSubmit={onSubmit}
      isDisabled={!canSubmit || isSubmitting || hasSucceeded}
      noBoxShadow
      inline
      submitInForm
      config={formConfig}
      label={label(config[0].texts.submitButtonLabel)}
      secondaryActionLabel={label("CORE_COMMON_LOGOUT")}
      onSecondayActionClick={() => !isSubmittingRef.current && logout()}
      onFormValueChange={onFormValueChange}
      heading={label("CORE_FORCE_PWD_HEADER")}
      description={label("CORE_FORCE_PWD_INFO")}
      descriptionStyles={DESCRIPTION_STYLES}
      cardDescriptionClassName="forcedPwdInfo"
      text={ruleChecklist}
      className=""
      cardSubHeaderClassName="loginCardSubHeaderClassName"
      cardClassName={cardClassName}
      buttonClassName="buttonClassName"
    >
      {loginHeader ? <Header loginHeader={loginHeader} /> : <Header showTenant={false} />}
    </FormComposerV2>
  );

  const renderFooter = (footerClassName) => (
    <div className={footerClassName} style={{ backgroundColor: "unset" }}>
      <ImageComponent
        alt="Powered by DIGIT"
        src={window?.globalConfigs?.getConfig?.("DIGIT_FOOTER_BW")}
        style={{ cursor: "pointer" }}
        onClick={() => {
          window.open(window?.globalConfigs?.getConfig?.("DIGIT_HOME_URL"), "_blank").focus();
        }}
      />
    </div>
  );

  const toastElement = toast && <Toast key={toast.id} type={toast.type} label={toast.text} onClose={closeToast} />;

  // The two layouts of the login page, chosen the same way.
  return bannerImages ? (
    <div className="login-container forcedPwdLogin">
      <style>{PAGE_CSS}</style>
      <Carousel bannerImages={bannerImages} />
      <div className="login-form-container">
        <div className="forcedPwdColumn">
          <div className="forcedPwdForm">{renderForm("")}</div>
          {renderFooter("EmployeeLoginFooter")}
        </div>
        {toastElement}
      </div>
    </div>
  ) : (
    <div className="forcedPwdLogin">
      <style>{PAGE_CSS}</style>
      <Background>
        {renderForm("loginCardClassName")}
        {toastElement}
        {renderFooter("employee-login-home-footer")}
      </Background>
    </div>
  );
};

export default ForcedChangePassword;
