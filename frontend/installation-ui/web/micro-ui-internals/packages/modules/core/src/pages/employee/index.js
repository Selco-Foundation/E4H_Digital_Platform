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
import SignUp from "./SignUp";
import Otp from "./Otp";
import ViewUrl from "./ViewUrl";
import UserProfile from "../citizen/Home/UserProfile";
import ErrorComponent from "../../components/ErrorComponent";
import ImageComponent from "../../components/ImageComponent";
import HomePage from "./Home";
import ComplaintTable from "./installation-centers";
import { Link } from "react-router-dom";

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
  // const isUserProfile = userScreensExempted.some((url) => location?.pathname?.includes(url));
  useEffect(() => {
    Digit.UserService.setType("employee");
  }, []);

  const GetCell = (value) => <span className="cell-text">{value}</span>;

  const GetProgress = (value) => {
    return (
      <div style={{ display: "flex", gap: `${value > 99 ? "10px" : "20px"}` }}>
        <div>{value}%</div>
        <div style={{ width: "100px", height: "20px", background: "#E0E0E0", borderRadius: "5px" }}>
          <div style={{ position: "absolute", height: "20px", width: `${value}px`, background: "#00703C", borderRadius: "5px" }}></div>
        </div>
      </div>
    );
  };

  const GetLink = (value) => (
    <Link to={`/${window.contextPath}/employee/im/complaint/details/${incidentId.toString()}/${tenantId}`} style={{ color: "#7a2829" }}>
      {value}
    </Link>
  );

  const columns = [
    {
      Header: "Field Plan Code",
      Cell: ({ row }) => {
        return (
          <div>
            <span className="link">
              <Link to={`/${window.contextPath}/employee/user/centres/${row.original["code"]}`} style={{ color: "#C84C0E" }}>
                {row.original["code"]}
              </Link>
            </span>
          </div>
        );
      },
    },
    {
      Header: "Activity Type",
      Cell: ({ row }) => {
        return GetCell(`${row.original["type"].toUpperCase()}`);
      },
    },
    {
      Header: "Health Facilities",
      Cell: ({ row }) => {
        return GetCell(`${row.original["facilities"].toUpperCase()}`);
      },
    },
    {
      Header: "Start Date",
      Cell: ({ row }) => {
        return GetCell(`${row.original["start"].toUpperCase()}`);
      },
    },
    {
      Header: "End Date",
      Cell: ({ row }) => {
        return GetCell(`${row.original["end"].toUpperCase()}`);
      },
    },
    {
      Header: "Completion Rate",
      Cell: ({ row }) => {
        return GetProgress(`${row.original["completion"]}`);
      },
    },
  ];

  const data = [
    { code: "MH-QC_HO-2024-200centres", type: "Installation", facilities: "200", start: "08/05/2025", end: "08/10/2025", completion: 40 },
    { code: "MH-QC_HO-2024-201centres", type: "Installation", facilities: "100", start: "08/03/2025", end: "22/05/2025", completion: 20 },
    { code: "MH-QC_HO-2024-202centres", type: "Installation", facilities: "400", start: "01/05/2024", end: "01/03/2025", completion: 100 },
  ];

  const additionalComponent = initData?.modules?.filter((i) => i?.additionalComponent)?.map((i) => i?.additionalComponent);

  return (
    <div className="employee">
      <Switch>
        <Route path={`${path}/user`}>
          {/* {isUserProfile && ( */}
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
          <div className={"grounded-container"} style={{ padding: 0, paddingTop: "0", marginLeft: mobileView ? "0" : "0" }}>
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
              <Route path={`${path}/user/home`}>
                <HomePage stateCode={stateCode} userType={"employee"} cityDetails={cityDetails} />
              </Route>
              <Route path={`${path}/user/table`}>
                <ComplaintTable
                  t={t}
                  columns={columns}
                  data={data}
                  getCellProps={(cellInfo) => {
                    return {
                      style: {
                        maxWidth: "100%",
                        padding: "17.24px 18px",
                        fontSize: "15px",
                      },
                    };
                  }}
                />
              </Route>
              <Route path={`${path}/user/centres/:id*`}>
                <HomePage stateCode={stateCode} userType={"employee"} cityDetails={cityDetails} />
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
          {/* {!noTopBar && ( */}
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
          {/* )} */}
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
