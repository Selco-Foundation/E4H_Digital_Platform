import { CardLabel, CardLabelError, LabelFieldPair, MobileNumber, SubmitBar, Toast } from "@selco/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import CancelRequestModal from "./CancelRequestModal";
import { useLabel } from "./useLabel";

const LABEL_WIDTH = "300px"; // same label column width as the other rows on the profile page
const CONTENT_MAX_WIDTH = "540px"; // same width as the Mobile No. field above
const TOAST_MS = 5000;
const APPROVED_NOTE_DAYS = 2; // how long the "number was updated" confirmation stays on the profile after the CRM approves

const COLORS = { PENDING: "#F47738", REJECTED: "#D4351C", APPROVED: "#00703C", muted: "#505A5F", border: "#D6D5D4", brand: "#7a2829" };

const formatDate = (epochMillis) =>
  epochMillis ? new Date(epochMillis).toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" }) : "";

const formatNumber = (number) => (number ? `+91 ${number}` : "");

// True when the timestamp is within the last `days` days. A missing timestamp counts as "not recent".
const isWithinDays = (epochMillis, days) => !!epochMillis && Date.now() - epochMillis < days * 24 * 60 * 60 * 1000;

const linkButtonStyle = { color: COLORS.brand, cursor: "pointer", outline: "none", background: "none", border: "none", padding: 0, font: "inherit" };
const outlineButtonStyle = { color: COLORS.brand, border: `1px solid ${COLORS.brand}`, background: "#fff", padding: "8px 20px", cursor: "pointer", font: "inherit" };

// One row = label column (fixed width, long words wrap instead of pushing the field) + content column.
// marginBottom matches the vertical rhythm of the Name / Facility / Mobile No. rows above.
const Row = ({ label, children }) => (
  <LabelFieldPair style={{ display: "flex", alignItems: "flex-start", marginBottom: "24px" }}>
    <CardLabel className="profile-label-margin" style={{ flex: `0 0 ${LABEL_WIDTH}`, width: LABEL_WIDTH, overflowWrap: "anywhere" }}>
      {label}
    </CardLabel>
    <div style={{ width: "100%" }}>{children}</div>
  </LabelFieldPair>
);

// Status card with a coloured left edge: used for pending and rejected requests.
const StatusCard = ({ status, title, statusLabel, children }) => (
  <div
    style={{
      maxWidth: CONTENT_MAX_WIDTH,
      background: "#fff",
      border: `1px solid ${COLORS.border}`,
      borderLeft: `4px solid ${COLORS[status]}`,
      borderRadius: "4px",
      padding: "16px 20px",
    }}
  >
    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", flexWrap: "wrap", gap: "8px 12px", marginBottom: "12px" }}>
      <strong style={{ fontSize: "18px" }}>{title}</strong>
      <span style={{ background: COLORS[status], color: "#fff", padding: "2px 12px", borderRadius: "12px", fontSize: "14px", whiteSpace: "nowrap" }}>{statusLabel}</span>
    </div>
    {children}
  </div>
);

const Detail = ({ label, value }) => (
  <div style={{ display: "flex", gap: "12px", marginBottom: "6px" }}>
    <span style={{ flex: "0 0 150px", color: COLORS.muted }}>{label}</span>
    <strong style={{ overflowWrap: "anywhere" }}>{value}</strong>
  </div>
);

/**
 * "Change registered mobile number" section of the employee profile .
 *
 * Shown ONLY to HCR users (role COMPLAINANT). Renders nothing for everyone else, so it is
 * safe to mount unconditionally in UserProfile.
 *
 * What it shows depends on the user's LATEST request (backend returns own requests newest first):
 *   none / CANCELLED -> "Change Mobile Number" link
 *   PENDING          -> status card + Cancel Request
 *   REJECTED         -> status card with CRM remark + "Raise New Request"
 *   APPROVED         -> one-line note + "Change Mobile Number" link
 * The number itself is never editable here ; a request is only ever raised.
 *
 * @param {string}   currentMobileNumber    the number currently shown on the profile
 * @param {function} onMobileNumberUpdated  called with the new number when an approval is detected,
 *                                          so the profile can refresh its (session) copy of the number
 */
