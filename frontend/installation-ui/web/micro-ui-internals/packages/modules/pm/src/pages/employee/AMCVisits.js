import React, { useEffect, useMemo, useRef, useState } from "react";
import { Loader, Table } from "@egovernments/digit-ui-react-components";
import { Link, useHistory, useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useDispatch } from "react-redux";
import useAMCConfigurationList from "../../hooks/useAMCConfigurationList";
import useAMCVisit from "../../hooks/useAMCVisit";
import Filter from "../../components/AMCVisits/Filter";
import SearchAction from "../../components/AMCVisits/SearchAction";
import { populateWorkingAMCConfiguration, populateWorkingAMCVisit } from "../../redux/actions";

const AMCVisits = () => {

  const { t } = useTranslation();
  const dispatch = useDispatch();
  const history = useHistory();
  const location = useLocation();
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const url = window.location.href;
  const configurationId = url.split("amc-configurations/")[1].split("/")[0];
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
      status: ["PENDING_APPROVAL"],
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

  const { isLoading: configurationDataLoading, data: configurationData } = useAMCConfigurationList({
    ids: [configurationId],
  });

  const amcConfiguration = configurationData?.amcConfigurations?.[0];

  const {
    isLoading,
    isFetching: visitDataFetching,
    data: visitData,
  } = useAMCVisit({
    configuration: {
      amcConfigurationId: [configurationId],
    },
    facilityFilterQuery: projectQueryFilter.facilityFilterQuery,
    facilitySearchQuery: projectQueryFilter.facilitySearchQuery,
  }, pageSize, pageOffset);

  useEffect(() => {
    if (amcConfiguration) {
      dispatch(populateWorkingAMCConfiguration(amcConfiguration));
    }
  }, [configurationData]);

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
          <span className="link" onClick={() => dispatch(populateWorkingAMCVisit(row.original))}>
            <Link
              to={`/${window.contextPath}/employee/pm/amc-configurations/${configurationId}/visits/${row.original["id"]}/details`}
              style={{ color: "#C84C0E" }}
            >
              {row.original["facilityName"]}
            </Link>
          </span>
        ),
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
    [t, configurationId, dispatch]
  );

  const statusesList = [
    {
      name: t("CS_SCHEDULED"),
      code: "SCHEDULED"
    },
    {
      name: t("CS_PENDING_APPROVAL"),
      code: "PENDING_APPROVAL"
    },
    {
      name: t("CS_APPROVED"),
      code: "APPROVED"
    },
    {
      name: t("CS_EXPIRED"),
      code: "EXPIRED"
    }
  ];

  const renderVisits = () => {
    if (isLoading) {
      return <Loader />;
    }

    if (!visitData?.visits?.length) {
      return (
        <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "70%" }}>
          <div style={{ fontSize: "20px", fontWeight: "bold" }}>
            {t("CS_NO_VISITS_FOUND")}
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
          className={"amc-visits-table-wrapper"}
          style={{
            margin: "0px 20px",
            overflow: "auto",
          }}
        >
          <Table
            t={t}
            customTableWrapperClassName={"amc-visits-table"}
            data={visitData.visits}
            columns={columns}
            getCellProps={() => {
              return {};
            }}
            onNextPage={onNextPage}
            onPrevPage={onPrevPage}
            currentPage={Math.floor(pageOffset / pageSize)}
            totalRecords={visitData?.totalCount}
            onPageSizeChange={onPageSizeChange}
            pageSizeLimit={pageSize}
          />
        </div>
      </div>
    );
  }

  if (configurationDataLoading) {
    return <Loader />;
  }

  return (
    <div style={{marginTop: "20px", padding: "0px 10px", overflow: "auto"}}>
      {(!isLoading && visitDataFetching) && (
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
      <div
        style={{
          fontSize: "40px",
          fontWeight: "bold",
          fontFamily: "Roboto Condensed",
          marginBottom: "20px",
          color: "#0B0C0C",
        }}
      >
        {amcConfiguration?.facilityName || "-"}
      </div>
      <div style={{ width: "100%", display: "flex", gap: "15px" }}>
        <div style={{ minWidth: "300px" }}>
          <Filter
            t={t}
            projectQueryFilter={projectQueryFilter}
            onFilterChange={handleFilterChange}
            statusesList={statusesList}
          />
        </div>
        <div style={{ width: "83%", minWidth: "750px", backgroundColor: "white" }}>
          <div style={{ padding: "20px" }}>
            <div
              style={{
                fontSize: "20px",
                fontWeight: "bold",
                marginBottom: "40px",
                marginLeft: "10px",
              }}
            >
              {t("AMC_VISITS")}
            </div>
            <SearchAction
              t={t}
              projectQueryFilter={projectQueryFilter}
              onSearch={handleFilterChange}
            />
          </div>
          {renderVisits()}
        </div>
      </div>
    </div>
  );
};

export default AMCVisits;
