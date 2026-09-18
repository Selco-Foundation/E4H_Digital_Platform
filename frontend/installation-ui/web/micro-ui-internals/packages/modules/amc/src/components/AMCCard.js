import React from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { ListAlt, RateReview } from "@egovernments/digit-ui-svg-components";
import CustomMenuIcon from "./Custom/CustomMenuIcon";

const AMCCard = () => {
  const history = useHistory();
  const { t } = useTranslation();
  const { info } = Digit.UserService.getUser();
  const currentUserRoles = info?.roles?.map(role => role.code);

  if(!currentUserRoles?.includes("AMC_REVIEWER")) {
    return null;
  }

  const userType = "employee";

  // Existing project hierarchy view.
  const handleProjectLevelView = () => {
    history.push(`/${window?.contextPath}/employee/amc/inbox`);
  };

  // New direct report listing view.
  const handleReportLevelView = () => {
    history.push(`/${window?.contextPath}/employee/amc/reports`);
  };

  return (
    <div
      style={{
        marginLeft: "0px",
        margin: userType === "citizen" ? "8px" : "0px",
        gap: userType === "citizen" ? "" : "0 24px",
        boxShadow: "1px 1px 4px 0px rgba(0,0,0,0.2)",
        backgroundColor: "white",
        borderRadius: "4px",
        width: "320px",
        maxWidth: "95%",
        minHeight: "297px",
        position: "relative",
        padding: "24px",
      }}
    >
      <div
        style={{
          marginBottom: "20px",
          padding: "8px 0px 27px 0px",
          display: "flex",
          gap: "16px",
          alignItems: "center",
          lineHeight: "35px",
          borderBottom: "1px solid #D6D5D4",
        }}
      >
        <CustomMenuIcon color="#B91900" />
        <div
          style={{
            fontFamily: "Roboto",
            fontWeight: "700",
            fontSize: "24px",
            lineHeight: "100%",
            letterSpacing: "0px",
            color: "#0B4B66",
            width: "70%",
          }}
        >
          {t("AMC_CARD_HEADING")}
        </div>
      </div>
      <div
        style={{
          marginBottom: "24px",
          fontFamily: "Roboto",
          fontWeight: 400,
          fontSize: "16px",
          lineHeight: "24px",
          letterSpacing: "0px",
          color: "#0B0C0C",
        }}
      >
        {t("CS_COMMON_HOME_INBOX_DESC")}
      </div>
      <button
        type="button"
        style={{
          display: "flex",
          gap: "8px",
          alignItems: "center",
          color: "#C84C0E",
          cursor: "pointer",
          marginBottom: "15px",
          fontSize: "16px",
          fontWeight: "500",
          fontFamily: "Roboto",
          background: "transparent",
          border: "none",
        }}
        onClick={handleProjectLevelView}
      >
        <ListAlt />
        <span>{t("PROJECT_VIEW")}</span>
      </button>
      <button
        type="button"
        style={{
          display: "flex",
          gap: "8px",
          alignItems: "center",
          color: "#C84C0E",
          cursor: "pointer",
          fontSize: "16px",
          fontWeight: "500",
          fontFamily: "Roboto",
          background: "transparent",
          border: "none",
        }}
        onClick={handleReportLevelView}
      >
        <RateReview />
        <span>{t("REPORT_VIEW")}</span>
      </button>
    </div>
  );
}

export default AMCCard;
