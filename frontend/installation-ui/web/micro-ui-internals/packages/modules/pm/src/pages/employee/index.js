import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Route, Switch, useRouteMatch } from "react-router-dom";
import { BreadCrumb } from "@egovernments/digit-ui-react-components";
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
      path: `/${window.contextPath}/employee`,
      show: true,
    },
    projectCreation: {
      content: t("PM_ACTION_CREATE_PROJECT"),
      path: match.url + `/project/create`,
      show: true,
    },
    response: {
      content: t("PM_ACTION_CREATE_PROJECT"),
      path: match.url + `/response`,
      show: true,
    },
    fieldPlanCreation: {
      content: t("PM_ACTION_CREATE_FIELD_PLAN"),
      path: match.url + `/project/1234567890/field-plan/create`,
      show: true,
    },
    projectDetails: {
      content: t("PM_PROJECT_PROJECT_DETAILS"),
      path: match.url + `/project/1234567890/details`,
      show: true,
    },
    project: {
      content: t("PM_LABEL_MY_PROJECTS"),
      path: match.url + `/projects`,
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
          <BreadCrumb crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.projectCreation]} />
          <CreateProject />
        </Route>
        <Route path={`${path}/project/:projectId/details`} exact={true}>
          <BreadCrumb crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.projectDetails]} />
          <ProjectDetails />
        </Route>
        <Route path={`${path}/project/:projectId/field-plan/create`} exact={true}>
          <BreadCrumb crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.fieldPlanCreation]} />
          <CreateFieldPlan />
        </Route>
        <Route path={`${path}/response`} exact={true}>
          <BreadCrumb crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.response]} />
          <Response />
        </Route>
        <Route path={`${path}/projects`} exact={true}>
          <BreadCrumb crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.project]} />
          <ProjectTable />
        </Route>
      </Switch>
    </div>
  );
};

export default PMApp;
