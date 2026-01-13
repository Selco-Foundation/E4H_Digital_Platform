import React, {useEffect, useMemo} from "react";
import {useTranslation} from "react-i18next";
import {Route, Switch, useRouteMatch} from "react-router-dom";
import {BreadCrumb} from "@egovernments/digit-ui-components";
import {useSelector} from "react-redux";
import FacilityTable from "./FacilityTable";
import FacilityDetails from "./FacilityDetails";
import BoundaryTable from "./BoundaryTable";
import CreateBoundary from "./CreateBoundary";
import UploadBoundary from "./UploadBoundary";
import Response from "@selco/digit-ui-module-pm/src/pages/employee/Response";

const FAApp = () => {
  const {t} = useTranslation();
  const {path} = useRouteMatch();
  const match = useRouteMatch();
  const navigator = useSelector((state) => state.fa.common);

  const breadCrumbsConfig = {
    home: {
      content: t("CS_COMMON_HOME"),
      internalLink: `/${window.contextPath}/employee`,
      show: true,
    },
    facilities: {
      content: t("CS_COMMON_FACILITIES"),
      internalLink: `/${window.contextPath}/employee/fa/facilities`,
      show: true,
    },
    facilityDetails: {
      content: navigator.workingFacility?.facilityName || "",
      internalLink: `/${window.contextPath}/employee/fa/facilities/:facilityId`,
      show: true,
    },
    boundaries: {
      content: t("FA_LABEL_BOUNDARIES"),
      internalLink: match.url + `/boundaries`,
      show: true,
    },
    boundaryCreation: {
      content: t("FA_ACTION_CREATE_BOUNDARY"),
      internalLink: match.url + `/boundary/create`,
      show: true,
    },
    uploadBoundaryData: {
      content: t("FA_ACTION_UPLOAD_BOUNDARY"),
      internalLink: match.url + `/boundary/upload`,
      show: true,
    },
    response: {
      content: t("CORE_COMMON_RESPONSE"),
      internalLink: match.url + `/response`,
      show: true,
    },
  };

  useEffect(() => {
    Digit.UserService.setType("employee");
  }, []);

  return (
    <div className="ground-container">
      <Switch>
        <Route path={`${path}/facilities`} exact={true}>
          <BreadCrumb
            spanStyle={{color: "#0B0C0C"}}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.facilities]}
          />
          <FacilityTable/>
        </Route>
        <Route path={`${path}/facilities/:facilityId`} exact={true}>
          <BreadCrumb
            spanStyle={{color: "#0B0C0C"}}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.facilities, breadCrumbsConfig.facilityDetails]}
          />
          <FacilityDetails/>
        </Route>
        <Route path={`${path}/boundaries`} exact={true}>
          <BreadCrumb
            spanStyle={{color: "#0B0C0C"}}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.boundaries]}
          />
          <BoundaryTable/>
        </Route>
        <Route path={`${path}/boundary/create`} exact={true}>
          <BreadCrumb
            spanStyle={{color: "#0B0C0C"}}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.boundaryCreation]}
          />
          <CreateBoundary/>
        </Route>
        <Route path={`${path}/boundary/upload`} exact={true}>
          <BreadCrumb
            spanStyle={{color: "#0B0C0C"}}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.boundaries, breadCrumbsConfig.uploadBoundaryData]}/>
          <UploadBoundary/>
        </Route>
        <Route path={`${path}/response`} exact={true}>
          <BreadCrumb
            spanStyle={{color: "#0B0C0C"}}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.response]}
          />
          <Response/>
        </Route>
      </Switch>
    </div>
  );
};

export default FAApp;
