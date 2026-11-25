import { Card, CardLabel, CardLabelError, CardSubHeader, CardText, OTPInput, SubmitBar, TextInput } from "@selco/digit-ui-react-components";
import PropTypes from "prop-types";
import React, { Fragment, useEffect, useRef, useState } from "react";
import useInterval from "../../../hooks/useInterval";
import { OtpLoginService } from "../../../services/OtpLoginService";
import {
  MOBILE_INPUT_MAX_LENGTH,
  OTP_LENGTH,
  formatCountdown,
  formatRoleCode,
  isCompleteOtp,
  isValidMobile,
  maskMobile,
  normalizeMobile,
  sanitizeMobileInput,
  sanitizeOtp,
  summarizeRoles,
} from "../../../utilities/otpLogin";

const STEP_MOBILE = "mobile";
const STEP_OTP = "otp";
const STEP_ACCOUNT = "account";

// Below this many seconds the account-selection countdown turns red.
const TICKET_WARNING_SECONDS = 20;

// Role labels shown per account row before "+N more" (the full list is in the hover tooltip).
const MAX_ROLES_SHOWN = 2;

const BRAND_COLOR = "#7a2829";
const BRAND_TINT = "rgba(122, 40, 41, 0.06)"; // BRAND_COLOR at 6%: selected-row background
const TEXT_SECONDARY = "#505a5f";
const BORDER_COLOR = "#d6d5d4";
const ERROR_COLOR = "#d4351c";

