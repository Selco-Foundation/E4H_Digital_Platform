import React, { useState } from "react";
import {Loader, Table} from "@egovernments/digit-ui-react-components";
import { Link, useHistory, useLocation } from "react-router-dom";
import useReportLevelVisits from "../../hooks/useReportLevelVisits";
import StatusFilter from "../../components/ReportLevelView/StatusFilter";

const getQueryParams = (search) => new URLSearchParams(search || "");

// Restore selected status filters from the URL after refresh or shared link navigation.
const getInitialStatuses = (search) => {
  const statuses = getQueryParams(search).get("statuses");
  return statuses ? statuses.split(",").filter(Boolean) : [];
};

const getInitialPaginationValue = (search, key, fallback, allowZero = true) => {
  const value = Number(getQueryParams(search).get(key));
  return Number.isFinite(value) && value >= (allowZero ? 0 : 1) ? value : fallback;
};

const getBoundaryFilter = (code, t) => code ? {
  code,
  name: t(`Boundary_${code}`),
  type: "boundary",
} : undefined;

// Rehydrate location and vendor dropdown selections from query params.
const getInitialSearchableFilters = (search, t) => {
  const params = getQueryParams(search);
  const filters = {};
  const state = getBoundaryFilter(params.get("state"), t);
  const district = getBoundaryFilter(params.get("district"), t);
  const block = getBoundaryFilter(params.get("block"), t);
  const vendor = params.get("vendor");

  if (state) filters.state = state;
  if (district) filters.district = district;
  if (block) filters.block = block;
  if (vendor) {
    filters.vendor = {
      code: vendor,
      name: params.get("vendorName") || vendor,
    };
  }

  return filters;
};

