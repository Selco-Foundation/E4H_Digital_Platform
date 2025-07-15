import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Route, useRouteMatch } from "react-router-dom";
import FieldPlanTable from "./FieldPlanTable";
import FacilityTable from "./FacilityTable";
import FacilityDetails from "./FacilityDetails";
import { Employee } from "../../constants/Routes";
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
      path: match.url + `/field-plan/${encodeURIComponent(navigator.selectedFieldPlan?.name)}/facilities`,
      show: true,
    },
    facilityDetails: {
      content: navigator.selectedFacility?.facilityName,
      path: match.url + `/field-plan/${encodeURIComponent(navigator.selectedFieldPlan?.name)}/facilities/${encodeURIComponent(navigator.selectedFacility?.facilityName)}`,
      show: true,
    },
  };

  useEffect(() => {
    Digit.UserService.setType("employee");
  }, []);

  return (
    <div className="ground-container">
      <Route
        path={match.url + Employee.FieldPlan}
        component={() => <BreadCrumb crumbs={[breadcrumbConfig.home, breadcrumbConfig.inbox]} />}
        exact={true}
      />
      <Route
        path={match.url + Employee.Facility}
        component={() => <BreadCrumb crumbs={[breadcrumbConfig.home, breadcrumbConfig.inbox, breadcrumbConfig.facility]} />}
        exact={true}
      />
      <Route
        path={match.url + Employee.FacilityDetails}
        component={() => <BreadCrumb crumbs={[breadcrumbConfig.home, breadcrumbConfig.inbox, breadcrumbConfig.facility, breadcrumbConfig.facilityDetails]}></BreadCrumb>}
        exact={true}
      />
      <Route path={`${path}/field-plan`} exact={true}>
        <FieldPlanTable
          t={t}
          getCellProps={(cellInfo) => {
            return {
              style: {
                maxWidth: "100%",
                padding: "17.24px 18px",
                fontSize: "15px",
              },
            };
          }}
        />
      </Route>
      <Route path={`${path}/field-plan/:planId/facilities`} exact={true}>
        <FacilityTable
          t={t}
          getCellProps={(cellInfo) => {
            return {
              style: {
                maxWidth: "100%",
                padding: "17.24px 18px",
                fontSize: "15px",
              },
            };
          }}
        />
      </Route>
      <Route path={`${path}/field-plan/:planId/facilities/:facilityId`} exact={true}>
        <FacilityDetails t={t}/>
      </Route>
    </div>
  );
};

export default QCApp;
