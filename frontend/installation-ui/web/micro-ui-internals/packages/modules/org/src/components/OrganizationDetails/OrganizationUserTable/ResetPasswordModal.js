import React, { useEffect, useRef, useState } from "react";
import { Button, PopUp } from "@egovernments/digit-ui-react-components";
import CustomCloseSvg from "../../Custom/CustomCloseSvg";
import { PasswordResetService } from "../../../services/PasswordReset";
import { getPasswordRuleChecks, isValidPassword } from "../../../utilities/passwordPolicy";
import { getResetPasswordLabel } from "./resetPasswordLabels";

/**
 * an admin sets a new password for one user of the org user list.
 *
 * - Asks only for New + Confirm password: the user's current password is never needed.
 * - Checks the rule as the admin types (utilities/passwordPolicy.js); the API checks it again.
 * - After the reset, says whether the credentials email went out - and, if the backend reports it
 *   failed, says THAT instead, so the admin knows to pass the password on themselves.
 * - Never says the user was logged out: session invalidation is descoped.
 *
 * Whether the action is offered at all is decided by the caller (utilities/orgAdminRoles.js).
 * Laid out like ConfirmationAlert so it looks like the rest of this screen.
 */

const NEW_PASSWORD = "newPassword";
const CONFIRM_PASSWORD = "confirmPassword";

const inputStyle = {
  width: "100%",
  height: "40px",
  padding: "0 10px",
  border: "1px solid #505A5F",
  borderRadius: "0",
  fontSize: "16px",
  boxSizing: "border-box",
};
const labelStyle = { display: "block", fontSize: "16px", fontWeight: 600, color: "#0B0C0C", marginBottom: "6px" };