const ReportLevelView = ({ t }) => {
  const history = useHistory();
  const location = useLocation();
  const [pageSize, setPageSize] = useState(() => getInitialPaginationValue(location.search, "pageSize", 10, false));
  const [pageOffset, setPageOffset] = useState(() => getInitialPaginationValue(location.search, "pageOffset", 0));
  const [selectedStatuses, setSelectedStatuses] = useState(() => getInitialStatuses(location.search));
  const [searchableFilters, setSearchableFilters] = useState(() => getInitialSearchableFilters(location.search, t));
  const { isLoading, isError, data } = useReportLevelVisits(pageSize, pageOffset, selectedStatuses, searchableFilters);
  const reportLevelLabel = t("AMC_REPORT_LEVEL_VIEW");

  // Store active filters and pagination in the URL so refresh/detail-back keeps the report state.
  const persistReportState = (statuses = selectedStatuses, filters = searchableFilters, nextPageSize = pageSize, nextPageOffset = pageOffset) => {
    const params = new URLSearchParams();

    if (statuses.length) params.set("statuses", statuses.join(","));
    params.set("pageSize", nextPageSize);
    params.set("pageOffset", nextPageOffset);
    if (filters.state?.code) params.set("state", filters.state.code);
    if (filters.district?.code) params.set("district", filters.district.code);
    if (filters.block?.code) params.set("block", filters.block.code);
    if (filters.vendor?.code) {
      params.set("vendor", filters.vendor.code);
      if (filters.vendor.name) params.set("vendorName", filters.vendor.name);
    }

    const search = params.toString();
    history.replace({
      pathname: location.pathname,
      search: search ? `?${search}` : "",
    });
  };

  const onNextPage = () => {
    const nextPageOffset = pageOffset + pageSize;
    setPageOffset(nextPageOffset);
    persistReportState(selectedStatuses, searchableFilters, pageSize, nextPageOffset);
  };

  const onPrevPage = () => {
    const nextPageOffset = Math.max(pageOffset - pageSize, 0);
    setPageOffset(nextPageOffset);
    persistReportState(selectedStatuses, searchableFilters, pageSize, nextPageOffset);
  };

  const onPageSizeChange = (event) => {
    const nextPageSize = parseInt(event.target.value, 10);
    if (!Number.isFinite(nextPageSize) || nextPageSize <= 0) return;
    setPageOffset(0);
    setPageSize(nextPageSize);
    persistReportState(selectedStatuses, searchableFilters, nextPageSize, 0);
  };

  const getCell = (value) => <span className="cell-text" style={{ color: "#000000" }}>{value || "-"}</span>;

  const getBoundaryCell = (code) => getCell(code ? t(`Boundary_${code}`) : "-");

  const getStatusLabel = (status) => {
    // Expired is shown as Lapsed in reviewer screens.
    if (status === "EXPIRED") return t("CS_LAPSED");
    return status ? t(`CS_${status}`) : "-";
  };

  const handleStatusFilterChange = (statuses) => {
    // Reset pagination whenever report status filters change.
    setSelectedStatuses(statuses);
    setPageOffset(0);
    persistReportState(statuses, searchableFilters, pageSize, 0);
  };

  const handleSearchableFilterChange = (filters) => {
    // Reset pagination whenever location/vendor filters change.
    setSearchableFilters(filters);
    setPageOffset(0);
    persistReportState(selectedStatuses, filters, pageSize, 0);
  };

  const columns = [
    {
      Header: t("CS_HEALTH_FACILITY"),
      Cell: ({ row }) => (
        <span className="link">
          {/* Open existing AMC report detail page from report-level view. */}
          <Link
            to={{
              pathname: `/${window.contextPath}/employee/amc/reports/${row.original.id}`,
              search: location.search,
            }}
            style={{ color: "#C84C0E" }}
          >
            {row.original.facilityName || "-"}
          </Link>
        </span>
      ),
    },
    {
      Header: t("PM_PROJECT_INFO_STATE"),
      Cell: ({ row }) => getBoundaryCell(row.original.state),
    },
    {
      Header: t("CS_DISTRICT"),
      Cell: ({ row }) => getBoundaryCell(row.original.district),
    },
    {
      Header: t("CS_BLOCK"),
      Cell: ({ row }) => getBoundaryCell(row.original.block),
    },
    {
      Header: t("CS_STATUS"),
      Cell: ({ row }) => getCell(getStatusLabel(row.original.status)),
    },
    {
      Header: t("AMC_SUBMITTED_ON"),
      Cell: ({ row }) => getCell(row.original.submittedOnFormatted),
    },
    {
      Header: t("AMC_ASSIGNED_VENDOR"),
      Cell: ({ row }) => getCell(row.original.assignedVendor),
    },
  ];

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
      name: t("CS_DELAYED"),
      code: "DELAYED"
    },
    {
      name: t("CS_LAPSED"),
      // Backend still stores lapsed AMC visits as EXPIRED.
      code: "EXPIRED"
    },
    {
      name: t("CS_REJECTED"),
      code: "REJECTED"
    },
  ];

  // Render loading, error, table, and empty states for report-level view.
  const renderState = () => {
    if (isLoading) {
      return (
        <div style={{
          display: "flex",
          flexDirection: "column",
          justifyContent: "center",
          alignItems: "center",
          minHeight: "260px",
          padding: "32px",
          textAlign: "center",
        }}>
          <Loader />
        </div>
      );
    }

    if (isError) {
      return (
        <div style={{
          display: "flex",
          flexDirection: "column",
          justifyContent: "center",
          alignItems: "center",
          minHeight: "260px",
          padding: "32px",
          textAlign: "center",
        }}>
          <div style={{
            color: "#0B0C0C",
            fontSize: "20px",
            fontWeight: "bold",
            marginBottom: "8px",
          }}>
            {t("CS_COMMON_ERROR")}
          </div>
        </div>
      );
    }

    if (data?.visits?.length) {
      return (
        <div style={{
          padding: "20px",
          overflow: "auto",
        }}>
          <Table
            t={t}
            data={data.visits}
            columns={columns}
            customTableWrapperClassName={"amc-report-level-table"}
            getCellProps={() => ({
              style: {
                maxWidth: "100%",
                padding: "20px 18px",
                fontSize: "16px",
              },
            })}
            onNextPage={onNextPage}
            onPrevPage={onPrevPage}
            currentPage={Math.floor(pageOffset / pageSize)}
            totalRecords={data.totalCount || 0}
            onPageSizeChange={onPageSizeChange}
            pageSizeLimit={pageSize}
          />
        </div>
      );
    }

    return (
      <div style={{
        display: "flex",
        flexDirection: "column",
        justifyContent: "center",
        alignItems: "center",
        minHeight: "260px",
        padding: "32px",
        textAlign: "center",
      }}>
        <div style={{
          color: "#0B0C0C",
          fontSize: "20px",
          fontWeight: "bold",
          marginBottom: "8px",
        }}>
          {t("AMC_NO_REPORTS_FOUND")}
        </div>
      </div>
    );
  };

  return (
    <div style={{marginTop: "20px", padding: "0px 10px", overflow: "auto"}}>
      <div
        style={{
          fontSize: "40px",
          fontWeight: "bold",
          fontFamily: "Roboto Condensed",
          marginBottom: "20px",
          color: "#0B0C0C",
        }}
      >
        {reportLevelLabel}
      </div>
      {/* Keep spacing same as project-level AMC visits screen. */}
      <div style={{ width: "100%", display: "flex", gap: "15px" }}>
        <div style={{ minWidth: "300px" }}>
          <StatusFilter
            t={t}
            selectedStatuses={selectedStatuses}
            searchableFilters={searchableFilters}
            statusesList={statusesList}
            filterOptions={data?.filterOptions}
            onFilterChange={handleStatusFilterChange}
            onSearchableFilterChange={handleSearchableFilterChange}
          />
        </div>
        <div style={{ width: "83%", minWidth: "750px", backgroundColor: "white" }}>
          <div style={{
            padding: "20px",
          }}>
            <div style={{
              fontSize: "20px",
              fontWeight: "bold",
              color: "#0B0C0C",
            }}>
              {t("AMC_REPORTS")}
            </div>
          </div>
          {renderState()}
        </div>
      </div>
    </div>
  );
};

export default ReportLevelView;
