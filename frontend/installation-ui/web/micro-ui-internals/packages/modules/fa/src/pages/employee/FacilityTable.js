import React, { useEffect, useMemo, useRef, useState } from "react";
import { Loader, Table, Toast } from "@egovernments/digit-ui-react-components";
import Filter from "../../components/FacilityTable/Filter";
import Search from "../../components/FacilityTable/Search";
import CustomDownloadIcon from "../../components/Custom/CustomDownloadIcon";
import { IngestionService } from "../../services/Ingestion";
import CommonUtils from "../../utilities/CommonUtils";
import { facilityExportFilters, namesByCode, searchTextOf } from "../../utilities/FacilityList";
import { useHistory, useLocation } from "react-router-dom";
import FacilityAdminActions from "../../components/FacilityTable/FacilityAdminActions";
import useFacility from "../../hooks/useFacility";
import { useTranslation } from "react-i18next";

const FacilityTable = () => {

  const { t } = useTranslation();
  const [mainCheck, setMainCheck] = useState(false);
  const [selectedFacilities, setSelectedFacilities] = useState([]);
  const [fetchedData, setFetchedData] = useState([]);
  const history = useHistory();
  const location = useLocation();
  const queryParams = new URLSearchParams(window.location.search);
  const [updatingWorkflow, setUpdatingWorkflow] = useState(false);
  const [toast, setToast] = useState(null);
  const tenantId = Digit.ULBService.getCurrentTenantId();

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
      project: {
        fieldPlanId: [""],
        activityCode: [""],
      },
      facilityFilter: {
        state: [],
        district: [],
        block: [],
        facility: [],
      },
      facilitySearch: {
        text: "",
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

  const { data: mdmsResponse } = Digit.Hooks.useCustomMDMS(
    tenantId,
    "facility",
    [{ name: "FacilityCategory" }, { name: "FacilityType" }],
    { select: (data) => data, enabled: !!tenantId }
  );
  const categoryNames = useMemo(() => namesByCode(mdmsResponse?.facility?.FacilityCategory), [mdmsResponse]);
  const typeNames = useMemo(() => namesByCode(mdmsResponse?.facility?.FacilityType), [mdmsResponse]);
  const searchText = searchTextOf(projectQueryFilter);

  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => setToast(null), 2500);
      return () => clearTimeout(timer);
    }
  }, [toast]);

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

  // A new term changes projectQueryFilter, so the offset effect above takes the list back to page 1.
  const handleSearch = (term) => {
    setProjectQueryFilter({
      ...projectQueryFilter,
      facilitySearch: { text: term },
      facilitySearchQuery: term ? { searchText: term } : {},
    });
  };

  // Every facility the user can see that matches the current search and filters, not just this
  // page, as an .xlsx built by the ingestion service.
  const handleDownload = async () => {
    setUpdatingWorkflow(true);
    try {
      await IngestionService.exportFacilities(facilityExportFilters(projectQueryFilter, tenantId));
    } catch (error) {
      console.error("Error downloading facilities", error);
      setToast({ key: "error", label: (await CommonUtils.getBlobApiErrorMessage(error)) || t("FA_FACILITY_DOWNLOAD_FAILED") });
    } finally {
      setUpdatingWorkflow(false);
    }
  };

  const GetCell = (value) => <span className="cell-text" style={{ color: "#000000" }}>{value}</span>;

  const revalidateData = () => {
    setMainCheck(false);
    setSelectedFacilities([]);
  }

  const columns = [
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
      Header: t("FACILITY_CATEGORY"),
      Cell: ({ row }) => {
        const code = row.original["categoryCode"];
        return GetCell(code ? categoryNames[code] || code : "-");
      },
    },
    {
      Header: t("FACILITY_TYPE"),
      Cell: ({ row }) => {
        const code = row.original["typeCode"];
        return GetCell(code ? typeNames[code] || code : "-");
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
            {searchText ? t("FA_FACILITY_SEARCH_NO_MATCH") : t("CS_NO_FACILITIES_FOUND")}
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
            onClickRow={(row) => history.push(`/${window?.contextPath}/employee/fa/facilities/${encodeURIComponent(row?.original?.id)}`)}
          />
        </div>
      </div>
    );
  }

  return (
    <div style={{ marginTop: "20px", padding: "0px 10px", overflow: "auto" }}>
      {updatingWorkflow && (
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
      <div style={{ padding: "20px" }}>
        <FacilityAdminActions
          t={t}
          mainCheckBox={mainCheck}
          selectedFacilities={selectedFacilities}
          projectQueryFilter={projectQueryFilter}
          onSearch={handleFilterChange}
          revalidateData={revalidateData}
          setUpdatingWorkflow={setUpdatingWorkflow}
        />
      </div>
      <div style={{ width: "100%", display: "flex", gap: "15px" }}>
        <div style={{ minWidth: "300px" }}>
          <Filter t={t} type="desktop" projectQueryFilter={projectQueryFilter} onFilterChange={handleFilterChange} />
        </div>
        <div style={{ width: "83%", minWidth: "750px", backgroundColor: "white" }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: "16px", padding: "20px 20px 0" }}>
            <Search t={t} value={projectQueryFilter?.facilitySearch?.text || ""} onSearch={handleSearch} />
            <button
              id={"faDownloadFacilitiesBtn"}
              type="button"
              title={t("FA_DOWNLOAD_VISIBLE_FACILITIES_HINT")}
              onClick={handleDownload}
              disabled={updatingWorkflow}
              style={{
                backgroundColor: "white",
                border: "1px solid #d35400",
                color: "#d35400",
                padding: "8px 20px",
                cursor: "pointer",
                fontWeight: "bold",
                fontSize: "16px",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                gap: "8px",
                height: "40px",
                whiteSpace: "nowrap",
              }}
            >
              <CustomDownloadIcon fill={"#d35400"} height="20" width="16" />
              <span>{t("CORE_COMMON_DOWNLOAD")}</span>
            </button>
          </div>
          {renderFacilities()}
        </div>
      </div>
      {toast && (
        <Toast
          error={toast.key === "error"}
          warning={toast.key === "warning"}
          style={{ zIndex: 100000000, ...(toast.key === "error" ? { backgroundColor: "#B91900" } : {}) }}
          label={toast.label}
          isDleteBtn={true}
          onClose={() => setToast(null)}
        />
      )}
    </div>
  );
};

export default FacilityTable;