// One line that ends in "…" instead of widening the card.
const singleLine = { overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" };

// Matches the look of the existing "Forgot Password?" link on the login page.
const linkButtonStyle = {
  background: "transparent",
  border: "none",
  color: "blue",
  cursor: "pointer",
  padding: 0,
  textDecoration: "underline",
};

/** Converts a thrown LoginError (see services/OtpLoginService.js) into { key, fallback } for rendering. */
const messageFromError = (err) => ({
  key: err?.i18nKey || "ERR_SOMETHING_WENT_WRONG",
  fallback: err?.fallback || "Something went wrong. Please try again.",
});

/** Whole seconds left until `timestamp` (ms), never negative. */
const secondsUntil = (timestamp, now) => Math.max(0, Math.ceil((timestamp - now) / 1000));

/** Same key pattern the rest of the app uses for tenant names (e.g. TENANT_TENANTS_RJ). */
const tenantI18nKey = (tenantId) => `TENANT_TENANTS_${String(tenantId).replace(/\./g, "_").toUpperCase()}`;

/**
 * Employee login with mobile number + OTP, in up to three steps:
 *
 *   1. Mobile   -> POST /user/otp-login/v1/_send
 *   2. OTP      -> POST /user/otp-login/v1/_verify  (returns a login ticket + linked accounts)
 *   3. Account  -> only when the number is linked to more than one account;
 *                  with a single account the login happens straight after step 2.
 *   Then        -> POST /user/oauth/token with the chosen account and the ticket.
 *
 * Kept OUT of this component:
 *  - API details -> services/OtpLoginService.js
 *  - what happens after login (session, HRMS lookup, redirect) -> login.js via onAuthenticated,
 *    so password and OTP login share one post-login path.
 */
const OtpLogin = ({ tf, canProceed, onBlocked, onAuthenticated, header, consent, className, cardStyle, buttonStyle }) => {
  const [step, setStep] = useState(STEP_MOBILE);
  const [mobileInput, setMobileInput] = useState("");
  const [sentTo, setSentTo] = useState(""); // normalized number the current OTP was sent to
  const [otp, setOtp] = useState("");
  const [otpValiditySeconds, setOtpValiditySeconds] = useState(0);
  const [accounts, setAccounts] = useState([]);
  const [selectedKey, setSelectedKey] = useState("");
  const [error, setError] = useState(null); // { key, fallback }
  const [notice, setNotice] = useState(null); // { key, fallback } for non-error messages
  const [pending, setPending] = useState(null); // "send" | "resend" | "verify" | "login" | null

  /*
   * Timers are stored as end timestamps, not counters, because browsers slow
   * down intervals in background tabs. `now` is refreshed every second only
   * while a timer is running, which re-renders the countdowns.
   */
  const [now, setNow] = useState(() => Date.now());
  const [resendAvailableAt, setResendAvailableAt] = useState(0);
  const [ticketExpiresAt, setTicketExpiresAt] = useState(0);

  // The login ticket is a short-lived password: kept in memory only, never in state
  // shown on screen, storage, or logs, and wiped as soon as it is used or expires.
  const loginTicketRef = useRef("");

  /*
   * Every request gets an id. "Change number" or a newer request bumps the id,
   * so a slow response from an older request is ignored instead of overwriting
   * the current screen.
   */
  const requestIdRef = useRef(0);
  const isMountedRef = useRef(true);

  useEffect(
    () => () => {
      isMountedRef.current = false;
      loginTicketRef.current = "";
    },
    []
  );

  const resendSecondsLeft = secondsUntil(resendAvailableAt, now);
  const ticketSecondsLeft = secondsUntil(ticketExpiresAt, now);
  const timerRunning = resendSecondsLeft > 0 || (step === STEP_ACCOUNT && ticketExpiresAt > 0);

  useInterval(() => setNow(Date.now()), timerRunning ? 1000 : null);

  /* ------------------------------ Helpers ------------------------------- */

  const startRequest = (kind) => {
    requestIdRef.current += 1;
    setPending(kind);
    setError(null);
    setNotice(null);
    return requestIdRef.current;
  };

  const isCurrentRequest = (id) => isMountedRef.current && id === requestIdRef.current;

  const clearTicket = () => {
    loginTicketRef.current = "";
    setTicketExpiresAt(0);
    setAccounts([]);
    setSelectedKey("");
  };

  /**
   * Sends the user back to the OTP step with "Resend OTP" available at once.
   * Used when the ticket expired or the token call failed: the OTP was already
   * used, so a new one is needed.
   */
  const restartFromOtp = (message) => {
    clearTicket();
    setOtp("");
    setStep(STEP_OTP);
    setResendAvailableAt(0);
    setNow(Date.now());
    setError(message);
    setPending(null);
  };

  /* ---------------------------- Step 1: mobile ---------------------------- */

  const handleMobileChange = (event) => {
    setMobileInput(sanitizeMobileInput(event.target.value));
    if (error) setError(null);
  };

  const applySendResult = ({ resendAfterSeconds, otpValiditySeconds: validity }) => {
    const current = Date.now();
    setNow(current);
    setResendAvailableAt(current + resendAfterSeconds * 1000);
    setOtpValiditySeconds(validity);
  };

  const handleSendOtp = async (event) => {
    event?.preventDefault?.();
    if (pending) return; // blocks double submit (double click or Enter + click)

    if (!canProceed) {
      onBlocked?.();
      return;
    }

    const mobile = normalizeMobile(mobileInput);
    if (!isValidMobile(mobile)) {
      setError({ key: "ERR_INVALID_MOBILE_NUMBER", fallback: "Enter a valid 10-digit mobile number." });
      return;
    }

    const requestId = startRequest("send");
    try {
      const result = await OtpLoginService.sendLoginOtp(mobile);
      if (!isCurrentRequest(requestId)) return;
      setSentTo(mobile);
      setOtp("");
      applySendResult(result);
      setStep(STEP_OTP);
      setPending(null);
    } catch (err) {
      if (!isCurrentRequest(requestId)) return;
      setError(messageFromError(err));
      setPending(null);
    }
  };

  /* ----------------------------- Step 2: OTP ------------------------------ */

  const handleOtpChange = (value) => {
    setOtp(sanitizeOtp(value, OTP_LENGTH));
    if (error) setError(null);
  };

  const handleResendOtp = async () => {
    if (pending || resendSecondsLeft > 0 || !sentTo) return;

    const requestId = startRequest("resend");
    try {
      const result = await OtpLoginService.sendLoginOtp(sentTo);
      if (!isCurrentRequest(requestId)) return;
      setOtp(""); // the old OTP is no longer valid
      applySendResult(result);
      setNotice({ key: "CORE_LOGIN_OTP_RESENT", fallback: "A new OTP has been sent." });
      setPending(null);
    } catch (err) {
      if (!isCurrentRequest(requestId)) return;
      setError(messageFromError(err));
      setPending(null);
    }
  };

  const handleVerifyOtp = async (event) => {
    event?.preventDefault?.();
    if (pending) return;

    if (!isCompleteOtp(otp, OTP_LENGTH)) {
      setError({ key: "ERR_ENTER_COMPLETE_OTP", fallback: `Enter the ${OTP_LENGTH}-digit OTP.` });
      return;
    }

    const requestId = startRequest("verify");
    let result;
    try {
      result = await OtpLoginService.verifyLoginOtp(sentTo, otp);
    } catch (err) {
      if (!isCurrentRequest(requestId)) return;
      setError(messageFromError(err));
      setOtp(""); // let the user type a fresh OTP straight away
      setPending(null);
      return;
    }
    if (!isCurrentRequest(requestId)) return;

    loginTicketRef.current = result.loginTicket;
    const current = Date.now();
    // 0 = the backend sent no validity: no countdown here, the backend still rejects an expired ticket.
    const expiresAt = result.ticketValiditySeconds > 0 ? current + result.ticketValiditySeconds * 1000 : 0;
    setNow(current);
    setTicketExpiresAt(expiresAt);
    setAccounts(result.accounts);

    if (result.accounts.length === 1) {
      // Nothing to choose: log in straight away.
      setSelectedKey(result.accounts[0].key);
      await performLogin(result.accounts[0], expiresAt);
      return;
    }

    setSelectedKey("");
    setStep(STEP_ACCOUNT);
    setPending(null);
  };

  /* --------------------------- Step 3: account ---------------------------- */

  /**
   * Exchanges the ticket for a session for `account`.
   * `expiresAt` is passed in because, straight after _verify, the state update
   * for ticketExpiresAt has not been applied yet.
   */
  const performLogin = async (account, expiresAt = ticketExpiresAt) => {
    const ticket = loginTicketRef.current;
    if (!ticket || (expiresAt > 0 && Date.now() >= expiresAt)) {
      restartFromOtp({
        key: "ERR_OTP_LOGIN_TICKET_EXPIRED",
        fallback: "Time to choose an account ran out. Please request a new OTP.",
      });
      return;
    }

    const requestId = startRequest("login");
    try {
      const authResponse = await OtpLoginService.loginWithTicket(account, ticket);
      if (!isCurrentRequest(requestId)) return;

      loginTicketRef.current = ""; // used; never keep it after a successful login
      // login.js stores the session and redirects; this component then unmounts.
      await onAuthenticated(authResponse, account);
      if (isMountedRef.current) setPending(null);
    } catch (err) {
      if (!isCurrentRequest(requestId)) return;
      const message = messageFromError(err);

      if (err?.isNetworkError) {
        // The request never reached the server, so the ticket is unused: show the
        // account list (even for one account) so the user can simply press Login again.
        setStep(STEP_ACCOUNT);
        setError(message);
        setPending(null);
        return;
      }

      // The server rejected the login. The ticket may be single-use, so start
      // again from a fresh OTP rather than retrying with it.
      restartFromOtp(message);
    }
  };

  const handleAccountLogin = (event) => {
    event?.preventDefault?.();
    if (pending) return;
    const account = accounts.find((item) => item.key === selectedKey);
    if (!account) {
      setError({ key: "ERR_SELECT_ACCOUNT", fallback: "Select an account to continue." });
      return;
    }
    performLogin(account);
  };

  // Ticket ran out while the user was choosing (skipped while a login request is in flight).
  useEffect(() => {
    if (step === STEP_ACCOUNT && !pending && ticketExpiresAt && now >= ticketExpiresAt) {
      restartFromOtp({
        key: "ERR_OTP_LOGIN_TICKET_EXPIRED",
        fallback: "Time to choose an account ran out. Please request a new OTP.",
      });
    }
    // restartFromOtp only uses state setters, so it is safe to leave out.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [now, step, pending, ticketExpiresAt]);

  const handleChangeNumber = () => {
    requestIdRef.current += 1; // ignore any request still in flight
    clearTicket();
    setPending(null);
    setStep(STEP_MOBILE);
    setOtp("");
    setSentTo("");
    setError(null);
    setNotice(null);
    setResendAvailableAt(0);
    // mobileInput is kept so the user can correct one digit instead of retyping.
  };

  /* -------------------------------- Render -------------------------------- */

  const showTenant = new Set(accounts.map((account) => account.tenantId)).size > 1;

  const formatValidity = (seconds) =>
    seconds >= 60
      ? `${Math.ceil(seconds / 60)} ${tf("CORE_COMMON_MINUTES", "minutes")}`
      : `${seconds} ${tf("CORE_COMMON_SECONDS", "seconds")}`;

  const submitConfig = {
    [STEP_MOBILE]: {
      onSubmit: handleSendOtp,
      label: pending === "send" ? tf("CORE_LOGIN_SENDING_OTP", "Sending OTP...") : tf("CORE_LOGIN_SEND_OTP", "Send OTP"),
      disabled: !mobileInput.trim(),
    },
    [STEP_OTP]: {
      onSubmit: handleVerifyOtp,
      label:
        pending === "verify" || pending === "login"
          ? tf("CORE_LOGIN_VERIFYING_OTP", "Verifying...")
          : tf("CORE_LOGIN_VERIFY_OTP", "Verify & Login"),
      disabled: !isCompleteOtp(otp, OTP_LENGTH),
    },
    [STEP_ACCOUNT]: {
      onSubmit: handleAccountLogin,
      label: pending === "login" ? tf("CORE_LOGIN_LOGGING_IN", "Logging in...") : tf("CORE_LOGIN_CONTINUE_AS_ACCOUNT", "Login"),
      disabled: !selectedKey,
    },
  }[step];

  const isSubmitDisabled = Boolean(pending) || !canProceed || submitConfig.disabled;

  const renderMobileStep = () => (
    <Fragment>
      <CardLabel>
        {tf("CORE_LOGIN_MOBILE_NUMBER", "Mobile Number")} *
      </CardLabel>
      <TextInput
        id="employee-otp-mobile"
        name="mobileNumber"
        type="tel" // numeric keypad on phones; still accepts "+91" and spaces
        value={mobileInput}
        onChange={handleMobileChange}
        maxlength={MOBILE_INPUT_MAX_LENGTH}
        placeholder={tf("CORE_LOGIN_MOBILE_PLACEHOLDER", "10-digit mobile number")}
        disabled={Boolean(pending)}
        autoFocus
        style={{ marginBottom: "5px" }}
      />
    </Fragment>
  );

  const renderOtpStep = () => (
    <Fragment>
      <CardText>
        {tf("CORE_LOGIN_OTP_SENT_TO", "OTP sent to")} +91 {maskMobile(sentTo)}
        {otpValiditySeconds > 0 && (
          <Fragment>
            . {tf("CORE_LOGIN_OTP_VALID_FOR", "It is valid for")} {formatValidity(otpValiditySeconds)}.
          </Fragment>
        )}
      </CardText>
      <CardLabel>
        {tf("CORE_LOGIN_ENTER_OTP", "Enter OTP")} *
      </CardLabel>
      <OTPInput length={OTP_LENGTH} value={otp} onChange={handleOtpChange} />

      <div style={{ margin: "12px 0" }}>
        {resendSecondsLeft > 0 ? (
          <CardText style={{ margin: 0 }}>
            {tf("CORE_LOGIN_RESEND_OTP_IN", "Resend OTP in")} {resendSecondsLeft}s
          </CardText>
        ) : (
          <button type="button" onClick={handleResendOtp} disabled={Boolean(pending)} style={linkButtonStyle}>
            {pending === "resend" ? tf("CORE_LOGIN_SENDING_OTP", "Sending OTP...") : tf("CORE_LOGIN_RESEND_OTP", "Resend OTP")}
          </button>
        )}
      </div>
    </Fragment>
  );

  const renderAccountStep = () => (
    <Fragment>
      {/* Short, small and centred under the heading, like the other steps' helper text. */}
      <p style={{ color: TEXT_SECONDARY, fontSize: "14px", lineHeight: "20px", margin: "0 0 12px", textAlign: "center" }}>
        {accounts.length > 1
          ? tf("CORE_LOGIN_MULTIPLE_ACCOUNTS", "Choose the account to log in with.")
          : tf("CORE_LOGIN_CONFIRM_ACCOUNT", "Log in with this account.")}
      </p>

      {/* A real radio group: arrow keys move between accounts and screen readers announce "1 of 3". */}
      <fieldset style={{ border: "none", margin: 0, padding: 0, minWidth: 0 }} disabled={Boolean(pending)}>
        <legend style={{ position: "absolute", left: "-9999px" }}>{tf("CORE_LOGIN_SELECT_ACCOUNT", "Select account")}</legend>
        {accounts.map((account) => {
          const isSelected = account.key === selectedKey;
          // Translation when one exists, otherwise "AMC_REVIEWER" -> "AMC Reviewer".
          const roleLabels = account.roleCodes.map((code) => tf(`ACCESSCONTROL_ROLES_ROLES_${code}`, formatRoleCode(code)));
          const tenantLabel = showTenant ? tf(tenantI18nKey(account.tenantId), account.tenantId) : "";
          const details = [summarizeRoles(roleLabels, MAX_ROLES_SHOWN), tenantLabel].filter(Boolean).join(" · ");
          const allDetails = [roleLabels.join(", "), tenantLabel].filter(Boolean).join(" · ");

          return (
            <label
              key={account.key}
              title={allDetails} // full role list on hover; the row itself stays one line
              style={{
                alignItems: "flex-start",
                background: isSelected ? BRAND_TINT : "transparent",
                border: `1px solid ${isSelected ? BRAND_COLOR : BORDER_COLOR}`,
                borderRadius: "4px",
                cursor: pending ? "default" : "pointer",
                display: "flex",
                gap: "10px",
                marginBottom: "8px",
                padding: "8px 12px",
              }}
            >
              <input
                type="radio"
                name="employee-otp-account"
                value={account.key}
                checked={isSelected}
                onChange={() => {
                  setSelectedKey(account.key);
                  if (error) setError(null);
                }}
                style={{ accentColor: BRAND_COLOR, flexShrink: 0, margin: "3px 0 0" }}
              />
              {/* minWidth 0 lets the text shrink and end in "…" instead of widening the card. */}
              <span style={{ display: "flex", flex: 1, flexDirection: "column", gap: "2px", minWidth: 0 }}>
                {/* Names can repeat across accounts, so the username leads; the name follows in grey. */}
                <span style={{ ...singleLine, fontSize: "15px", lineHeight: "20px" }}>
                  <strong>{account.userName}</strong>
                  {account.name && <span style={{ color: TEXT_SECONDARY }}> · {account.name}</span>}
                </span>
                {details && <span style={{ ...singleLine, color: TEXT_SECONDARY, fontSize: "13px", lineHeight: "18px" }}>{details}</span>}
              </span>
            </label>
          );
        })}
      </fieldset>

      {/* Quiet until the last 20 seconds, then red. Shown only when the backend sent a validity. */}
      {ticketExpiresAt > 0 && (
        <p
          style={{
            color: ticketSecondsLeft <= TICKET_WARNING_SECONDS ? ERROR_COLOR : TEXT_SECONDARY,
            fontSize: "13px",
            margin: "0 0 12px",
            textAlign: "right",
          }}
        >
          {tf("CORE_LOGIN_CHOOSE_WITHIN", "Expires in")} {formatCountdown(ticketSecondsLeft)}
        </p>
      )}
    </Fragment>
  );

  /*
   * The card never grows past the password card's width (its minWidth): long
   * content wraps or ends in "…" instead, so the page doesn't jump between steps.
   */
  // Same box model as minWidth, so the cap equals the password card's width exactly.
  const fixedWidthCardStyle = { ...cardStyle, maxWidth: (cardStyle && cardStyle.minWidth) || "100%" };

  return (
    // noValidate: our own validation shows translated messages instead of browser pop-ups.
    <form onSubmit={submitConfig.onSubmit} className={className} noValidate>
      <Card style={fixedWidthCardStyle}>
        {header}
        <CardSubHeader style={{ textAlign: "center" }}>{tf("CORE_LOGIN_WITH_OTP_HEADER", "Login with OTP")}</CardSubHeader>

        {step === STEP_MOBILE && renderMobileStep()}
        {step === STEP_OTP && renderOtpStep()}
        {step === STEP_ACCOUNT && renderAccountStep()}

        {/* role="alert" makes screen readers announce errors as soon as they appear. */}
        <div role="alert" aria-live="assertive">
          {error && <CardLabelError style={{ marginBottom: "12px" }}>{tf(error.key, error.fallback)}</CardLabelError>}
        </div>
        <div aria-live="polite">{notice && <CardText style={{ color: "#00703c" }}>{tf(notice.key, notice.fallback)}</CardText>}</div>

        {/* Consent checkboxes are needed before an OTP is sent, same as before password login. */}
        {step === STEP_MOBILE && consent}

        <SubmitBar label={submitConfig.label} submit="submit" disabled={isSubmitDisabled} style={buttonStyle} className="w-full" />

        {step !== STEP_MOBILE && (
          <div style={{ textAlign: "center", marginTop: "16px" }}>
            <button type="button" onClick={handleChangeNumber} style={linkButtonStyle}>
              {tf("CORE_LOGIN_CHANGE_MOBILE_NUMBER", "Change mobile number")}
            </button>
          </div>
        )}
      </Card>
    </form>
  );
};

OtpLogin.propTypes = {
  /** translate-with-fallback: (key, englishFallback) => string */
  tf: PropTypes.func.isRequired,
  /** false until required consents are accepted */
  canProceed: PropTypes.bool,
  /** called when the user tries to continue without consent */
  onBlocked: PropTypes.func,
  /** (authResponse, account) => void; login.js stores the session and redirects */
  onAuthenticated: PropTypes.func.isRequired,
  header: PropTypes.node,
  consent: PropTypes.node,
  className: PropTypes.string,
  cardStyle: PropTypes.object,
  buttonStyle: PropTypes.object,
};

OtpLogin.defaultProps = {
  canProceed: true,
  onBlocked: undefined,
  header: null,
  consent: null,
  className: "",
  cardStyle: {},
  buttonStyle: {},
};

export default OtpLogin;
