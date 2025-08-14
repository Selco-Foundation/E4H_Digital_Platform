import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Redirect, Route, Switch, useLocation, useRouteMatch, useHistory } from "react-router-dom";
import { AppModules } from "../../components/AppModules";
import ErrorBoundary from "../../components/ErrorBoundaries";
import TopBarSideBar from "../../components/TopBarSideBar";
import ChangePassword from "./ChangePassword";
import ForgotPassword from "./ForgotPassword";
import LanguageSelection from "./LanguageSelection";
import EmployeeLogin from "./Login";
import Otp from "./Otp";
import UserProfile from "../citizen/Home/UserProfile";
import ErrorComponent from "../../components/ErrorComponent";
import ImageComponent from "../../components/ImageComponent";

const EmployeeApp = ({
  stateInfo,
  userDetails,
  CITIZEN,
  cityDetails,
  mobileView,
  handleUserDropdownSelection,
  logoUrl,
  logoUrlWhite,
  DSO,
  stateCode,
  modules,
  appTenants,
  sourceUrl,
  pathname,
  initData,
  noTopBar = false,
}) => {
  const history = useHistory();
  const { t } = useTranslation();
  const { path } = useRouteMatch();
  const location = useLocation();
  const showLanguageChange = location?.pathname?.includes("language-selection");
  const userScreensExempted = ["user/profile", "user/error"];
  const isUserProfile = userScreensExempted.some((url) => location?.pathname?.includes(url));
  const bgImageUrl = window?.globalConfigs?.getConfig("BG_IMAGE");

  useEffect(() => {
    Digit.UserService.setType("employee");
  }, []);

  const additionalComponent = initData?.modules?.filter((i) => i?.additionalComponent)?.map((i) => i?.additionalComponent);

  return (
    <div className="employee">
      <Switch>
        <Route path={`${path}/user`}>
          {isUserProfile && (
            <TopBarSideBar
              t={t}
              stateInfo={stateInfo}
              userDetails={userDetails}
              CITIZEN={CITIZEN}
              cityDetails={cityDetails}
              mobileView={mobileView}
              handleUserDropdownSelection={handleUserDropdownSelection}
              logoUrl={logoUrl}
              logoUrlWhite={logoUrlWhite}
              showSidebar={true}
              showLanguageChange={!showLanguageChange}
            />
          )}
          <div
              className={isUserProfile ? "grounded-container" : "loginContainer"}
              style={{
                '--bg-image-url': `url(${bgImageUrl})`,
                ...(isUserProfile
                        ? !window.Digit.Utils.browser.isMobile()
                            ? { backgroundColor: "#225670", marginLeft: "40px", paddingTop: "100px" }
                            : { backgroundColor: "#225670", paddingTop: "100px" }
                        : { backgroundColor: "#225670" }
                )
              }}
          >
            <Switch>
              <Route exact path={`${path}/user/login`}>
                <EmployeeLogin stateCode={stateCode} />
              </Route>
              <Route exact path={`${path}/user/login/otp`}>
                <Otp isLogin={true} />
              </Route>
              <Route path={`${path}/user/forgot-password`}>
                <ForgotPassword />
              </Route>
              <Route path={`${path}/user/change-password`}>
                <ChangePassword />
              </Route>
              <Route path={`${path}/user/profile`}>
                <UserProfile stateCode={stateCode} userType={"employee"} cityDetails={cityDetails} />
              </Route>
              <Route path={`${path}/user/error`}>
                <ErrorComponent
                  initData={initData}
                  goToHome={() => {
                    history.push(`/${window?.contextPath}/${Digit?.UserService?.getType?.()}`);
                  }}
                />
              </Route>
              <Route path={`${path}/user/language-selection`}>
                <LanguageSelection />
              </Route>
              <Route>
                <Redirect to={`${path}/user/language-selection`} />
              </Route>
            </Switch>
          </div>
        </Route>
        <Route>
           {!noTopBar && (
          <TopBarSideBar
            t={t}
            stateInfo={stateInfo}
            userDetails={userDetails}
            CITIZEN={CITIZEN}
            cityDetails={cityDetails}
            mobileView={mobileView}
            handleUserDropdownSelection={handleUserDropdownSelection}
            logoUrl={logoUrl}
            logoUrlWhite={logoUrlWhite}
            modules={modules}
          />
           )}
          <div className={!noTopBar ? `main ${DSO ? "m-auto" : ""} digit-home-main` : ""}>
            <div className="employee-app-wrapper digit-home-app-wrapper">
              <ErrorBoundary initData={initData}>
                <AppModules
                  stateCode={stateCode}
                  userType="employee"
                  modules={modules}
                  appTenants={appTenants}
                  additionalComponent={additionalComponent}
                />
              </ErrorBoundary>
            </div>
            <div className="employee-home-footer">
              <ImageComponent
                alt="Powered by DIGIT"
                src={window?.globalConfigs?.getConfig?.("DIGIT_FOOTER")}
                style={{ height: "1.1em", cursor: "pointer" }}
                onClick={() => {
                  window.open(window?.globalConfigs?.getConfig?.("DIGIT_HOME_URL"), "_blank").focus();
                }}
              />
            </div>
          </div>
        </Route>
        <Route>
          <Redirect to={`${path}/user/language-selection`} />
        </Route>
      </Switch>
    </div>
  );
};

export default EmployeeApp;
