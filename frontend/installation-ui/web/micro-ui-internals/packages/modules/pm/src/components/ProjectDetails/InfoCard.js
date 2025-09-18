import React, { useEffect, useState } from "react";
import { EditIcon } from "@egovernments/digit-ui-react-components";
import { useHistory } from "react-router-dom";

const InfoCard = ({ t, project }) => {

  const history = useHistory();
  const [mobileView, setMobileView] = useState(window.innerWidth <= 700);
  const { id, projectType, startDate, endDate, status, additionalDetails : { geographyDetails } } = project;

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 700);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const PropertyCard = (infoName, infoValue) => (
    <div style={{ width: "500px", maxWidth: "100%", display: "flex", gap: "20px", alignItems: "center", marginBottom: "15px" }}>
      <div style={{ fontFamily: "Roboto", fontWeight: 700, width: "30%", fontSize: "16px", lineHeight: "100%", color: "#0B0C0C" }}>
        {infoName}
      </div>
      <div style={{ fontFamily: "Roboto", fontWeight: 400, fontSize: "14px", lineHeight: "137%", color: "#0B0C0C" }}>
        {infoValue}
      </div>
    </div>
  )

  const formatDate = (timestamp) => {
    const date = new Date(timestamp);
    const month = date.toLocaleString("en-US", { month: "long" });
    const day = String(date.getDate()).padStart(2, "0");
    const year = date.getFullYear();
    return `${day} ${month} ${year}`;
  };

  const handleEditProjectNavigation = () => {
    history.push(`/${window.contextPath}/employee/pm/project/create?projectId=${id}&key=1`);
  }

  return (
    <div
      style={{
        width: "100%",
        background: "white",
        height: "fit-content",
        marginBottom: "15px",
        padding: "20px",
        boxShadow: "1px 1px 4px 0px rgba(0,0,0,0.2)",
        position: "relative",
      }}
    >
      <div
        style={{
          fontFamily: "Roboto",
          fontWeight: "700",
          fontSize: "24px",
          lineHeight: "100%",
          letterSpacing: "0px",
          color: "#0B4B66",
          marginBottom: "20px",
        }}
      >
        {t("PM_PROJECT_PROJECT_DETAILS")}
      </div>
      <button
        type="button"
        style={{
          position: "absolute",
          top: "20px",
          right: "20px",
          padding: "0px",
          backgroundColor: "white",
        }}
        onClick={handleEditProjectNavigation}
      >
        <EditIcon />
      </button>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          flexDirection: mobileView ? "column" : "row",
        }}
      >
        <div style={{width: mobileView ? "100%" : "40%"}}>
          {PropertyCard(t("PM_PROJECT_INFO_TYPE_OF_PROJECT"), projectType)}
          {PropertyCard(t("PM_PROJECT_INFO_PROJECT_STATUS"), t(`PM_PROJECT_STATUS_${status ? status.toUpperCase() : "DRAFT"}`))}
          {PropertyCard(t("PM_PROJECT_INFO_PROJECT_DATES"), `${formatDate(startDate)} - ${formatDate(endDate)}`)}
        </div>
        <div style={{width: mobileView ? "100%" : "40%"}}>
          {PropertyCard(t("PM_PROJECT_INFO_STATE"), t(`STATE_${geographyDetails.state.code.toUpperCase()}`))}
          {PropertyCard(
            t("PM_PROJECT_INFO_DISTRICTS"),
            <span style={{ color: "#C84C0E", textDecoration: "underline" }}>
              {`${geographyDetails.districts.length} ${t("CORE_COMMON_SELECTED")}`}
            </span>
          )}
          {PropertyCard(
            t("PM_PROJECT_INFO_BLOCKS"),
            <span style={{ color: "#C84C0E", textDecoration: "underline" }}>
              {`${geographyDetails.blocks.length} ${t("CORE_COMMON_SELECTED")}`}
            </span>
          )}
        </div>
      </div>
    </div>
  );
};

export default InfoCard;
