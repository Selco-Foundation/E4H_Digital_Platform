import React, {useEffect, useMemo} from "react";
import {useTranslation} from "react-i18next";
import {Route, Switch, useRouteMatch} from "react-router-dom";
import {BreadCrumb} from "@egovernments/digit-ui-components";
import {useSelector} from "react-redux";
import BoundaryTable from "./BoundaryTable";
import CreateBoundary from "./CreateBoundary";
import UploadBoundary from "./UploadBoundary";

const FAApp = () => {
  const {t} = useTranslation();
  const {path} = useRouteMatch();
  const match = useRouteMatch();
  const navigator = useSelector((state) => state.fa.common);

  const breadcrumbConfig = useMemo(() => ({
      home: {
        content: t("CS_COMMON_HOME"),
        internalLink: `/${window.contextPath}/employee`,
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
    }),
  );

  useEffect(() => {
    Digit.UserService.setType("employee");
  }, []);

  return (
    <div className="ground-container">
      <Switch>
        <Route path={`${path}/boundaries`} exact={true}>
          <BreadCrumb
            spanStyle={{color: "#0B0C0C"}}
            crumbs={[breadcrumbConfig.home, breadcrumbConfig.boundaries]}
          />
          <BoundaryTable/>
        </Route>
        <Route path={`${path}/boundary/create`} exact={true}>
          <BreadCrumb
            spanStyle={{color: "#0B0C0C"}}
            crumbs={[breadcrumbConfig.home, breadcrumbConfig.boundaryCreation]}
          />
          <CreateBoundary/>
        </Route>
        <Route path={`${path}/boundary/upload`} exact={true}>
          <BreadCrumb
            spanStyle={{color: "#0B0C0C"}}
            crumbs={[breadcrumbConfig.home, breadcrumbConfig.boundaries, breadcrumbConfig.uploadBoundaryData]}/>
          <UploadBoundary/>
        </Route>
      </Switch>
    </div>
  );
};

export default FAApp;