const MobileChangeRequestSection = ({ currentMobileNumber, onMobileNumberUpdated }) => {
  const tr = useLabel();
  const utils = Digit.Utils.mobileChangeRequest;
  const userInfo = Digit.UserService.getUser()?.info;
  const isHcr = utils.isHcrUser(userInfo);

  const [showForm, setShowForm] = useState(false);
  const [newNumber, setNewNumber] = useState("");
  const [touched, setTouched] = useState(false); // user has typed something
  const [blurred, setBlurred] = useState(false);
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [toast, setToast] = useState(null);

  // Hooks are always called (rules of hooks); `enabled` keeps non-HCR users from hitting the API.
  const { isLoading, isError, data, revalidate } = Digit.Hooks.im.useMobileChangeRequests({
    criteria: { limit: 1, offset: 0 },
    enabled: isHcr,
  });
  const { create, cancel } = Digit.Hooks.im.useMobileChangeRequestActions();

  const latest = data?.requests?.[0];

  const showToast = (type, label) => setToast({ type, label });

  // Auto-dismiss the toast; the cleanup stops the timer if the user leaves the page first.
  useEffect(() => {
    if (!toast) return undefined;
    const timer = setTimeout(() => setToast(null), TOAST_MS);
    return () => clearTimeout(timer);
  }, [toast]);

  // The profile keeps a copy of the number in the browser session. After the CRM approves, the
  // real number changes in the backend but that copy is stale until the user logs in again.
  // Detect "approved, and the session still holds the number that was current when the request
  // was raised" and hand the new number to the profile. Comparing with request.currentMobileNumber
  // (not just "latest is APPROVED") stops an old approval from overwriting a number that someone
  // else (e.g. SELCO via the Management Hub) changed later.
  useEffect(() => {
    if (
      latest?.status === utils.MOBILE_CHANGE_STATUS.APPROVED &&
      latest.newMobileNumber &&
      latest.currentMobileNumber === currentMobileNumber &&
      latest.newMobileNumber !== currentMobileNumber
    ) {
      onMobileNumberUpdated?.(latest.newMobileNumber);
    }
  }, [latest?.id, latest?.status, currentMobileNumber]);

  if (!isHcr) return null;

  const validationKey = utils.validateNewMobileNumber(newNumber, currentMobileNumber);
  // Show a message only once it is fair to: all 10 digits are in, or the user typed and then left the field.
  // An untouched empty field never shows an error (the disabled Submit button already says "not yet").
  const visibleErrorKey = validationKey && ((touched && blurred) || newNumber.length >= 10) ? validationKey : null;

  const closeForm = () => {
    setShowForm(false);
    setNewNumber("");
    setTouched(false);
    setBlurred(false);
  };

  const openForm = () => {
    closeForm();
    setShowForm(true);
  };

  const onNumberChange = (value) => {
    setTouched(true);
    setNewNumber(value);
  };

  const submit = () => {
    if (validationKey || create.isLoading) return;
    create.mutate(
      { newMobileNumber: newNumber.trim() },
      {
        onSuccess: () => {
          closeForm();
          showToast("success", tr("MOBILE_CHANGE_SUBMIT_SUCCESS"));
        },
        // e.g. ALREADY_PENDING / SAME_MOBILE_NUMBER raised by the backend. The hook also refetches,
        // so if a request is now pending the form is replaced by its status card.
        onError: (error) => showToast("error", tr(utils.getMobileChangeErrorKey(error))),
      }
    );
  };

  const confirmCancel = () => {
    setShowCancelModal(false);
    cancel.mutate(
      { id: latest.id },
      {
        onSuccess: () => showToast("success", tr("MOBILE_CHANGE_CANCEL_SUCCESS")),
        // NOT_PENDING here means the CRM acted first; the hook's refetch shows the real outcome.
        onError: (error) => showToast("error", tr(utils.getMobileChangeErrorKey(error))),
      }
    );
  };

  const renderBody = () => {
    if (isLoading) return null; // avoid a flash of the wrong state while we find out
    if (isError) {
      return (
        <div>
          <CardLabelError style={{ margin: 0, padding: 0 }}>{tr("MOBILE_CHANGE_ERR_GENERIC")}</CardLabelError>
          <button type="button" style={linkButtonStyle} onClick={revalidate}>
            {tr("MOBILE_CHANGE_RETRY")}
          </button>
        </div>
      );
    }

    //with a pending request there is no way to start another; show its status instead.
    if (latest?.isPending) {
      return (
        <StatusCard status="PENDING" title={tr("MOBILE_CHANGE_REQUEST_LABEL")} statusLabel={tr("MOBILE_CHANGE_STATUS_PENDING")}>
          <Detail label={tr("MOBILE_CHANGE_REQUESTED_NUMBER")} value={formatNumber(latest.newMobileNumber)} />
          <Detail label={tr("MOBILE_CHANGE_RAISED_ON")} value={formatDate(latest.createdTime)} />
          <div style={{ color: COLORS.muted, margin: "12px 0 16px" }}>{tr("MOBILE_CHANGE_PENDING_INFO")}</div>
          <button type="button" style={outlineButtonStyle} onClick={() => setShowCancelModal(true)} disabled={cancel.isLoading}>
            {tr("MOBILE_CHANGE_CANCEL_REQUEST")}
          </button>
        </StatusCard>
      );
    }

    if (showForm) {
      return (
        <div style={{ maxWidth: CONTENT_MAX_WIDTH }}>
          <MobileNumber
            value={newNumber}
            name="newMobileNumber"
            style={{ width: "100%" }}
            placeholder={tr("MOBILE_CHANGE_PLACEHOLDER")}
            onChange={onNumberChange}
            onBlur={() => setBlurred(true)}
            autoFocus
            {...{ required: true, pattern: "[6-9]{1}[0-9]{9}", type: "tel" }}
          />
          {visibleErrorKey && <CardLabelError style={{ margin: "4px 0 0", padding: 0 }}>{tr(visibleErrorKey)}</CardLabelError>}
          <div style={{ color: COLORS.muted, fontSize: "14px", margin: "8px 0 16px" }}>{tr("MOBILE_CHANGE_HINT")}</div>
          <div style={{ display: "flex", alignItems: "center", gap: "24px" }}>
            <SubmitBar
              label={tr("MOBILE_CHANGE_SUBMIT")}
              onSubmit={submit}
              disabled={!!validationKey || create.isLoading}
              style={{ width: "220px" }}
            />
            <button type="button" style={linkButtonStyle} onClick={closeForm}>
              {tr("MOBILE_CHANGE_CLOSE_FORM")}
            </button>
          </div>
        </div>
      );
    }

    return (
      <div style={{ maxWidth: CONTENT_MAX_WIDTH }}>
        {/*the user can see that the last request was rejected, and why.*/}
        {latest?.status === utils.MOBILE_CHANGE_STATUS.REJECTED && (
          <div style={{ marginBottom: "16px" }}>
            <StatusCard status="REJECTED" title={tr("MOBILE_CHANGE_REQUEST_LABEL")} statusLabel={tr("MOBILE_CHANGE_STATUS_REJECTED")}>
              <Detail label={tr("MOBILE_CHANGE_REQUESTED_NUMBER")} value={formatNumber(latest.newMobileNumber)} />
              {latest.comment && <Detail label={tr("MOBILE_CHANGE_REMARK")} value={latest.comment} />}
              <div style={{ color: COLORS.muted, marginTop: "8px" }}>{tr("MOBILE_CHANGE_REJECTED_INFO")}</div>
            </StatusCard>
          </div>
        )}
        {/* Only a confirmation: shown for a couple of days after approval, then the field above is the record. */}
        {latest?.status === utils.MOBILE_CHANGE_STATUS.APPROVED && isWithinDays(latest.actionTime, APPROVED_NOTE_DAYS) && (
          <div style={{ color: COLORS.APPROVED, marginBottom: "8px" }}>
            {tr("MOBILE_CHANGE_APPROVED_INFO")} {formatDate(latest.actionTime)}
          </div>
        )}
        {/*a fresh request is allowed after a rejection (or cancellation / approval).*/}
        {latest?.status === utils.MOBILE_CHANGE_STATUS.REJECTED ? (
          <button type="button" style={outlineButtonStyle} onClick={openForm}>
            {tr("MOBILE_CHANGE_RAISE_NEW")}
          </button>
        ) : (
          <button type="button" style={linkButtonStyle} onClick={openForm}>
            {tr("MOBILE_CHANGE_BUTTON")}
          </button>
        )}
      </div>
    );
  };

  return (
    <React.Fragment>
      <Row label={showForm && !latest?.isPending ? `${tr("MOBILE_CHANGE_NEW_NUMBER_LABEL")}*` : ""}>{renderBody()}</Row>
      {showCancelModal && <CancelRequestModal onConfirm={confirmCancel} onDismiss={() => setShowCancelModal(false)} />}
      {toast && <Toast error={toast.type === "error"} label={toast.label} onClose={() => setToast(null)} style={{ maxWidth: "670px" }} />}
    </React.Fragment>
  );
};

export default MobileChangeRequestSection;
