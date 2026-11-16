import React from "react";
import { useTranslation } from "react-i18next";

const ReportingStepper = ({ currentStep, showFacility = false, showTheft = false, showDescription = false }) => {
  const { t } = useTranslation();
  const steps = [
    ...(showFacility ? [{ key: "facility", label: t("IM_REPORT_STEP_FACILITY", { defaultValue: "Facility" }) }] : []),
    { key: "issue", label: t("IM_REPORT_STEP_COMPONENT", { defaultValue: "Component/Issue" }) },
    { key: "observation", label: showTheft ? t("IM_REPORT_STEP_STOLEN_COMPONENT", { defaultValue: "Stolen component" }) : t("IM_REPORT_STEP_OBSERVATION", { defaultValue: "Observation" }) },
    ...(showDescription ? [{ key: "description", label: t("IM_REPORT_STEP_DESCRIPTION", { defaultValue: "Description" }) }] : []),
    { key: "status", label: t("IM_REPORT_STEP_STATUS", { defaultValue: "System status" }) },
    ...(showTheft ? [{ key: "fir", label: t("IM_REPORT_STEP_FIR", { defaultValue: "FIR/Complaint letter" }) }] : []),
    { key: "details", label: t("IM_REPORT_STEP_DETAILS", { defaultValue: "Ticket details" }) },
  ];
  const currentIndex = steps.findIndex(({ key }) => key === currentStep);

  return (
    <ol aria-label={t("IM_REPORT_PROGRESS", { defaultValue: "Reporting progress" })}
      style={{ display: "flex", listStyle: "none", padding: 0, margin: "0 auto 24px", width: "100%" }}>
      {steps.map(({ key, label }, index) => {
        const active = index === currentIndex;
        const complete = index < currentIndex;
        return (
          <li key={key} aria-current={active ? "step" : undefined}
            style={{ flex: "1 1 0", minWidth: 0, position: "relative", textAlign: "center" }}>
            {index < steps.length - 1 && <span aria-hidden="true" style={{
              position: "absolute", top: "16px", left: "50%", width: "100%", height: "2px",
              backgroundColor: complete ? "#82212b" : "#d6d6d6",
            }} />}
            <span aria-hidden="true" style={{
              position: "relative", display: "flex", alignItems: "center", justifyContent: "center",
              width: "32px", height: "32px", margin: "0 auto 10px", borderRadius: "50%", boxSizing: "border-box",
              border: `2px solid ${active || complete ? "#82212b" : "#d6d6d6"}`,
              backgroundColor: active || complete ? "#82212b" : "#fff", color: active || complete ? "#fff" : "#505a5f",
              boxShadow: active ? "0 0 0 4px rgba(130, 33, 43, 0.12)" : "none",
            }}>{index + 1}</span>
            <span style={{ display: "block", padding: "0 4px", overflowWrap: "anywhere", fontWeight: active ? 700 : 400,
              color: active || complete ? "#0b0c0c" : "#505a5f" }}>{label}</span>
          </li>
        );
      })}
    </ol>
  );
};

export default ReportingStepper;
