import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Route, Switch, useRouteMatch } from "react-router-dom";
import { BreadCrumb } from "@egovernments/digit-ui-react-components";
import { useSelector } from "react-redux";
import CreateProject from "./CreateProject";
import Response from "./Response";

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
      content: t("PM_ACTION_RESPONSE"),
      path: match.url + `/response`,
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
        <Route path={`${path}/response`} exact={true}>
          <BreadCrumb crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.projectCreation]} />
          <Response />
        </Route>
      </Switch>
    </div>
  );
};

export default PMApp;
