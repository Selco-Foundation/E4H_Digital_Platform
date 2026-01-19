import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Route, Switch, useRouteMatch } from "react-router-dom";
import { BreadCrumb } from "@egovernments/digit-ui-components";
import { useSelector } from "react-redux";
import OrganizationDetails from "./OrganizationDetails";

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
    organizations: {
      content: t("ORGANIZATIONS"),
      internalLink: `/${window.contextPath}/org/organizations`,
      show: true,
    },
    organizationDetails: {
      content: navigator?.workingOrganization?.name,
      internalLink: `/${window.contextPath}/org/organizations/:organizationId/details`,
      show: true,
    },
  };

  useEffect(() => {
    Digit.UserService.setType("employee");
  }, []);

  return (
    <div className="ground-container">
      <Switch>
        <Route path={`${path}/organizations/:organizationId/details`} exact={true}>
          <BreadCrumb
            spanStyle={{ color: "#0B0C0C" }}
            crumbs={[breadCrumbsConfig.home, breadCrumbsConfig.organizations, breadCrumbsConfig.organizationDetails]}
          />
          <OrganizationDetails />
        </Route>
      </Switch>
    </div>
  );
};

export default ORGApp;