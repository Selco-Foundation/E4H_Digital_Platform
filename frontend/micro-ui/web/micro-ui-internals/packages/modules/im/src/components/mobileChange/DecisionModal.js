import { CardLabelError, CardText, Modal, TextArea } from "@selco/digit-ui-react-components";
import React, { useState } from "react";
import { useLabel } from "./labels";

/**
 * ASSUMPTION (open question for the backend/BA): a rejection must carry a reason, because the HCR sees it
 * as the "CRM remark" on their profile. Approval's comment stays optional. Flip this one constant if the
 * business decides otherwise; the backend accepts an empty comment either way (spec: comment optional).
 */
export const REJECT_COMMENT_REQUIRED = true;

/** Backend column size is not documented; keep the comment to a sensible length until confirmed. */
export const COMMENT_MAX_LENGTH = 500;

/**
 * Popup shown when the CRM clicks Approve or Reject on a PENDING request (criteria 10-12).
 *
 * @param {"APPROVE"|"REJECT"} type
 * @param {object}   request    normalised request (see Digit.Utils.mobileChangeRequest)
 * @param {boolean}  submitting true while the API call is running; disables the confirm button
 * @param {function} onConfirm  called with the trimmed comment (string, possibly empty)
 * @param {function} onDismiss  close without doing anything
 */
const DecisionModal = ({ type, request, submitting, onConfirm, onDismiss }) => {
  const tr = useLabel();
  const [comment, setComment] = useState("");
  const [showError, setShowError] = useState(false);

  const isReject = type === "REJECT";
  const commentRequired = isReject && REJECT_COMMENT_REQUIRED;
  const trimmed = comment.trim();
  const missingRequiredComment = commentRequired && !trimmed;

  const handleConfirm = () => {
    if (submitting) return; // guard against a double click while the call is in flight
    if (missingRequiredComment) {
      setShowError(true);
      return;
    }
    onConfirm(trimmed);
  };

  const textVars = {
    userName: request.userName || "-",
    current: request.currentMobileNumber ? `+91 ${request.currentMobileNumber}` : "-",
    next: `+91 ${request.newMobileNumber}`,
  };

  return (
    <Modal
      popupModuleMianStyles={{ paddingTop: "0px", color: "black" }}
      popupStyles={{ width: "460px", maxWidth: "95vw" }}
      headerBarMain={<h1 className="heading-m">{tr(isReject ? "MOBILE_CHANGE_REJECT_TITLE" : "MOBILE_CHANGE_APPROVE_TITLE")}</h1>}
      headerBarEnd={
        <span onClick={onDismiss} style={{ cursor: "pointer", color: "#fff", fontSize: "28px", padding: "0 12px", lineHeight: "1" }}>
          ×
        </span>
      }
      actionCancelLabel={tr("MOBILE_CHANGE_GO_BACK")}
      actionCancelOnSubmit={onDismiss}
      actionSaveLabel={tr(isReject ? "MOBILE_CHANGE_CONFIRM_REJECT" : "MOBILE_CHANGE_CONFIRM_APPROVE")}
      actionSaveOnSubmit={handleConfirm}
      isDisabled={submitting}
      formId="mobile-change-decision-modal"
    >
      <div style={{ margin: "0 8px 40px" }}>
        <CardText style={{ marginBottom: "16px" }}>{tr(isReject ? "MOBILE_CHANGE_REJECT_TEXT" : "MOBILE_CHANGE_APPROVE_TEXT", textVars)}</CardText>
        <label htmlFor="mobile-change-comment" style={{ display: "block", marginBottom: "8px", fontWeight: "bold" }}>
          {tr(commentRequired ? "MOBILE_CHANGE_COMMENT_LABEL" : "MOBILE_CHANGE_COMMENT_OPTIONAL")}
          {commentRequired ? " *" : ""}
        </label>
        <TextArea
          id="mobile-change-comment"
          name="mobile-change-comment"
          value={comment}
          maxlength={COMMENT_MAX_LENGTH}
          placeholder={tr("MOBILE_CHANGE_COMMENT_PLACEHOLDER")}
          style={{ width: "100%", minHeight: "90px", boxSizing: "border-box" }}
          onChange={(e) => {
            setComment(e.target.value);
            setShowError(false);
          }}
        />
        {showError && missingRequiredComment ? <CardLabelError style={{ marginTop: "4px" }}>{tr("MOBILE_CHANGE_COMMENT_REQUIRED")}</CardLabelError> : null}
      </div>
    </Modal>
  );
};

export default DecisionModal;
