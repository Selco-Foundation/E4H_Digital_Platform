import React, { useEffect, useMemo, useRef, useState } from "react";
import { Loader, Table } from "@egovernments/digit-ui-react-components";
import { Link, useHistory, useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import useFieldPlan from "../../hooks/useFieldPlan";
import SearchCentre from "../../components/FieldPlanList/Search";

const FieldPlanList = () => {

  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getStateId();
  const history = useHistory();
  const location = useLocation();
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const queryParams = new URLSearchParams(window.location.search);

  const [queryFilter, setQueryFilter] = useState((() => {
    try {
      const filterParam = queryParams.get("filter");
      return filterParam ? JSON.parse(filterParam) : null;
    } catch (error) {
      console.error("Failed to parse filter parameter:", error);
      return null;
    }
  })() || { tenantId, name: "" });
  const prevSearchParamsRef = useRef(JSON.stringify(queryFilter));
  const [pageSize, setPageSize] = useState(parseInt(queryParams.get("pageSize")) || 10);
  const [pageOffset, setPageOffset] = useState(parseInt(queryParams.get("pageOffset")) || 0);
  const prevPageSizeRef = useRef(pageSize);

  const { isLoading, data } = useFieldPlan(queryFilter, pageSize, pageOffset);

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  useEffect(() => {
    history.replace({
      pathname: location.pathname,
      search: `filter=${JSON.stringify(queryFilter)}&pageSize=${pageSize}&pageOffset=${pageOffset}`
    });
  }, [queryFilter, pageSize, pageOffset])

  useEffect(() => {
    const prevSearchParams = prevSearchParamsRef.current;
    const currentSearchParams = JSON.stringify(queryFilter);

    if (prevSearchParams !== currentSearchParams || prevPageSizeRef.current !== pageSize) {
      setPageOffset(0);
      prevSearchParamsRef.current = currentSearchParams;
      prevPageSizeRef.current = pageSize;
    }
  }, [queryFilter, pageSize]);

  const onSearch = (textToSearch) => {
    setQueryFilter({
      tenantId,
      name: textToSearch
    });
  }

  const onClear = () => {
    setQueryFilter({
      tenantId
    });
  }

  const onPageSizeChange = (e) => {
    setPageSize(parseInt(e.target.value));
    setPageOffset(0);
  }

  const onNextPage = () => {
    setPageOffset(pageOffset + pageSize);
  }

  const onPrevPage = () => {
    setPageOffset(pageOffset - pageSize);
  }

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
            to={`/${window.contextPath}/employee/pm/field-plans/${row.original["id"]}/facilities`}
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
    [t]
  );

  const renderFieldPlans = () => {
    if (isLoading) {
      return <Loader />;
    }

    if (!data?.fieldPlans?.length) {
      return (
        <div style={{ display: "flex", minWidth: "700px", justifyContent: "center", alignItems: "center", height: "300px", backgroundColor: "white" }}>
          <div style={{ fontSize: "20px", fontWeight: "bold" }}>
            {t("PM_NO_FIELD_PLANS_FOUND")}
          </div>
        </div>
      );
    }

    return (
      <div style={{
        backgroundColor: "white",
        padding: "15px 0px 0px 0px",
        minWidth: "700px",
      }}>
        <div style={{
          margin: "0px 20px",
          overflow: "auto",
        }}>
          <Table
            t={t}
            data={data.fieldPlans}
            columns={columns}
            getCellProps={() => {
              return {
                style: {
                  maxWidth: "100%",
                  padding: "17.24px 18px",
                  fontSize: "15px",
                },
              };
            }}
            onNextPage={onNextPage}
            onPrevPage={onPrevPage}
            currentPage={Math.floor(pageOffset / pageSize)}
            totalRecords={data.totalCount}
            onPageSizeChange={onPageSizeChange}
            pageSizeLimit={pageSize}
          />
        </div>
      </div>
    )
  }

  return (
    <div style={{marginTop: "20px", padding: mobileView ? "15px" : "0px 10px", overflow: "auto"}}>
      <div style={{fontSize: "40px", fontWeight: "bold", fontFamily: "Roboto Condensed", marginBottom: "20px", color: "#0B0C0C"}}>
        {t("CS_COMMON_FIELD_PLANS")}
      </div>
      <SearchCentre t={t} queryFilter={queryFilter} onSearch={onSearch} onClear={onClear} />
      {renderFieldPlans()}
    </div>
  );
};

export default FieldPlanList;
