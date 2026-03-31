import React from "react";
import { useTranslation } from "react-i18next";
import ImageComponent from "./ImageComponent";

const Header = ({ showTenant = true, loginHeader }) => {
  const { t } = useTranslation();

  return (
    <div className="bannerHeader">
      <ImageComponent className="bannerLogo" src={loginHeader?.logo} style={!showTenant ? { borderRight: "unset" } : {}} alt="Digit Banner" />
      {showTenant && loginHeader?.title && <p>{t(loginHeader.title)}</p>}
    </div>
  );
};

export default Header;
