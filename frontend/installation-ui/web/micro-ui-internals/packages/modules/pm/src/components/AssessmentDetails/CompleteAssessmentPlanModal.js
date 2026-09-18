import React from "react";
import { Button, PopUp } from "@egovernments/digit-ui-react-components";

const CompleteAssessmentPlanModal = ({ t, loading, onConfirm, onClose }) => {

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
        <div style={{ fontFamily: "Roboto", fontWeight: 700, fontSize: "24px", color: "#0B0C0C", marginBottom: "12px" }}>
          {t("PM_ASSESSMENT_COMPLETE_PLAN_TITLE")}
        </div>

        <p style={{ fontSize: "16px", color: "#0B0C0C", marginBottom: "24px" }}>
          {t("PM_ASSESSMENT_COMPLETE_PLAN_DESC")}
        </p>

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
            onButtonClick={onConfirm}
            isDisabled={loading}
            style={{
              backgroundColor: "#0B4B66",
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

export default CompleteAssessmentPlanModal;
