import React from "react";
import { Button, PopUp } from "@egovernments/digit-ui-react-components";
import CustomCloseSvg from "./Custom/CustomCloseSvg";

/**
 * Confirmation for a change with consequences beyond the form: a title, what is changing
 * (`children`), the warnings that apply, then Cancel and Confirm. Cancel only closes it, so the
 * form behind it keeps its input.
 */
const ConfirmChangesModal = ({ t, title, description, warnings = [], onConfirm, onCancel, confirmLabel, children }) => (
  <PopUp>
    <div
      role="dialog"
      aria-modal="true"
      aria-label={t(title)}
      style={{
        backgroundColor: "white",
        position: "fixed",
        top: "50%",
        left: "50%",
        transform: "translate(-50%, -50%)",
        width: "560px",
        maxWidth: "95%",
        maxHeight: "85vh",
        overflow: "auto",
        padding: "24px",
        borderRadius: "5px",
        zIndex: 10000010,
      }}
    >
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: "12px" }}>
        <h2 style={{ margin: 0, fontSize: "24px", fontWeight: 700, fontFamily: "Roboto", color: "#0B0C0C" }}>{t(title)}</h2>
        <button
          type="button"
          aria-label={t("CORE_COMMON_CLOSE")}
          onClick={onCancel}
          style={{ cursor: "pointer", backgroundColor: "#D6D5D4", display: "flex", alignItems: "center", padding: 0, borderRadius: "3px", border: "none" }}
        >
          <CustomCloseSvg fill={"transparent"} />
        </button>
      </div>
      {description && <p style={{ margin: "0 0 16px", fontSize: "16px", color: "#505A5F" }}>{t(description)}</p>}
      {children}
      {warnings.length > 0 && (
        <div
          style={{
            margin: "16px 0 0",
            padding: "12px 16px",
            backgroundColor: "#FFF4E5",
            borderLeft: "4px solid #F19C3B",
            fontSize: "15px",
            color: "#0B0C0C",
          }}
        >
          {warnings.map((warning, index) => (
            <p key={index} style={{ margin: index ? "8px 0 0" : 0 }}>
              {warning}
            </p>
          ))}
        </div>
      )}
      <div style={{ display: "flex", justifyContent: "flex-end", gap: "16px", marginTop: "24px" }}>
        <Button variation={"secondary"} label={t("CORE_COMMON_CANCEL")} onButtonClick={onCancel} />
        <Button variation={"primary"} label={t(confirmLabel || "CORE_COMMON_CONFIRM")} onButtonClick={onConfirm} />
      </div>
    </div>
  </PopUp>
);

export default ConfirmChangesModal;
