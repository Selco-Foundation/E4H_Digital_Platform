import { Card, CustomButton, SubmitBar } from "@selco/digit-ui-react-components";
import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { Redirect, useHistory } from "react-router-dom";
import Background from "../../../components/Background";

const LanguageSelection = () => {
  const { data: storeData, isLoading } = Digit.Hooks.useStore.getInitData();
  const { t } = useTranslation();
  const history = useHistory()
  const { languages, stateInfo } = storeData || {};
  const selectedLanguage = Digit.StoreData.getCurrentLanguage();
  const [selected, setselected] = useState(selectedLanguage);
  const handleChangeLanguage = (language) => {
    setselected(language.value);
    Digit.LocalizationService.changeLanguage(language.value, stateInfo.code);
  };
  const loginURL = `/${window.contextPath}/employee/user/login`

  const handleSubmit = (event) => {
    history.push(loginURL)
  };

  if(languages?.length === 1) {
    return <Redirect to={loginURL} />;
  }

  if (isLoading) return null;

  return (
    <Background>
      <style>
        {`
        @media screen and (max-width: 768px) {
            .banner .bannerCard,
            .loginFormStyleEmployee .employeeCard {
                min-width: 300px !important;
                margin: 10px !important;
            }
        }
        `}
      </style>
      <Card className="bannerCard removeBottomMargin">
        <div className="bannerHeader">
          <img
            className="bannerLogo"
            src={"https://selco-assets.s3.ap-south-1.amazonaws.com/TwoClr_horizontal_4X.png"}
            alt="Selco Foundation"
            style={{ width: "100px" }}
          />

          <p style={{ marginLeft: "-10px", paddingLeft: "10px" }}>{t(`HEADER_TENANT_TENANTS_${stateInfo?.code.toUpperCase()}`)}</p>
        </div>
        <div className="language-selector" style={{ justifyContent: "space-around", marginBottom: "24px", padding: "0 5%" }}>
          {languages.map((language, index) => (
            <div className="language-button-container" key={index}>
              <CustomButton
                selected={language.value === selected}
                text={language.label}
                onClick={() => handleChangeLanguage(language)}
              ></CustomButton>
            </div>
          ))}
        </div>
        <SubmitBar style={{ width: "100%" }} label={t(`CORE_COMMON_CONTINUE`)} onSubmit={handleSubmit} />
        <div style={{ display: "flex", justifyContent: "center", marginTop: "10px" }}>
          <img
            className="bannerLogo"
            src={window?.globalConfigs?.getConfig("STATE_NHM_LOGO")}
            alt="Selco Foundation"
            style={{ border: "0px", marginLeft: "15px" }}
          />
          <img
            className="bannerLogo"
            src={window?.globalConfigs?.getConfig("STATE_GOVT_LOGO")}
            alt="Selco Foundation"
            style={{ border: "0px" }}
          />
          <img
            className="bannerLogo"
            src={window?.globalConfigs?.getConfig("SELCO_LOGO")}
            alt="Selco Foundation"
            style={{ border: "0px" }}
          />
        </div>
      </Card>
      <div className="EmployeeLoginFooter">
        <img
          alt="Powered by DIGIT"
          src={window?.globalConfigs?.getConfig?.("DIGIT_FOOTER_BW")}
          style={{ cursor: "pointer" }}
          onClick={() => {
            window.open(window?.globalConfigs?.getConfig?.("DIGIT_HOME_URL"), "_blank").focus();
          }}
        />{" "}
      </div>
    </Background>
  );
};

export default LanguageSelection;
