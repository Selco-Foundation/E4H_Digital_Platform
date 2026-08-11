import React from "react";
import { Button, PopUp } from "@egovernments/digit-ui-react-components";
import CustomCloseSvg from "../Custom/CustomCloseSvg";
import { getAssessmentResponses, getFieldOutcome, getPhoneOutcome } from "../../utilities/AssessmentPlanData";

const FacilityDetailsModal = ({
  t,
  facility,
  planCompleted,
  canAssignOnSite,
  onClose,
  onAssignOnSite,
  onMarkEligible,
  onMarkNotEligible,
}) => {

  if (!facility) return null;

  const { remoteResponses, siteResponses } = getAssessmentResponses(facility);
  const phoneOutcome = getPhoneOutcome(facility);
  const fieldOutcome = getFieldOutcome(facility);

  const InfoItem = (label, value) => (
    <div style={{ marginBottom: "16px" }}>
      <div style={{ fontSize: "14px", color: "#6B7280", marginBottom: "4px" }}>{label}</div>
      <div style={{ fontSize: "16px", fontWeight: 700, color: "#0B0C0C" }}>{value || "-"}</div>
    </div>
  );

  const ResponseList = (title, responses) => (
    <div
      style={{
        backgroundColor: "#F1F1F1",
        borderRadius: "4px",
        padding: "16px 20px",
        marginTop: "16px",
      }}
    >
      <div style={{ fontSize: "18px", fontWeight: 700, color: "#0B0C0C", marginBottom: "10px" }}>{title}</div>
      <ul style={{ margin: 0, paddingLeft: "20px" }}>
        {responses.map((response, index) => (
          <li key={index} style={{ fontSize: "15px", color: "#0B0C0C", marginBottom: "6px" }}>
            {response.question} {response.answer}
          </li>
        ))}
      </ul>
    </div>
  );

  return (
    <PopUp>
      <div
        style={{
          backgroundColor: "white",
          position: "fixed",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -50%)",
          width: "650px",
          maxWidth: "95%",
          maxHeight: "90vh",
          overflowY: "auto",
          padding: "24px",
          borderRadius: "5px",
        }}
      >
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: "20px" }}>
          <div style={{ fontFamily: "Roboto", fontWeight: 700, fontSize: "24px", color: "#0B0C0C" }}>
            {facility.name} {t("PM_ASSESSMENT_DETAILS_TITLE_SUFFIX")}
          </div>
          <button
            type={"button"}
            style={{ cursor: "pointer", background: "transparent", border: "none", padding: "0" }}
            onClick={onClose}
          >
            <CustomCloseSvg fill={"transparent"} />
          </button>
        </div>

        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", columnGap: "40px" }}>
          {InfoItem(t("PM_ASSESSMENT_HF_TYPE"), facility.facilityType)}
          {InfoItem(t("PM_ASSESSMENT_DISTRICT_BLOCK"), `${facility.district} / ${facility.block}`)}
          {InfoItem(t("PM_ASSESSMENT_REMOTE_STATUS"), t(`PM_ASSESSMENT_FACILITY_STATUS_${facility.remoteStatus}`))}
          {InfoItem(t("PM_ASSESSMENT_PHONE_OUTCOME"), t(`PM_ASSESSMENT_OUTCOME_${phoneOutcome}`))}
          {InfoItem(t("PM_ASSESSMENT_ONSITE_STATUS"), t(`PM_ASSESSMENT_FACILITY_STATUS_${facility.onSiteStatus}`))}
          {InfoItem(t("PM_ASSESSMENT_FIELD_OUTCOME"), t(`PM_ASSESSMENT_OUTCOME_${fieldOutcome}`))}
          {InfoItem(t("PM_ASSESSMENT_OVERALL_STATUS"), t(`PM_ASSESSMENT_FACILITY_STATUS_${facility.result}`))}
        </div>

        {facility.result === "NOT_ELIGIBLE" && facility.notEligibleReason && (
          <div style={{ marginTop: "4px", marginBottom: "8px" }}>
            {InfoItem(t("PM_ASSESSMENT_NOT_ELIGIBLE_REASON"), facility.notEligibleReason)}
          </div>
        )}

        {!!remoteResponses.length && ResponseList(t("PM_ASSESSMENT_RESPONSES"), remoteResponses)}
        {!!siteResponses.length && ResponseList(t("PM_ASSESSMENT_SITE_RESPONSES"), siteResponses)}

        {!planCompleted && (
          <div style={{ display: "flex", gap: "10px", flexWrap: "wrap", marginTop: "24px" }}>
            <Button
              variation={"secondary"}
              label={t("PM_ASSESSMENT_ACTION_ASSIGN_ONSITE")}
              isDisabled={!canAssignOnSite}
              onButtonClick={onAssignOnSite}
              style={{
                border: "none",
                padding: "10px 20px",
                fontWeight: "bold",
                fontSize: "16px",
                color: "white",
                backgroundColor: canAssignOnSite ? "#0B4B66" : "#D6D5D4",
              }}
            />
            <Button
              variation={"secondary"}
              label={t("PM_ASSESSMENT_ACTION_MARK_ELIGIBLE")}
              isDisabled={facility.result === "ELIGIBLE"}
              onButtonClick={onMarkEligible}
              style={{
                border: "none",
                padding: "10px 20px",
                fontWeight: "bold",
                fontSize: "16px",
                color: "white",
                backgroundColor: facility.result === "ELIGIBLE" ? "#D6D5D4" : "#1B8354",
              }}
            />
            <Button
              variation={"secondary"}
              label={t("PM_ASSESSMENT_ACTION_MARK_NOT_ELIGIBLE")}
              isDisabled={facility.result === "NOT_ELIGIBLE"}
              onButtonClick={onMarkNotEligible}
              style={{
                border: "none",
                padding: "10px 20px",
                fontWeight: "bold",
                fontSize: "16px",
                color: "white",
                backgroundColor: facility.result === "NOT_ELIGIBLE" ? "#D6D5D4" : "#B91900",
              }}
            />
          </div>
        )}
      </div>
    </PopUp>
  );
};

export default FacilityDetailsModal;
