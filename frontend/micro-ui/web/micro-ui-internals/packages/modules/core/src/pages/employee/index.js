import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Redirect, Route, Switch, useLocation, useRouteMatch, useHistory } from "react-router-dom";
import { AppModules } from "../../components/AppModules";
import ErrorBoundary from "../../components/ErrorBoundaries";
import TopBarSideBar from "../../components/TopBarSideBar";
import ChangePassword from "./ChangePassword";
import ForgotPassword from "./ForgotPassword";
import LanguageSelection from "./LanguageSelection";
import EmployeeLogin from "./Login";
import UserProfile from "../citizen/Home/UserProfile";
import ErrorComponent from "../../components/ErrorComponent";
import { PrivateRoute } from "@selco/digit-ui-react-components";

const userScreensExempted = ["user/profile", "user/error"];

const EmployeeApp = ({
  stateInfo,
  userDetails,
  CITIZEN,
  cityDetails,
  mobileView,
  handleUserDropdownSelection,
  logoUrl,
  DSO,
  stateCode,
  modules,
  appTenants,
  sourceUrl,
  pathname,
  initData,
}) => {
  const history = useHistory();
  const { t } = useTranslation();
  const { path } = useRouteMatch();
  const location = useLocation();
  const showLanguageChange = location?.pathname?.includes("language-selection");
  const isUserProfile = userScreensExempted.some((url) => location?.pathname?.includes(url));
  const bgImageUrl = window?.globalConfigs?.getConfig("BG_IMAGE");
  useEffect(() => {
    Digit.UserService.setType("employee");
  }, []);
  const isMobile = window.Digit.Utils.browser.isMobile();
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
              showSidebar={isUserProfile ? true : false}
              showLanguageChange={!showLanguageChange}
            />
          )}
          <div
            className={isUserProfile ? "grounded-container" : "loginContainer"}
            style={{
              '--bg-image-url': `url(${bgImageUrl})`,
              ...(isUserProfile
                ? !isMobile
                  ? { backgroundColor: "#225670", marginLeft: "40px", paddingTop: "100px" }
                  : { backgroundColor: "#225670", paddingTop: "100px" }
                : { backgroundColor: "#225670" }
              )
            }}
          >
            <Switch>
              <Route path={`${path}/user/login`}>
                <EmployeeLogin />
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
                    history.push(`/${window.contextPath}/employee`);
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
          <TopBarSideBar
            t={t}
            stateInfo={stateInfo}
            userDetails={userDetails}
            CITIZEN={CITIZEN}
            cityDetails={cityDetails}
            mobileView={mobileView}
            handleUserDropdownSelection={handleUserDropdownSelection}
            logoUrl={logoUrl}
            modules={modules}
          />
          <div className={`main ${DSO ? "m-auto" : ""}`}>
            <div className="employee-app-wrapper">
              <ErrorBoundary initData={initData}>
                <AppModules stateCode={stateCode} userType="employee" modules={modules} appTenants={appTenants} />
              </ErrorBoundary>
            </div>

            <div
              style={
                window.location.href.includes("/im/inbox")
                  ? { display: "flex", justifyContent: "center", marginBottom: "15px", marginTop: "10px" }
                  : { display: "flex", justifyContent: "center", marginBottom: "58px", marginTop: "-26px" }
              }
            >
              <img
                className="bannerLogo"
                src={window?.globalConfigs?.getConfig("STATE_NHM_LOGO")}
                alt="Selco Foundation"
                style={{ height: "3rem", width: "3rem", cursor: "pointer", marginRight: "15px", marginLeft: "15px" }}
              />
              <img
                className="bannerLogo"
                src={window?.globalConfigs?.getConfig("STATE_GOVT_LOGO")}
                alt="Selco Foundation"
                style={{ height: "3rem", width: "3rem", cursor: "pointer", marginRight: "15px" }}
              />
              <img
                className="bannerLogo"
                src={window?.globalConfigs?.getConfig("SELCO_LOGO")}
                alt="Selco Foundation"
                style={{ height: "3rem", cursor: "pointer", width: "3rem", marginRight: "15px" }}
              />
            </div>
            <div
              className="employee-home-footer"
              style={
                window.location.href.includes("/im/inbox")
                  ? { padding: "0px", height: "auto", marginBottom: "30px" }
                  : { padding: "0px", height: "auto", marginBottom: "73px", marginTop: "-43px" }
              }
            >
              <img
                alt="Powered by DIGIT"
                src={window?.globalConfigs?.getConfig?.("DIGIT_FOOTER")}
                style={{ height: "1.1rem", cursor: "pointer" }}
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
