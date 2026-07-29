import React from "react";
import CustomCloseSvg from "../Custom/CustomCloseSvg";
import { Button, PopUp, WarningIcon } from "@egovernments/digit-ui-react-components";

const ConfirmActionAlert = ({ t, alert, setAlert }) => {
  if (!alert) return null;
  return (
    <PopUp>
      <style>
        {`
          .qc-confirm-action-alert .submit-bar,
          .qc-confirm-action-alert .jk-digit-primary-btn {
            box-shadow: none;
          }
        `}
      </style>
      <div
        className="qc-confirm-action-alert"
        style={{
          backgroundColor: "white",
          position: "fixed",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
          width: "400px",
          maxWidth: "95%",
          padding: "24px",
          borderRadius: "5px",
        }}
      >
        <div
          style={{
            width: "100%",
            position: "relative",
          }}
        >
          <button
            type={"button"}
            aria-label={t("CORE_COMMON_CLOSE")}
            style={{
              cursor: "pointer",
              position: "absolute",
              top: "-15px",
              right: "-15px",
              backgroundColor: "#D6D5D4",
              display: "flex",
              alignItems: "center",
              padding: "0",
              borderRadius: "3px",
            }}
            onClick={() => setAlert(null)}
          >
            <CustomCloseSvg fill={"transparent"} />
          </button>
        </div>
        <h2
          style={{
            margin: "0 0 16px 0",
            fontSize: "20px",
            fontWeight: "600",
            color: "#333",
            textAlign: "center",
          }}
        >
          {t("CORE_COMMON_ALERT")}
        </h2>

        <p
          style={{
            fontSize: "16px",
            color: "#555",
            marginBottom: alert.irreversible ? "12px" : "24px",
            textAlign: "center",
          }}
        >
          {t(alert.messageKey, alert.messageParams)}
        </p>

        {alert.irreversible && (
          <div
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              gap: "6px",
              marginBottom: "24px",
            }}
          >
            <WarningIcon style={{ width: "20px", height: "20px", flexShrink: 0 }} />
            <span
              style={{
                fontSize: "14px",
                fontWeight: "600",
                color: "#B91900",
                textAlign: "center",
              }}
            >
              {t("QC_ACTION_CANNOT_BE_REVERSED")}
            </span>
          </div>
        )}

        <div style={{display: "flex", justifyContent: "space-around"}}>
          <Button variation={"secondary"} label={t("CORE_COMMON_CANCEL")} onButtonClick={() => setAlert(null)} />
          <Button
            variation={"primary"}
            label={t("CORE_COMMON_CONFIRM")}
            onButtonClick={() => {
              alert.confirmAction();
              setAlert(null);
            }}
          />
        </div>
      </div>
    </PopUp>
  )
}

export default ConfirmActionAlert;
