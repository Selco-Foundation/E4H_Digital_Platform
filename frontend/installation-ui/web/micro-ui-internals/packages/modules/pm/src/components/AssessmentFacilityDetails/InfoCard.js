import React from "react";

const InfoCard = ({ t, facility, phoneOutcome, fieldOutcome }) => {

  const InfoItem = (label, value) => (
    <div style={{ marginBottom: "16px" }}>
      <div style={{ fontSize: "14px", color: "#6B7280", marginBottom: "4px" }}>{label}</div>
      <div style={{ fontSize: "16px", fontWeight: 700, color: "#0B0C0C" }}>{value || "-"}</div>
    </div>
  );

  return (
    <div
      style={{
        width: "100%",
        background: "white",
        marginBottom: "15px",
        padding: "20px",
        borderRadius: "4px",
        boxShadow: "0px 0px 4px rgba(0, 0, 0, 0.25)",
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        columnGap: "40px",
      }}
    >
      {InfoItem(t("PM_ASSESSMENT_HF_TYPE"), facility?.facilityType)}
      {InfoItem(t("PM_ASSESSMENT_DISTRICT_BLOCK"), `${facility?.district || "-"} / ${facility?.block || "-"}`)}
      {InfoItem(t("PM_ASSESSMENT_REMOTE_STATUS"), facility?.remoteStatus ? t(`PM_ASSESSMENT_FACILITY_STATUS_${facility.remoteStatus}`) : "-")}
      {InfoItem(t("PM_ASSESSMENT_PHONE_OUTCOME"), t(`PM_ASSESSMENT_OUTCOME_${phoneOutcome}`))}
      {InfoItem(t("PM_ASSESSMENT_ONSITE_STATUS"), facility?.onSiteStatus ? t(`PM_ASSESSMENT_FACILITY_STATUS_${facility.onSiteStatus}`) : "-")}
      {InfoItem(t("PM_ASSESSMENT_FIELD_OUTCOME"), t(`PM_ASSESSMENT_OUTCOME_${fieldOutcome}`))}
      {InfoItem(t("PM_ASSESSMENT_OVERALL_STATUS"), facility?.result ? t(`PM_ASSESSMENT_FACILITY_STATUS_${facility.result}`) : "-")}
      {facility?.decisionReason && (
        InfoItem(t("PM_ASSESSMENT_DECISION_REASON"), facility.decisionReason)
      )}
    </div>
  );
};

export default InfoCard;
