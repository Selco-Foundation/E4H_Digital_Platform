import React from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import CustomMenuIcon from "./Custom/CustomMenuIcon";
import CustomArrowRight from "./Custom/CustomArrowRight";

const PMCard = () => {
  const history = useHistory();
  const { t } = useTranslation();
  const windowWidth = window.innerWidth;

  const userType = "employee";

  const handleClick = () => {
    console.log("clicked");
  };

  return (
    <div
      style={{
        marginLeft: "0px",
        margin: userType === "citizen" ? "8px" : "0px",
        gap: userType === "citizen" ? "" : "0 24px",
        boxShadow: userType === "citizen" ? "1px 1px 4px 0px rgba(0,0,0,0.2)" : "",
        background: userType === "citizen" ? "white" : "",
        borderRadius: userType === "citizen" ? "4px" : "",
        maxWidth: "fit-content",
      }}
    >
      <section
        style={{
          position: "relative",
          width: "95%",
          maxWidth: "400px",
          borderRadius: "4px",
          boxShadow: userType === "citizen" ? "" : "1px 1px 4px 0px rgba(0,0,0,0.2)",
          background: "white",
          padding: userType === "citizen" ? "8px" : "16px",
        }}
      >
        <div style={{ marginBottom: "10px", padding: "8px", paddingLeft: 0, display: "flex", gap: "16px", alignItems: "center" }}>
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
            {t("CS_COMMON_INBOX")}
          </div>
        </div>
        <div
          style={{
            marginBottom: "10px",
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
        <div
          style={{
            width: 116,
            height: 32,
            display: "flex",
            gap: "8px",
            justifyContent: "center",
            alignItems: "center",
            paddingTop: "8px",
            paddingRight: "20px",
            paddingBottom: "8px",
            paddingLeft: "20px",
            background: "#C84C0E",
            color: "white",
            cursor: "pointer",
          }}
          onClick={handleClick}
        >
          <span>
            {t("CORE_COMMON_VIEW")}
          </span>
          <CustomArrowRight color={"white"} height={"14px"} width={"14px"} />
        </div>
      </section>
    </div>
  );
}

export default PMCard;
