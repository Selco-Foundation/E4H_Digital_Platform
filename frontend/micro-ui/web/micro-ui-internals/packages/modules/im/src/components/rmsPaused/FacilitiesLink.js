import { Card } from "@selco/digit-ui-react-components";
import React from "react";
import { Link } from "react-router-dom";
import {useTranslation} from "react-i18next";

const RMSFacilitiesLink = ({ isMobile }) => {
  const { t } = useTranslation();

  const GetLogo = () => (
    <div className="header">
      <span className="logo">
        <svg xmlns="http://www.w3.org/2000/svg" height="24" viewBox="0 0 24 24" width="24">
          <path d="M0 0h24v24H0z" style={{ fill: "#7a2829" }} />
          <path d="M19 6h-1V4a2 2 0 0 0-2-2H8a2 2 0 0 0-2 2v2H5a2 2 0 0 0-2 2v12h18V8a2 2 0 0 0-2-2zm-9-2h4v2h-4V4zm9 14H5V8h14v10z" style={{ fill: "white" }} />
        </svg>
      </span>
      <span className="text">{t("FACILITIES")}</span>
    </div>
  );

  return (
    <Card className="employeeCard filter inboxLinks" style={{ width: "270px !important" }}>
      <div className="complaint-links-container" style={{ padding: "16px", height: "115px", margin: "auto", width: !isMobile ? "250px" : "" }}>
        <style>
          {`
          .complaint-links-container .header .logo {
              width: 56px;
              height: 56px;
              --bg-opacity: 1;
              background-color: #7a2829;
              padding: 12px;
              border-radius: 4px;
          }
          .complaint-links-container .body .link {
             padding-left: 0px;
          }
          .complaint-links-container .body {
            padding-left: 0px;
            margin-left: 0px;
          `}
        </style>
        {GetLogo()}
        <div className="body">
          <span className="link">
            <Link to={`/${window.contextPath}/employee/im/pause-rms`} style={{ color: "#7a2829" }}>
              {t("PAUSE_RMS")}
            </Link>
          </span>
        </div>
      </div>
    </Card>
  );
};

export default RMSFacilitiesLink;
