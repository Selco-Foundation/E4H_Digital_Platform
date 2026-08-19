import React, { useState } from "react";
import {Loader, Table} from "@egovernments/digit-ui-react-components";
import { Link } from "react-router-dom";
import useReportLevelVisits from "../../hooks/useReportLevelVisits";
import StatusFilter from "../../components/ReportLevelView/StatusFilter";

const ReportLevelView = ({ t }) => {
  const [pageSize, setPageSize] = useState(10);
  const [pageOffset, setPageOffset] = useState(0);
  const [selectedStatuses, setSelectedStatuses] = useState([]);
  const [searchableFilters, setSearchableFilters] = useState({});
  const { isLoading, isError, data } = useReportLevelVisits(pageSize, pageOffset, selectedStatuses, searchableFilters);
  const reportLevelLabel = t("AMC_REPORT_LEVEL_VIEW");

  const onNextPage = () => {
    setPageOffset(pageOffset + pageSize);
  };

  const onPrevPage = () => {
    setPageOffset(pageOffset - pageSize);
  };

  const onPageSizeChange = (event) => {
    setPageSize(parseInt(event.target.value, 10));
    setPageOffset(0);
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
  };

  const handleSearchableFilterChange = (filters) => {
    // Reset pagination whenever location/vendor filters change.
    setSearchableFilters(filters);
    setPageOffset(0);
  };

  const columns = [
    {
      Header: t("CS_HEALTH_FACILITY"),
      Cell: ({ row }) => (
        <span className="link">
          {/* Open existing AMC report detail page from report-level view. */}
          <Link
            to={`/${window.contextPath}/employee/amc/reports/${row.original.id}`}
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
          <div style={{
            color: "#505A5F",
            fontSize: "16px",
            maxWidth: "520px",
            lineHeight: "24px",
          }}>
            {t("AMC_REPORT_LEVEL_ERROR")}
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
