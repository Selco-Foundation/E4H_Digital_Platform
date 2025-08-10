import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Route, useRouteMatch } from "react-router-dom";
import FieldPlanTable from "./FieldPlanTable";
import { Link } from "react-router-dom";
import FacilityTable from "./FacilityTable";
import FacilityDetails from "./FacilityDetails";
import { Employee } from "../../constants/Routes";
import { BreadCrumb } from "@egovernments/digit-ui-react-components";
import { useDispatch, useSelector } from "react-redux";
import { setSelectedFieldPlan } from "../../redux/actions/index";

const QCApp = () => {
  const { t } = useTranslation();
  const { path } = useRouteMatch();
  const match = useRouteMatch();
  const dispatch = useDispatch();
  const navigator = useSelector((state) => state.qc.reports);

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
      content: navigator.selectedFieldPlan,
      path: match.url + `/field-plan/${navigator.selectedFieldPlan}/facilities`,
      show: true,
    },
    facilityDetails: {
      content: navigator.selectedFacility,
      path: match.url + `/field-plan/${navigator.selectedFieldPlan}/facilities/${navigator.selectedFacility}`,
      show: true,
    },
  };

  useEffect(() => {
    Digit.UserService.setType("employee");
  }, []);

  const GetCell = (value) => <span className="cell-text">{value}</span>;

  const GetProgress = (value) => {
    return (
      <div style={{ display: "flex", gap: `${value > 99 ? "10px" : "20px"}` }}>
        <div>{value}%</div>
        <div style={{ width: "100px", height: "20px", background: "#E0E0E0", borderRadius: "5px" }}>
          <div style={{ position: "absolute", height: "20px", width: `${value}px`, background: "#00703C", borderRadius: "5px" }}></div>
        </div>
      </div>
    );
  };

  const columns = [
    {
      Header: "Field Plan Code",
      Cell: ({ row }) => {
        return (
          <div>
            <span className="link" onClick={() => dispatch(setSelectedFieldPlan(row.original["code"]))}>
              <Link to={`${path}/field-plan/${row.original["code"]}/facilities`} style={{ color: "#C84C0E" }}>
                {row.original["code"]}
              </Link>
            </span>
          </div>
        );
      },
    },
    {
      Header: "Activity Type",
      Cell: ({ row }) => {
        return GetCell(`${row.original["type"].toUpperCase()}`);
      },
    },
    {
      Header: "Health Facilities",
      Cell: ({ row }) => {
        return GetCell(`${row.original["facilities"].toUpperCase()}`);
      },
    },
    {
      Header: "Start Date",
      Cell: ({ row }) => {
        return GetCell(`${row.original["start"].toUpperCase()}`);
      },
    },
    {
      Header: "End Date",
      Cell: ({ row }) => {
        return GetCell(`${row.original["end"].toUpperCase()}`);
      },
    },
    {
      Header: "Completion Rate",
      Cell: ({ row }) => {
        return GetProgress(`${row.original["completion"]}`);
      },
    },
  ];

  const data = [
    { code: "MH-QC_HO-2024-200centres", type: "Installation", facilities: "200", start: "08/05/2025", end: "08/10/2025", completion: 40 },
    { code: "MH-QC_HO-2024-201centres", type: "Installation", facilities: "100", start: "08/03/2025", end: "22/05/2025", completion: 20 },
    { code: "MH-QC_HO-2024-202centres", type: "Installation", facilities: "400", start: "01/05/2024", end: "01/03/2025", completion: 100 },
  ];

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
          columns={columns}
          data={data}
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
