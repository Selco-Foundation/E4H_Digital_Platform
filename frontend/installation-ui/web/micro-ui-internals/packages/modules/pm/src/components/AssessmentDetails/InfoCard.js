import React from "react";

const InfoCard = ({ t, summary }) => {

  const StatCard = (label, value, valueColor) => (
    <div
      style={{
        border: "1px solid #D6D5D4",
        borderRadius: "4px",
        padding: "16px 20px",
        flex: "1 1 180px",
        minWidth: "180px",
      }}
    >
      <div style={{ fontSize: "14px", fontFamily: "Roboto", color: "#6B7280", marginBottom: "8px" }}>{label}</div>
      <div style={{ fontSize: "24px", fontWeight: 700, fontFamily: "Roboto", color: valueColor || "#0B0C0C" }}>{value}</div>
    </div>
  );

  return (
    <div
      style={{
        width: "100%",
        background: "white",
        height: "fit-content",
        marginBottom: "15px",
        padding: "20px",
        display: "flex",
        flexWrap: "wrap",
        gap: "15px",
      }}
    >
      {StatCard(t("PM_ASSESSMENT_TOTAL_FACILITIES"), summary?.totalFacilities || 0)}
      {StatCard(t("PM_ASSESSMENT_REMOTE_ASSESSMENTS"), `${summary?.remoteAssessmentsCompleted || 0} / ${summary?.totalFacilities || 0}`)}
      {StatCard(t("PM_ASSESSMENT_ONSITE_ASSESSMENTS"), `${summary?.onSiteAssessmentsCompleted || 0} / ${summary?.totalFacilities || 0}`)}
      {StatCard(t("PM_ASSESSMENT_ELIGIBLE"), summary?.eligibleCount || 0, "#1B8354")}
      {StatCard(t("PM_ASSESSMENT_NOT_ELIGIBLE"), summary?.notEligibleCount || 0, "#B91900")}
    </div>
  );
};

export default InfoCard;
