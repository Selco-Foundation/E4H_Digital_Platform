import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Route, Switch, useRouteMatch } from "react-router-dom";
import { BreadCrumb } from "@egovernments/digit-ui-react-components";
import { useSelector } from "react-redux";
import CreateProject from "./CreateProject";

const PMApp = () => {
  const { t } = useTranslation();
  const { path } = useRouteMatch();
  const match = useRouteMatch();

  const breadCrumbsConfig = {};

  useEffect(() => {
    Digit.UserService.setType("employee");
  }, []);

  return (
    <div className="ground-container">
      <Switch>
          <Route path={`${path}/project/create`} exact={true}>
            <CreateProject />
          </Route>
      </Switch>
    </div>
  );
};

export default PMApp;
