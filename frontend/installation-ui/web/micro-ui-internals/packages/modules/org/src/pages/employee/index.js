import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Route, Switch, useRouteMatch } from "react-router-dom";
import { BreadCrumb } from "@egovernments/digit-ui-components";
import { useSelector } from "react-redux";

const ORGApp = () => {
  const { t } = useTranslation();
  const { path } = useRouteMatch();
  const match = useRouteMatch();
  const navigator = useSelector((state) => state.org.common);

  const breadcrumbConfig = {
    home: {
      content: t("CS_COMMON_HOME"),
      internalLink: `/${window.contextPath}/employee`,
      show: true,
    },
  };

  useEffect(() => {
    Digit.UserService.setType("employee");
  }, []);

  return (
    <div className="ground-container">
      <Switch>

      </Switch>
    </div>
  );
};

export default ORGApp;