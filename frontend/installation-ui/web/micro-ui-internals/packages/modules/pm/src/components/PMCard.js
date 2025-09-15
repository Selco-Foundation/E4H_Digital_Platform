import React, {useState} from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { AdminPanelSettings } from "@egovernments/digit-ui-svg-components";
import { AddExpense, ListAlt } from "@egovernments/digit-ui-svg-components";
import IntroModal from "./IntroModal"

const PMCard = () => {
  const [showIntro, setShowIntro] = useState(false);
  const history = useHistory();
  const { t } = useTranslation();
  const { info } = Digit.UserService.getUser();
  const currentUserRoles = info?.roles?.map(role => role.code);

  if(!currentUserRoles?.includes("PROJECT_MANAGER")) {
    return null;
  }

  const userType = "employee";

  const handleCreateProjectRedirection = () => {
    history.push(`/${window?.contextPath}/employee/pm/project/create`)
  };

  const handleViewProjectRedirection = () => {
      history.push(`/${window?.contextPath}/employee/pm/projects`)
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
          padding: "8px 0px 25px 0px",
          display: "flex",
          gap: "10px",
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
          {t("PM_COMMON_PROJECTS")}
        </div>
      </div>
      <div>
        <div
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
          }}
          onClick={() => setShowIntro(true)}
        >
          <AddExpense />
          <span>
            {t("QC_ACTION_CREATE_PROJECT")}
          </span>
        </div>
        <div
          style={{
            display: "flex",
            gap: "8px",
            alignItems: "center",
            color: "#C84C0E",
            cursor: "pointer",
            fontSize: "16px",
            fontWeight: "500",
            fontFamily: "Roboto",
          }}
          onClick={handleViewProjectRedirection}
        >
          <ListAlt />
          <span>
            {t("QC_ACTION_VIEW_PROJECTS")}
          </span>
        </div>
      </div>

      <IntroModal open={showIntro} onClose={() => setShowIntro(false)} t={t} action={handleCreateProjectRedirection} />
    </div>
  );
}

export default PMCard;
