import React, {useEffect} from "react";
import { Redirect, Route, Switch, useLocation, useRouteMatch } from "react-router-dom";

import ChangePassword from "../pages/employee/ChangePassword/index";
import ForgotPassword from "../pages/employee/ForgotPassword/index";
import { AppHome } from "./Home";
import {useDispatch} from "react-redux";
import {addLanguageOptions, addStateLogos, setCrmHelplineNumber } from "../redux/actions";
// import UserProfile from "./userProfile";

const getTenants = (codes, tenants) => {
  return tenants.filter((tenant) => codes?.map?.((item) => item.code).includes(tenant.code));
};

export const AppModules = ({ stateCode, userType, modules, appTenants }) => {
  const ComponentProvider = Digit.Contexts.ComponentProvider;
  const { path } = useRouteMatch();
  const location = useLocation();
  const jurisdictionBoundaries = Digit.SessionStorage.get("Jurisdiction.Boundaries");
  const dispatch = useDispatch();

  const { data: boundaryData } = Digit.Hooks.im.useBoundary(jurisdictionBoundaries?.codes || []);
  const { data: boundaryLanguageData } = Digit.Hooks.pgr.useMDMS(stateCode, "common-masters", ["BoundaryLanguage"]);

  useEffect(() => {
    const initData = Digit.SessionStorage.get("initData");
    if (!initData || !Array.isArray(initData.languages)) return;

    if (boundaryData && boundaryLanguageData) {
      const stateCodes = (boundaryData.states || []).map((state) => state.code);
      const stateBoundaryInfos = window?.globalConfigs?.getStateBoundaryInfos?.(stateCodes);
      if (stateBoundaryInfos?.length === 1) {
        dispatch(addStateLogos(stateBoundaryInfos[0].logos || []))
        dispatch(setCrmHelplineNumber(stateBoundaryInfos[0].crmHelplineNumber || ""))
      }
      const boundaryLanguages = [];
      if (stateBoundaryInfos) {
        for (let stateBoundaryInfo of stateBoundaryInfos) {
          boundaryLanguages.push(...(stateBoundaryInfo.languages || []));
        }
      }
      const existingLanguageValues = initData.languages?.map(language => language?.value) || [];
      const filteredBoundaryLanguages = boundaryLanguages.filter((boundaryLanguage) => !existingLanguageValues.includes(boundaryLanguage?.value));
      initData.languages = [...initData.languages, ...filteredBoundaryLanguages];
      Digit.SessionStorage.set("initData", initData);
    }

    dispatch(addLanguageOptions(initData.languages));
  }, [boundaryData, boundaryLanguageData]);

  const appRoutes = modules.map(({ code, tenants }, index) => {
    const Module = Digit.ComponentRegistryService.getComponent(`${code}Module`);
    return Module ? (
      <Route key={index} path={`${path}/${code.toLowerCase()}`}>
        <Module stateCode={stateCode} moduleCode={code} userType={userType} tenants={getTenants(tenants, appTenants)} />
      </Route>
    ) :   <Route key={index} path={`${path}/${code.toLowerCase()}`}>
    <Redirect to={{ pathname: `/${window.contextPath}/employee/user/error?type=notfound`, state: { from: location.pathname + location.search } }} />
  </Route>;
  });

  return (
    <div className="ground-container">
      <Switch>
        {appRoutes}
        <Route path={`${path}/login`}>
          <Redirect to={{ pathname: `/${window.contextPath}/employee/user/login`, state: { from: location.pathname + location.search } }} />
        </Route>
        <Route path={`${path}/forgot-password`}>
          <ForgotPassword />
        </Route>
        <Route path={`${path}/change-password`}>
          <ChangePassword />
        </Route>
        <Route>
          <AppHome userType={userType} modules={modules}/>
        </Route>
        {/* <Route path={`${path}/user-profile`}> <UserProfile /></Route> */}
      </Switch>
    </div>
  );
};
