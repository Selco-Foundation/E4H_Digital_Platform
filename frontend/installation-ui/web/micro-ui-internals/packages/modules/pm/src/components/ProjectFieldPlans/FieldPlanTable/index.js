import React, { useMemo, useState } from "react";
import { Loader, Table } from "@egovernments/digit-ui-react-components";
import { Link } from "react-router-dom";
import useFieldPlan from "../../../hooks/useFieldPlan";

const FieldPlanTable = ({ t, projectId }) => {

  const tenantId = Digit.ULBService.getStateId();
  const [pageSize, setPageSize] = useState(10);
  const [pageOffset, setPageOffset] = useState(0);

  const { isLoading: fieldPlanDataLoading, data: fieldPlanData } = useFieldPlan({
    tenantId,
    projectIds: [projectId],
  });

  const placeHolderFieldPlans = [{}, {}];

  const formatDate = (timestamp) => {
    const date = new Date(timestamp);
    const month = date.toLocaleString("en-US", { month: "long" });
    const day = String(date.getDate()).padStart(2, "0");
    const year = date.getFullYear();
    return `${day} ${month} ${year}`;
  };

  const GetHead = (value) => (
    <div style={{ height: "38px", width: "100%", display: "flex", alignItems: "center" }}>
      <span>{value}</span>
    </div>
  );

  const GetCell = (value) => (
    <span style={{ fontSize: "16px", fontWeight: "400", fontFamily: "Roboto", color: "#363636" }}>
      {value}
    </span>
  );

  const GetActivityList = (activities) => (
    <div style={{ display: "flex", flexWrap: "wrap", gap: "10px", alignItems: "center" }}>
      {activities?.map((activity) => (
        <span
          key={activity.code}
          style={{
            backgroundColor: "#F1FFF8",
            color: "#00703C",
            width: "fit-content",
            padding: "5px 10px",
          }}
        >
          {activity.name}
        </span>
      ))}
    </div>
  );

  const columns = useMemo(
    () => [
      {
        id: "fieldPlanName",
        Header: () => GetHead(t("FIELD_PLAN_NAME")),
        Cell: ({ row }) => (
          <Link
            to={`/${window.contextPath}/employee/pm/project/${projectId}/field-plan/create?fieldPlanId=${row.original["id"]}&key=1`}
            style={{ color: "#C84C0E" }}
          >
            {row.original["name"]}
          </Link>
        ),
      },
      {
        id: "activities",
        Header: () => GetHead(t("ACTIVITIES")),
        Cell: ({ row }) => GetActivityList(row.original["activities"]),
      },
      {
        id: "startDate",
        Header: () => GetHead(t("START_DATE")),
        Cell: ({ row }) => GetCell(row.original["startDate"] ? formatDate(row.original["startDate"]) : ""),
      },
      {
        id: "endDate",
        Header: () => GetHead(t("END_DATE")),
        Cell: ({ row }) => GetCell(row.original["endDate"] ? formatDate(row.original["endDate"]) : ""),
      },
      {
        id: "numberOfHealthFacilities",
        Header: () => GetHead(t("NUMBER_OF_HEALTH_FACILITIES")),
        Cell: ({ row }) => GetCell(row.original["healthFacilityNumber"]),
      },
      {
        id: "status",
        Header: () => GetHead(t("FIELD_PLAN_STATUS")),
        Cell: ({ row }) => GetCell(row.original["status"] ? t(`PM_FIELD_PLAN_STATUS_${row.original["status"].toUpperCase()}`) : ""),
      },
    ],
    [t, projectId]
  );

  const onPageSizeChange = (e) => {
    setPageSize(parseInt(e.target.value));
    setPageOffset(0);
  };

  const onNextPage = () => {
    setPageOffset(pageOffset + pageSize);
  };

  const onPrevPage = () => {
    setPageOffset(pageOffset - pageSize);
  };

  if (fieldPlanDataLoading) {
    return <Loader />;
  }

  return (
    <div style={{ borderRadius: "6px", overflow: "hidden", boxShadow: "0px 0px 4px 0 rgba(0, 0, 0, 0.2)" }}>
      <Table
        t={t}
        data={fieldPlanData?.fieldPlans?.length ? fieldPlanData.fieldPlans : placeHolderFieldPlans}
        columns={columns}
        customTableWrapperClassName={"project-details-table"}
        getCellProps={() => {
          return {
            style: {
              height: "70px",
              minHeight: "fit-content",
            },
          };
        }}
        styles={{ minWidth: "300px", overflow: "auto" }}
        onNextPage={onNextPage}
        onPrevPage={onPrevPage}
        currentPage={Math.floor(pageOffset / pageSize)}
        totalRecords={fieldPlanData?.totalCount || placeHolderFieldPlans.length}
        onPageSizeChange={onPageSizeChange}
        pageSizeLimit={pageSize}
      />
    </div>
  );
};

export default FieldPlanTable;
