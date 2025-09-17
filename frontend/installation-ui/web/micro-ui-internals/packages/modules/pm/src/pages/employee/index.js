import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Route, Switch, useRouteMatch } from "react-router-dom";
import { BreadCrumb } from "@egovernments/digit-ui-components";
import { useSelector } from "react-redux";
import CreateProject from "./CreateProject";
import Response from "./Response";
import CreateFieldPlan from "./CreateFieldPlan";
import ProjectDetails from "./ProjectDetails";
import ProjectTable from "./ProjectTable";

const PMApp = () => {
  const { t } = useTranslation();
  const { path } = useRouteMatch();
  const match = useRouteMatch();

  const breadCrumbsConfig = {
    home: {
      content: t("CS_COMMON_HOME"),
      internalLink: `/${window.contextPath}/employee`,
      show: true,
    },
    projectCreation: {
      content: t("PM_ACTION_CREATE_PROJECT"),
      internalLink: match.url + `/project/create`,
      show: true,
    },
    response: {
      content: t("CORE_COMMON_RESPONSE"),
      internalLink: match.url + `/response`,
      show: true,
    },
    fieldPlanCreation: {
      content: t("PM_ACTION_CREATE_FIELD_PLAN"),
      internalLink: match.url + `/project/1234567890/field-plan/create`,
      show: true,
    },
    projectDetails: {
      content: t("PM_PROJECT_PROJECT_DETAILS"),
      internalLink: match.url + `/project/1234567890/details`,
      show: true,
    },
    project: {
      content: t("PM_LABEL_MY_PROJECTS"),
      internalLink: match.url + `/projects`,
      show: true,
    }
  };

  useEffect(() => {
    Digit.UserService.setType("employee");
  }, []);

  return (
    <div className="ground-container">
      <Switch>
        <Route path={`${path}/project/create`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.projectCreation]}
          />
          <CreateProject />
        </Route>
        <Route path={`${path}/project/:projectId/details`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.projectDetails]}
          />
          <ProjectDetails />
        </Route>
        <Route path={`${path}/project/:projectId/field-plan/create`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.fieldPlanCreation]}
          />
          <CreateFieldPlan />
        </Route>
        <Route path={`${path}/response`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.response]}
          />
          <Response />
        </Route>
        <Route path={`${path}/projects`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.project]}
          />
          <ProjectTable />
        </Route>
      </Switch>
    </div>
  );
};

export default PMApp;
