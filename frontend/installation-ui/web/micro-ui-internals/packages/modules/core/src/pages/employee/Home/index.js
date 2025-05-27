import { Loader } from "@egovernments/digit-ui-components";
import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { MenuIcon, ArrowRight } from "../../../../../../react-components/src/atoms/svgindex";

const HomePage = ({ stateCode, userType, cityDetails }) => {
  const history = useHistory();
  const { t } = useTranslation();
  const windowWidth = window.innerWidth;

  const handleClick = () => {
    history.push(`/${window?.contextPath}/employee/user/table`);
  };

  return (
    <div className={`user-profile ${userType === "citizen" ? "citizen" : "employee"}`}>
      <div
        style={{
          display: "flex",
          flex: 1,
          flexDirection: windowWidth < 768 || userType === "citizen" ? "column" : "row",
          margin: userType === "citizen" ? "8px" : "0px",
          gap: userType === "citizen" ? "" : "0 24px",
          boxShadow: userType === "citizen" ? "1px 1px 4px 0px rgba(0,0,0,0.2)" : "",
          background: userType === "citizen" ? "white" : "",
          borderRadius: userType === "citizen" ? "4px" : "",
          width: userType === "citizen" ? "960px" : "",
        }}
      >
        <section
          style={{
            position: "relative",
            // flex: userType === "citizen" ? 1 : 2.5,
            // justifyContent: "center",
            width: "40%",
            maxWidth: "400px",
            height: "200px",
            // height: "376px",
            borderRadius: "4px",
            boxShadow: userType === "citizen" ? "" : "1px 1px 4px 0px rgba(0,0,0,0.2)",
            background: "white",
            // background: "#EEEEEE",
            padding: userType === "citizen" ? "8px" : "16px",
          }}
        >
          {/* <MenuIcon */}
          <div style={{ marginBottom: "10px", padding: "8px", paddingLeft: 0, display: "flex", gap: "16px", alignItems: "center" }}>
            <MenuIcon color="#B91900" />
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
              Inbox
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
            View new activities that have been assigned to you, assign activities among your team and review their work.
          </div>
          <div
            style={{
              width: 116,
              height: 32,
              display: "flex",
              gap: "8px",
              justifyContent: "center",
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
            View <ArrowRight color={"white"} />
          </div>
        </section>
      </div>
    </div>
  );
};

export default HomePage;
