import React from "react";
import { Button, PopUp } from "@egovernments/digit-ui-react-components";
import { CheckCircleOutline } from "@egovernments/digit-ui-svg-components";
import CustomCloseSvg from "../Custom/CustomCloseSvg";

const ConfirmActionModal = ({ t, title, description, message, confirmLabel, confirmColor = "#0B4B66", singleAction, loading, onConfirm, onClose }) => {

  return (
    <PopUp>
      <div
        style={{
          backgroundColor: "white",
          position: "fixed",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
          width: "500px",
          maxWidth: "95%",
          padding: "24px",
          borderRadius: "5px",
        }}
      >
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
          <div style={{ fontFamily: "Roboto", fontWeight: 700, fontSize: "24px", color: "#0B0C0C" }}>
            {title}
          </div>
          <button
            type={"button"}
            style={{ cursor: "pointer", background: "transparent", border: "none", padding: "0" }}
            onClick={onClose}
          >
            <CustomCloseSvg fill={"transparent"} />
          </button>
        </div>

        <p style={{ fontSize: "16px", color: "#0B0C0C", marginTop: "12px", marginBottom: "20px" }}>
          {description}
        </p>

        {message && (
          <div
            style={{
              display: "flex",
              alignItems: "flex-start",
              gap: "10px",
              backgroundColor: "#E7F6EC",
              borderRadius: "4px",
              padding: "16px",
              marginBottom: "24px",
            }}
          >
            <CheckCircleOutline fill={"#1B8354"} />
            <div style={{ fontSize: "15px", color: "#0B0C0C" }}>{message}</div>
          </div>
        )}

        <div style={{ display: "flex", justifyContent: "flex-end", gap: "12px" }}>
          {!singleAction && (
            <Button
              variation={"secondary"}
              label={t("CORE_COMMON_CANCEL")}
              onButtonClick={onClose}
              isDisabled={loading}
              style={{
                backgroundColor: "white",
                border: "1px solid #D6D5D4",
                color: "#0B0C0C",
                padding: "10px 24px",
                fontWeight: "500",
                fontSize: "16px",
              }}
            />
          )}
          <Button
            variation={"primary"}
            label={confirmLabel || t("CORE_COMMON_CONFIRM")}
            onButtonClick={onConfirm}
            isDisabled={loading}
            style={{
              backgroundColor: confirmColor,
              border: "none",
              color: "white",
              padding: "10px 24px",
              fontWeight: "500",
              fontSize: "16px",
            }}
          />
        </div>
      </div>
    </PopUp>
  );
};

export default ConfirmActionModal;
