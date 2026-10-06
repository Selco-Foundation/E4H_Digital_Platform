import React, { useEffect, useRef, useState } from "react";
import {Loader, Table} from "@egovernments/digit-ui-react-components";
import Filter from "../../components/Facilities/Filter";
import { useHistory, useLocation } from "react-router-dom";
import useFacility from "../../hooks/useFacility";
import { useTranslation } from "react-i18next";

const Facilities = () => {

  const { t } = useTranslation();
  const [fetchedData, setFetchedData] = useState([]);
  const history = useHistory();
  const location = useLocation();
  const queryParams = new URLSearchParams(window.location.search);

  const [projectQueryFilter, setProjectQueryFilter] = useState(
    (() => {
      try {
        const filterParam = queryParams.get("filter");
        return filterParam ? JSON.parse(filterParam) : null;
      } catch (error) {
        console.error("Failed to parse filter parameter:", error);
        return null;
      }
    })() || {
      facilityFilter: {
        state: [],
        district: [],
        block: [],
        facility: [],
      },
      facilitySearch: {
        name: "",
      },
      facilityFilterQuery: {},
      facilitySearchQuery: {},
    }
  );
  const prevSearchParamsRef = useRef(JSON.stringify(projectQueryFilter));

  const [pageSize, setPageSize] = useState(parseInt(queryParams.get("pageSize")) || 10);
  const [pageOffset, setPageOffset] = useState(parseInt(queryParams.get("pageOffset")) || 0);
  const prevPageSizeRef = useRef(pageSize);

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

  const { isLoading, data: facilityData } = useFacility(projectQueryFilter, pageSize, pageOffset);

  useEffect(() => {
    if (facilityData) {
      setFetchedData(facilityData.facilities || []);
    }
  }, [facilityData]);

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

  const handleFilterChange = (filters) => {
    setProjectQueryFilter({
      ...projectQueryFilter,
      ...filters,
    });
  };

  const GetCell = (value) => <span className="cell-text" style={{ color: "#000000" }}>{value}</span>;

  const columns = [
    {
      Header: t("CS_FACILITY_ID"),
      Cell: ({ row }) => {
        return GetCell(row.original["id"] ? row.original["id"] : "-");
      },
    },
    {
      Header: t("CS_HEALTH_FACILITY"),
      Cell: ({ row }) => {
        return GetCell(row.original["facilityName"] ? row.original["facilityName"] : "-");
      },
    },
    {
      Header: t("CS_STATE"),
      Cell: ({ row }) => {
        return GetCell(row.original["state"] ? t(`Boundary_${row.original["state"]}`) : "-");
      },
    },
    {
      Header: t("CS_DISTRICT"),
      Cell: ({ row }) => {
        return GetCell(row.original["district"] ? t(`Boundary_${row.original["district"]}`) : "-");
      },
    },
    {
      Header: t("CS_BLOCK"),
      Cell: ({ row }) => {
        return GetCell(row.original["block"] ? t(`Boundary_${row.original["block"]}`) : "-");
      },
    },
    {
      Header: t("CS_POC_NAME"),
      Cell: ({ row }) => {
        return GetCell(row.original["pocName"] ? row.original["pocName"] : "-");
      },
    },
  ];

  const renderFacilities = () => {
    if (isLoading) {
      return <Loader />;
    }

    if (fetchedData.length === 0) {
      return (
        <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "70%" }}>
          <div style={{ fontSize: "20px", fontWeight: "bold" }}>
            {t("CS_NO_FACILITIES_FOUND")}
          </div>
        </div>
      );
    }

    return (
      <div
        style={{
          backgroundColor: "white",
        }}
      >
        <div
          className={"admin-facility-table-wrapper"}
          style={{
            margin: "20px",
            overflow: "auto",
          }}
        >
          <Table
            t={t}
            data={fetchedData}
            columns={columns}
            customTableWrapperClassName={"admin-facility-table"}
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
            totalRecords={facilityData?.total || 0}
            onPageSizeChange={onPageSizeChange}
            pageSizeLimit={pageSize}
            onClickRow={(row) => history.push(`/${window?.contextPath}/employee/pm/facilities/${encodeURIComponent(row?.original?.id)}/details`)}
          />
        </div>
      </div>
    );
  }

  return (
    <div style={{ marginTop: "20px", padding: "0px 10px", overflow: "auto" }}>
      <div style={{ padding: "20px" }}>
        <h1
          style={{
            fontSize: "40px",
            fontWeight: "bold",
            fontFamily: "Roboto Condensed",
            margin: "0",
            color: "#0B0C0C",
          }}
        >
          {t("FACILITIES")}
        </h1>
      </div>
      <div style={{ width: "100%", display: "flex", gap: "15px" }}>
        <div style={{ minWidth: "300px" }}>
          <Filter t={t} type="desktop" projectQueryFilter={projectQueryFilter} onFilterChange={handleFilterChange} />
        </div>
        <div style={{ width: "83%", minWidth: "750px", backgroundColor: "white" }}>{renderFacilities()}</div>
      </div>
    </div>
  );
};

export default Facilities;
