import React, { useState } from "react";
import { Button, Dropdown, PopUp, TextArea } from "@egovernments/digit-ui-react-components";
import CustomCloseSvg from "../Custom/CustomCloseSvg";
import { ASSESSMENT_NOT_ELIGIBLE_REASON_CODES } from "../../utilities/AssessmentPlanData";

const ReasonRequiredModal = ({ t, description, loading, onConfirm, onClose }) => {

  const [reason, setReason] = useState(null);
  const [remarks, setRemarks] = useState("");
  const [error, setError] = useState("");

  const reasonMenu = ASSESSMENT_NOT_ELIGIBLE_REASON_CODES.map((code) => ({ code, name: t(`PM_ASSESSMENT_NOT_ELIGIBLE_REASON_${code}`) }));

  const handleConfirm = () => {
    if (!reason?.code) {
      setError(t("CORE_COMMON_REQUIRED"));
      return;
    }

    onConfirm(reason.name, remarks);
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

        <div style={{ marginBottom: "16px" }}>
          <div style={{ fontWeight: "500", fontSize: "14px", marginBottom: "6px" }}>
            {t("PM_ASSESSMENT_REASON_LABEL")} <span style={{ color: "#B91900" }}>*</span>
          </div>
          <div className={"custom-dropdown"}>
            <Dropdown
              t={t}
              option={reasonMenu}
              selected={reason || { name: "", code: "" }}
              select={(value) => {
                setReason(value);
                setError("");
              }}
              optionKey={"name"}
            />
          </div>
          {error && <div style={{ color: "#B91900", fontSize: "13px", marginTop: "4px" }}>{error}</div>}
        </div>

        <div style={{ marginBottom: "24px" }}>
          <div style={{ fontWeight: "500", fontSize: "14px", marginBottom: "6px" }}>
            {t("PM_ASSESSMENT_REMARKS_LABEL")}
          </div>
          <TextArea
            name={"remarks"}
            value={remarks}
            onChange={(e) => setRemarks(e.target.value)}
            placeholder={t("PM_ASSESSMENT_REMARKS_PLACEHOLDER")}
            style={{ fontFamily: "Roboto", width: "100%" }}
          />
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
            isDisabled={loading}
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
