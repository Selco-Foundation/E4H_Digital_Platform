import React, { useEffect, useMemo, useRef, useState } from "react";
import { Loader, Table } from "@egovernments/digit-ui-react-components";
import { Link, useHistory, useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useDispatch } from "react-redux";
import useFieldPlan from "../../hooks/useFieldPlan";
import useActivityFacility from "../../hooks/useActivityFacility";
import Filter from "../../components/FieldPlanFacilities/Filter";
import SearchAction from "../../components/FieldPlanFacilities/SearchAction";
import { populateWorkingFieldPlan, populateWorkingFacility } from "../../redux/actions";

const FieldPlanFacilities = () => {

  const { t } = useTranslation();
  const dispatch = useDispatch();
  const history = useHistory();
  const location = useLocation();
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const url = window.location.href;
  const fieldPlanId = url.split("field-plans/")[1].split("/")[0];
  const queryParams = new URLSearchParams(window.location.search);

  const [projectQueryFilter, setProjectQueryFilter] = useState((() => {
    try {
      const filterParam = queryParams.get("filter");
      return filterParam ? JSON.parse(filterParam) : null;
    } catch (error) {
      console.error("Failed to parse filter parameter:", error);
      return null;
    }
  })() || {
    facilityFilter: {
      district: [],
      block: [],
      status: []
    },
    facilitySearch: {
      name: ""
    },
    facilityFilterQuery: {},
    facilitySearchQuery: {},
  });
  const prevSearchParamsRef = useRef(JSON.stringify(projectQueryFilter));

  const [pageSize, setPageSize] = useState(parseInt(queryParams.get("pageSize")) || 10);
  const [pageOffset, setPageOffset] = useState(parseInt(queryParams.get("pageOffset")) || 0);
  const prevPageSizeRef = useRef(pageSize);

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const { isLoading: fieldPlanDataLoading, data: fieldPlanData } = useFieldPlan({
    ids: [fieldPlanId],
  });

  const fieldPlan = fieldPlanData?.fieldPlans?.[0];
  const activityCodes = fieldPlan?.activities?.map((activity) => activity.code);

  const {
    isLoading,
    isFetching: facilityDataFetching,
    data: facilityData,
  } = useActivityFacility({
    project: {
      fieldPlanId: [fieldPlanId],
      activityCode: activityCodes,
    },
    facilityFilterQuery: projectQueryFilter.facilityFilterQuery,
    facilitySearchQuery: projectQueryFilter.facilitySearchQuery,
  }, pageSize, pageOffset);

  useEffect(() => {
    if (fieldPlan) {
      dispatch(populateWorkingFieldPlan(fieldPlan));
    }
  }, [fieldPlanData]);

  useEffect(() => {
    history.replace({
      pathname: location.pathname,
      search: `filter=${JSON.stringify(projectQueryFilter)}&pageSize=${pageSize}&pageOffset=${pageOffset}`
    });
  }, [projectQueryFilter, pageSize, pageOffset])

  useEffect(() => {
    const prevSearchParams = prevSearchParamsRef.current;
    const currentSearchParams = JSON.stringify(projectQueryFilter);

    if (prevSearchParams !== currentSearchParams || prevPageSizeRef.current !== pageSize) {
      setPageOffset(0);
      prevSearchParamsRef.current = currentSearchParams;
      prevPageSizeRef.current = pageSize;
    }
  }, [projectQueryFilter, pageSize]);

  const handleFilterChange = (filters) => {
    setProjectQueryFilter({
      ...projectQueryFilter,
      ...filters,
    });
  };

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

  const GetCell = (value) => <span className="cell-text" style={{ color: "#000000" }}>{value}</span>;

  const columns = useMemo(
    () => [
      {
        Header: t("CS_HEALTH_FACILITY"),
        Cell: ({ row }) => (
          <span className="link" onClick={() => dispatch(populateWorkingFacility(row.original))}>
            <Link
              to={`/${window.contextPath}/employee/pm/field-plans/${fieldPlanId}/facilities/${row.original["id"]}/details`}
              style={{ color: "#C84C0E" }}
            >
              {row.original["facilityName"]}
            </Link>
          </span>
        ),
      },
      {
        Header: t("CS_ACTIVITY_TYPE"),
        Cell: ({ row }) => GetCell(row.original["activityType"] || "-"),
      },
      {
        Header: t("CS_BLOCK"),
        Cell: ({ row }) => GetCell(row.original["block"] ? t(`Boundary_${row.original["block"]}`) : "-"),
      },
      {
        Header: t("CS_DISTRICT"),
        Cell: ({ row }) => GetCell(row.original["district"] ? t(`Boundary_${row.original["district"]}`) : "-"),
      },
      {
        Header: t("CS_ASSIGNED_TO"),
        Cell: ({ row }) => GetCell(row.original["assigned"] ? `${row.original["assigned"]}` : "-"),
      },
      {
        Header: t("CS_STATUS"),
        Cell: ({ row }) => GetCell(row.original["status"] ? t(`CS_${row.original["status"]}`) : "-"),
      },
    ],
    [t, fieldPlanId, dispatch]
  );

  const renderFacilities = () => {
    if (isLoading) {
      return <Loader />;
    }

    if (!facilityData?.facilities?.length) {
      return (
        <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "300px" }}>
          <div style={{ fontSize: "20px", fontWeight: "bold" }}>
            {t("CS_NO_FACILITIES_FOUND")}
          </div>
        </div>
      );
    }

    return (
      <div style={{
        backgroundColor: "white",
        padding: "15px 0px 0px 0px",
      }}>
        <div
          className={"field-plan-facility-table-wrapper"}
          style={{
            margin: "0px 20px",
            overflow: "auto",
          }}
        >
          <Table
            t={t}
            customTableWrapperClassName={"field-plan-facility-table"}
            data={facilityData.facilities}
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
            totalRecords={facilityData?.totalCount}
            onPageSizeChange={onPageSizeChange}
            pageSizeLimit={pageSize}
          />
        </div>
      </div>
    )
  }

  if (fieldPlanDataLoading) {
    return <Loader />;
  }

  return (
    <div style={{marginTop: "20px", padding: "0px 10px", overflow: "auto"}}>
      {(!isLoading && facilityDataFetching) && (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            height: "100%",
            width: "100%",
            zIndex: 10000000,
            backgroundColor: "gray",
            opacity: 0.5,
            position: "fixed",
            top: 0,
            left: 0,
          }}
        >
          <Loader />
        </div>
      )}
      <div style={{fontSize: "40px", fontWeight: "bold", fontFamily: "Roboto Condensed", marginBottom: "20px", color: "#0B0C0C"}}>
        {fieldPlan?.name || t("CS_COMMON_FIELD_PLAN")}
      </div>
      <div style={{ width: "100%", display: "flex", gap: "15px" }}>
        <div style={{ minWidth: "300px" }}>
          <Filter
            t={t}
            fieldPlan={fieldPlan}
            projectQueryFilter={projectQueryFilter}
            onFilterChange={handleFilterChange}
          />
        </div>
        <div style={{ width: "83%", minWidth: "750px", backgroundColor: "white" }}>
          <div style={{ padding: "20px" }}>
            <SearchAction
              t={t}
              projectQueryFilter={projectQueryFilter}
              onSearch={handleFilterChange}
            />
          </div>
          {renderFacilities()}
        </div>
      </div>
    </div>
  );
};

export default FieldPlanFacilities;
