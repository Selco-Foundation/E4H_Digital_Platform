import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Route, Switch, useRouteMatch } from "react-router-dom";
import FieldPlanTable from "./FieldPlanTable";
import FacilityTable from "./FacilityTable";
import FacilityDetails from "./FacilityDetails";
import { BreadCrumb } from "@egovernments/digit-ui-react-components";
import { useSelector } from "react-redux";

const QCApp = () => {
  const { t } = useTranslation();
  const { path } = useRouteMatch();
  const match = useRouteMatch();
  const navigator = useSelector((state) => state.qc.common);

  const breadcrumbConfig = {
    home: {
      content: t("CS_COMMON_HOME"),
      path: `/${window.contextPath}/employee`,
      show: true,
    },
    inbox: {
      content: t("CS_COMMON_INBOX"),
      path: match.url + `/field-plan`,
      show: true,
    },
    facility: {
      content: navigator.selectedFieldPlan?.name,
      path: match.url + `/field-plan/${navigator.selectedFieldPlan?.id}/facilities`,
      show: true,
    },
    facilityDetails: {
      content: navigator.selectedFacility?.facilityName,
      path: match.url + `/field-plan/${navigator.selectedFieldPlan?.id}/facilities/${navigator.selectedFacility?.id}--${encodeURIComponent(navigator.selectedFacility?.facilityId)}`,
      show: true,
    },
  };

  useEffect(() => {
    Digit.UserService.setType("employee");
  }, []);

  return (
    <div className="ground-container">
      <Switch>
        <Route path={`${path}/field-plan`} exact={true}>
          <BreadCrumb crumbs={[breadcrumbConfig.home, breadcrumbConfig.inbox]} />
          <FieldPlanTable t={t} />
        </Route>
        <Route path={`${path}/field-plan/:planId/facilities`} exact={true}>
          <BreadCrumb crumbs={[breadcrumbConfig.home, breadcrumbConfig.inbox, breadcrumbConfig.facility]} />
          <FacilityTable t={t} />
        </Route>
        <Route path={`${path}/field-plan/:planId/facilities/:facilityId`} exact={true}>
          <BreadCrumb crumbs={[breadcrumbConfig.home, breadcrumbConfig.inbox, breadcrumbConfig.facility, breadcrumbConfig.facilityDetails]} />
          <FacilityDetails t={t}/>
        </Route>
      </Switch>
    </div>
  );
};

export default QCApp;
