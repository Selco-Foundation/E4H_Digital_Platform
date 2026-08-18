import React from "react";

const ActionButton = ({ label, enabled, color, onClick }) => (
  <button
    type="button"
    disabled={!enabled}
    onClick={onClick}
    style={{
      border: "none",
      padding: "10px 20px",
      fontWeight: "bold",
      fontSize: "16px",
      color: "white",
      borderRadius: "2px",
      cursor: enabled ? "pointer" : "default",
      backgroundColor: enabled ? color : "#D6D5D4",
    }}
  >
    {label}
  </button>
);

const ActionBar = ({ t, canAssignOnSite, canMarkEligible, canMarkNotEligible, onAssignOnSite, onMarkEligible, onMarkNotEligible }) => (
  <div
    style={{
      position: "fixed",
      bottom: 0,
      left: 0,
      right: 0,
      padding: "12px 50px",
      display: "flex",
      justifyContent: "flex-end",
      gap: "10px",
      backgroundColor: "#fff",
      boxShadow: "0px 0px 4px rgba(0, 0, 0, 0.25)",
      zIndex: 1,
    }}
  >
    <ActionButton label={t("PM_ASSESSMENT_ACTION_ASSIGN_ONSITE")} enabled={canAssignOnSite} color="#0B4B66" onClick={onAssignOnSite} />
    <ActionButton label={t("PM_ASSESSMENT_ACTION_MARK_ELIGIBLE")} enabled={canMarkEligible} color="#1B8354" onClick={onMarkEligible} />
    <ActionButton label={t("PM_ASSESSMENT_ACTION_MARK_NOT_ELIGIBLE")} enabled={canMarkNotEligible} color="#B91900" onClick={onMarkNotEligible} />
  </div>
);

export default ActionBar;