const ResetPasswordModal = ({ t, user, organizationId, onClose }) => {
  const [values, setValues] = useState({ [NEW_PASSWORD]: "", [CONFIRM_PASSWORD]: "" });
  const [error, setError] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [result, setResult] = useState(null); // { emailSent: true | false | null } once reset
  const isSubmittingRef = useRef(false); // state updates are async; this blocks a fast double click
  const isMountedRef = useRef(true);

  useEffect(() => () => {
    isMountedRef.current = false;
  }, []);

  const label = (key) => getResetPasswordLabel(t, key);

  // Closing is blocked while the request is in flight, so the admin always sees how it ended.
  const close = () => {
    if (!isSubmittingRef.current) onClose();
  };

  const onChange = (event) => {
    const { name, value } = event.target;
    setValues((prev) => ({ ...prev, [name]: value }));
    setError(null);
  };

  const onSubmit = async (event) => {
    event?.preventDefault?.();
    if (isSubmittingRef.current || result) return;

    // Sent exactly as typed - never trimmed. Spaces are not allowed, so the rule check rejects them
    // instead of silently changing the password the admin will hand over.
    const newPassword = values[NEW_PASSWORD];
    const confirmPassword = values[CONFIRM_PASSWORD];
    if (!newPassword || !confirmPassword) return setError(label("ORG_RESET_PWD_ERR_REQUIRED"));
    if (!isValidPassword(newPassword)) return setError(label("ORG_RESET_PWD_ERR_INVALID"));
    if (newPassword !== confirmPassword) return setError(label("ORG_RESET_PWD_ERR_MISMATCH"));

    isSubmittingRef.current = true;
    setIsSubmitting(true);
    setError(null);
    try {
      const response = await PasswordResetService.resetOrganizationUserPassword({
        organizationId,
        targetUser: user,
        newPassword,
        tenantId: Digit.ULBService.getCurrentTenantId(),
      });
      if (!isMountedRef.current) return;
      setValues({ [NEW_PASSWORD]: "", [CONFIRM_PASSWORD]: "" }); // don't keep the password around
      // true / false / null - the service already normalises it (no `??`: webpack 4 cannot parse it).
      setResult({ emailSent: typeof response?.emailSent === "boolean" ? response.emailSent : null });
    } catch (err) {
      // err comes from PasswordResetService and never contains the password - safe to show.
      if (!isMountedRef.current) return;
      setError(err?.backendMessage || label(err?.i18nKey || "ORG_RESET_PWD_ERR_GENERIC"));
    } finally {
      isSubmittingRef.current = false;
      if (isMountedRef.current) setIsSubmitting(false);
    }
  };

  const ruleChecks = getPasswordRuleChecks(values[NEW_PASSWORD]);
  const canSubmit = isValidPassword(values[NEW_PASSWORD]) && !!values[CONFIRM_PASSWORD] && !isSubmitting;

  // --- Result ------------------------------------------------------------------------------------
  const renderResult = () => {
    const emailFailed = result.emailSent === false;
    const messageKey =
      result.emailSent === true
        ? "ORG_RESET_PWD_SUCCESS_EMAILED"
        : emailFailed
        ? "ORG_RESET_PWD_SUCCESS_EMAIL_FAILED"
        : "ORG_RESET_PWD_SUCCESS";
    return (
      <div>
        <p
          role="status"
          style={{
            margin: "0 0 24px",
            padding: "12px",
            fontSize: "16px",
            lineHeight: "22px",
            color: "#0B0C0C",
            // Amber when the email failed: the admin still has something to do.
            backgroundColor: emailFailed ? "#FFF4E5" : "#F1FFF8",
            borderLeft: `4px solid ${emailFailed ? "#D4351C" : "#00703C"}`,
          }}
        >
          {label(messageKey)}
        </p>
        <div style={{ display: "flex", justifyContent: "center" }}>
          <Button variation={"primary"} label={label("CORE_COMMON_CLOSE")} onButtonClick={onClose} />
        </div>
      </div>
    );
  };

  // --- Form --------------------------------------------------------------------------------------
  const renderForm = () => (
    <form onSubmit={onSubmit} noValidate>
      <div style={{ marginBottom: "16px" }}>
        <label htmlFor="orgResetNewPassword" style={labelStyle}>
          {label("ORG_RESET_PWD_NEW_PASSWORD")} *
        </label>
        {/* new-password stops the browser filling in the ADMIN's own saved password */}
        <input
          id="orgResetNewPassword"
          name={NEW_PASSWORD}
          type="password"
          autoComplete="new-password"
          autoCapitalize="off"
          spellCheck={false}
          value={values[NEW_PASSWORD]}
          onChange={onChange}
          disabled={isSubmitting}
          style={inputStyle}
        />
      </div>
      <div style={{ marginBottom: "16px" }}>
        <label htmlFor="orgResetConfirmPassword" style={labelStyle}>
          {label("ORG_RESET_PWD_CONFIRM_PASSWORD")} *
        </label>
        <input
          id="orgResetConfirmPassword"
          name={CONFIRM_PASSWORD}
          type="password"
          autoComplete="new-password"
          autoCapitalize="off"
          spellCheck={false}
          value={values[CONFIRM_PASSWORD]}
          onChange={onChange}
          disabled={isSubmitting}
          style={inputStyle}
        />
      </div>

      {/* Live checklist of the rule, ticked as the new password is typed. */}
      <div style={{ background: "#F6F6F6", borderRadius: "4px", padding: "8px 12px", marginBottom: "16px" }}>
        <div style={{ fontSize: "14px", fontWeight: 600, color: "#505A5F", marginBottom: "2px" }}>{label("ORG_RESET_PWD_RULES_TITLE")}</div>
        <ul aria-live="polite" style={{ listStyle: "none", margin: 0, padding: 0 }}>
          {ruleChecks.map((rule) => (
            <li key={rule.key} style={{ color: rule.passed ? "#00703C" : "#505A5F", fontSize: "13px", lineHeight: "20px" }}>
              <span aria-hidden="true">{rule.passed ? "✔ " : "• "}</span>
              {label(rule.labelKey)}
            </li>
          ))}
        </ul>
      </div>

      {error && (
        <p role="alert" style={{ color: "#D4351C", fontSize: "14px", margin: "0 0 16px" }}>
          {error}
        </p>
      )}

      <div style={{ display: "flex", justifyContent: "space-around", gap: "12px" }}>
        <Button variation={"secondary"} label={label("CORE_COMMON_CANCEL")} onButtonClick={close} isDisabled={isSubmitting} />
        <Button type="submit" variation={"primary"} label={label("ORG_RESET_PWD_SUBMIT")} onButtonClick={onSubmit} isDisabled={!canSubmit} />
      </div>
    </form>
  );

  return (
    <PopUp>
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="orgResetPasswordTitle"
        style={{
          backgroundColor: "white",
          position: "fixed",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
          width: "440px",
          maxWidth: "95%",
          maxHeight: "95vh",
          overflowY: "auto",
          padding: "24px",
          borderRadius: "5px",
          boxSizing: "border-box",
        }}
      >
        <div style={{ width: "100%", position: "relative" }}>
          <button
            type={"button"}
            aria-label={label("CORE_COMMON_CLOSE")}
            disabled={isSubmitting}
            style={{
              cursor: isSubmitting ? "not-allowed" : "pointer",
              position: "absolute",
              top: "-15px",
              right: "-15px",
              backgroundColor: "#D6D5D4",
              display: "flex",
              alignItems: "center",
              padding: "0",
              borderRadius: "3px",
            }}
            onClick={close}
          >
            <CustomCloseSvg fill={"transparent"} />
          </button>
        </div>
        <h2 id="orgResetPasswordTitle" style={{ margin: "0 0 8px", fontSize: "20px", fontWeight: "600", color: "#333", textAlign: "center" }}>
          {label("ORG_RESET_PWD_HEADER")}
        </h2>
        <p style={{ margin: "0 0 20px", fontSize: "15px", color: "#555", textAlign: "center", wordBreak: "break-word" }}>
          {label("ORG_RESET_PWD_FOR_USER")} <strong>{user?.name || user?.userName || "-"}</strong>
          {user?.name && user?.userName ? ` (${user.userName})` : ""}
        </p>
        {result ? renderResult() : renderForm()}
      </div>
    </PopUp>
  );
};

export default ResetPasswordModal;
