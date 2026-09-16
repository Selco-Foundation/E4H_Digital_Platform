import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Route, Switch, useLocation, useRouteMatch } from "react-router-dom";
import VisitTable from "./VisitTable";
import VisitDetails from "./VisitDetails";
import { BreadCrumb } from "@egovernments/digit-ui-components";
import { useSelector } from "react-redux";
import ProjectTable from "./ProjectTable";
import ReportLevelView from "./ReportLevelView";

const AMCApp = () => {
  const { t } = useTranslation();
  const { path } = useRouteMatch();
  const match = useRouteMatch();
  const location = useLocation();
  const navigator = useSelector((state) => state.amc.common);

  const breadcrumbConfig = {
    home: {
      content: t("CS_COMMON_HOME"),
      internalLink: `/${window.contextPath}/employee`,
      show: true,
    },
    inbox: {
      content: t("CS_COMMON_INBOX"),
      internalLink: match.url + `/inbox`,
      show: true,
    },
    reportLevelView: {
      content: t("AMC_REPORT_LEVEL_VIEW"),
      // Preserve report filters and pagination when returning from a report detail route.
      internalLink: match.url + `/reports${location.search || ""}`,
      show: true,
    },
    amcVisits: {
      content: navigator.workingProject?.name,
      internalLink: match.url + `/project/${navigator.workingProject?.id}/amc-visits`,
      show: true,
    },
    visitDetails: {
      content: navigator.workingVisit?.facilityDetails?.facilityName,
      internalLink: match.url + `/project/${navigator.workingProject?.id}/amc-visits/${navigator.workingVisit?.id}`,
      show: true,
    },
    reportVisitDetails: {
      content: navigator.workingVisit?.facilityDetails?.facilityName,
      internalLink: match.url + `/reports/${navigator.workingVisit?.id}`,
      show: true,
    },
  };

  useEffect(() => {
    Digit.UserService.setType("employee");
  }, []);

  return (
    <div className="ground-container">
      <Switch>
        <Route path={`${path}/inbox`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadcrumbConfig.home, breadcrumbConfig.inbox]}
          />
          <ProjectTable t={t} />
        </Route>
        <Route path={`${path}/reports`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadcrumbConfig.home, breadcrumbConfig.reportLevelView]}
          />
          {/* Report-level view is a separate reviewer route, not part of Inbox. */}
          <ReportLevelView t={t} />
        </Route>
        <Route path={`${path}/reports/:visitId`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadcrumbConfig.home, breadcrumbConfig.reportLevelView, breadcrumbConfig.reportVisitDetails]}
          />
          {/* Reuse the same detail page for report-level navigation. */}
          <VisitDetails t={t}/>
        </Route>
        <Route path={`${path}/project/:projectId/amc-visits`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadcrumbConfig.home, breadcrumbConfig.inbox, breadcrumbConfig.amcVisits]}
          />
          <VisitTable t={t} />
        </Route>
        <Route path={`${path}/project/:projectId/amc-visits/:visitId`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadcrumbConfig.home, breadcrumbConfig.inbox, breadcrumbConfig.amcVisits, breadcrumbConfig.visitDetails]}
          />
          <VisitDetails t={t}/>
        </Route>
      </Switch>
    </div>
  );
};

export default AMCApp;
