import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Route, Switch, useRouteMatch } from "react-router-dom";
import { BreadCrumb } from "@egovernments/digit-ui-components";
import { useSelector } from "react-redux";
import OrganizationTable from "./OrganizationTable";

const ORGApp = () => {
  const { t } = useTranslation();
  const { path } = useRouteMatch();
  const match = useRouteMatch();
  const navigator = useSelector((state) => state.org.common);

  const breadCrumbsConfig = {
    home: {
      content: t("CS_COMMON_HOME"),
      internalLink: `/${window.contextPath}/employee`,
      show: true,
    },
    vendorOrganizations: {
      content: t("CS_COMMON_ORGANIZATION"),
      internalLink: `/${window.contextPath}/employee/org/vendors`,
      show: true,
    },

  };

  useEffect(() => {
    Digit.UserService.setType("employee");
  }, []);

  return (
    <div className="ground-container">
      <Switch>
        <Route path={`${path}/vendors`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.vendorOrganizations]}
          />
          <OrganizationTable />
        </Route>
      </Switch>
    </div>
  );
};

export default ORGApp;