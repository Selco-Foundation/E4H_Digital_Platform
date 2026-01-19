import React from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import CustomMenuIcon from "./Custom/CustomMenuIcon";
import { ListAlt, AdminPanelSettings } from "@egovernments/digit-ui-svg-components";

const ORGCard = () => {
  const history = useHistory();
  const { t } = useTranslation();
  const { info } = Digit.UserService.getUser();
  const currentUserRoles = info?.roles?.map(role => role.code);

  // if (!currentUserRoles?.includes("FACILITY_ADMIN")) {
  //   return null;
  // }

  const userType = "employee";

  const handleManageFacilitiesRedirection = () => {
    history.push(`/#`);
  };

  const handleManageBoundariesRedirection = () => {
    history.push(`/#`);
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
        <AdminPanelSettings height={"28px"} width={"28px"} />
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
          {t("ORG_CARD_HEADING")}
        </div>
      </div>
      <div>
        <button
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
          onClick={handleManageFacilitiesRedirection}
        >
          <ListAlt />
          <span>{t("ORG_ACTION_MANAGE_VENDOR_ORGANIZATION")}</span>
        </button>
        <button
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
          onClick={handleManageBoundariesRedirection}
        >
          <ListAlt />
          <span>{t("ORG_ACTION_MANAGE_PLATFORM_ORGANIZATION")}</span>
        </button>
      </div>
    </div>
  );
}

export default ORGCard;