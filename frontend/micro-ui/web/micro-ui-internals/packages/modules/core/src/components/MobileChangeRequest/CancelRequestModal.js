import { CardText, Modal } from "@selco/digit-ui-react-components";
import React from "react";
import { useLabel } from "./useLabel";

/**
 * Small confirmation popup shown before cancelling a PENDING request (criterion 15).
 * Cancelling is final: a cancelled request cannot be reopened, the user must raise a new one.
 */
const CancelRequestModal = ({ onConfirm, onDismiss }) => {
  const tr = useLabel();

  return (
    <Modal
      popupModuleMianStyles={{ paddingTop: "0px", color: "black" }}
      popupStyles={{ width: "400px" }}
      headerBarMain={<h1 className="heading-m">{tr("MOBILE_CHANGE_CANCEL_CONFIRM_TITLE")}</h1>}
      headerBarEnd={
        <span onClick={onDismiss} style={{ cursor: "pointer", color: "#fff", fontSize: "28px", padding: "0 12px", lineHeight: "1" }}>
          ×
        </span>
      }
      actionCancelLabel={tr("MOBILE_CHANGE_CANCEL_KEEP")}
      actionCancelOnSubmit={onDismiss}
      actionSaveLabel={tr("MOBILE_CHANGE_CANCEL_YES")}
      actionSaveOnSubmit={onConfirm}
      formId="mobile-change-cancel-modal"
    >
      <CardText style={{ marginBottom: "40px", marginLeft: "8px", marginRight: "8px" }}>{tr("MOBILE_CHANGE_CANCEL_CONFIRM_TEXT")}</CardText>
    </Modal>
  );
};

export default CancelRequestModal;
