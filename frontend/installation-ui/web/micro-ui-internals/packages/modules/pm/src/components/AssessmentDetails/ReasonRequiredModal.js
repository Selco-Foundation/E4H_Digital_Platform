import React, { useState } from "react";
import { Button, PopUp, TextArea } from "@egovernments/digit-ui-react-components";
import CustomCloseSvg from "../Custom/CustomCloseSvg";

const ReasonRequiredModal = ({ t, description, loading, onConfirm, onClose }) => {

  const [reason, setReason] = useState("");
  const [error, setError] = useState("");

  const handleConfirm = () => {
    if (!reason.trim()) {
      setError(t("CORE_COMMON_REQUIRED"));
      return;
    }

    onConfirm(reason);
  };

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
            {t("PM_ASSESSMENT_REASON_REQUIRED_TITLE")}
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

        <div style={{ marginBottom: "24px" }}>
          <div style={{ fontWeight: "500", fontSize: "14px", marginBottom: "6px" }}>
            {t("PM_ASSESSMENT_REASON_LABEL")} <span style={{ color: "#B91900" }}>*</span>
          </div>
          <TextArea
            name={"reason"}
            value={reason}
            maxlength={500}
            onChange={(e) => {
              setReason(e.target.value);
              setError("");
            }}
            style={{ fontFamily: "Roboto", width: "100%" }}
          />
          {error && <div style={{ color: "#B91900", fontSize: "13px", marginTop: "4px" }}>{error}</div>}
        </div>

        <div style={{ display: "flex", justifyContent: "flex-end", gap: "12px" }}>
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
          <Button
            variation={"primary"}
            label={t("CORE_COMMON_CONFIRM")}
            onButtonClick={handleConfirm}
            isDisabled={loading || !reason.trim()}
            style={{
              backgroundColor: "#B91900",
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

export default ReasonRequiredModal;
