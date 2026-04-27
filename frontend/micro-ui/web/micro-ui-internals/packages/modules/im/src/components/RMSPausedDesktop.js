import React from "react";
import { Card, Loader } from "@selco/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import RMSPausedFilter from "./rmsPaused/Filter";
import RMSPausedTable from "./rmsPaused/Table";
import RMSFacilitiesLink from "./rmsPaused/FacilitiesLink";

const RMSPausedDesktop = ({
  data,
  isLoading,
  onFilterChange,
  searchParams,
  onNextPage,
  onPrevPage,
  onPageSizeChange,
  currentPage,
  totalRecords,
  pageSizeLimit,
}) => {
  const { t } = useTranslation();

  const columns = React.useMemo(
    () => {
      const getBoundaryLabel = (code) => {
        if (!code) return "-";
        const translated = t(`Boundary_${code}`);
        return translated === `Boundary_${code}` ? code : translated;
      };
      return [
      {
        Header: t("RMS_FACILITY_NAME"),
        Cell: ({ row }) => (
          <span className="cell-text">
            <Link
              to={`/${window.contextPath}/employee/im/pause-rms?facilityId=${encodeURIComponent(row.original.facilityId)}`}
              style={{ color: "#7a2829" }}
            >
              {row.original.facilityName || "-"}
            </Link>
          </span>
        ),
      },
      {
        Header: t("RMS_FACILITY_ID"),
        Cell: ({ row }) => <span className="cell-text">{row.original.facilityId || "-"}</span>,
      },
      {
        Header: t("RMS_FACILITY_BOUNDARY"),
        Cell: ({ row }) => <span className="cell-text">{getBoundaryLabel(row.original.boundaryCode)}</span>,
      },
      {
        Header: t("RMS_PAUSED_UNTIL"),
        Cell: ({ row }) => <span className="cell-text">{row.original.pausedUntil || "-"}</span>,
      },
      {
        Header: t("RMS_PAUSED_BY"),
        Cell: ({ row }) => <span className="cell-text">{row.original.pausedBy || "-"}</span>,
      },
      ];
    },
    [t]
  );

  let result;
  if (isLoading) {
    result = <Loader />;
  } else if (data?.rmsPausedFacilities?.length) {
    result = (
      <RMSPausedTable
        t={t}
        data={data.rmsPausedFacilities}
        columns={columns}
        onNextPage={onNextPage}
        onPrevPage={onPrevPage}
        onPageSizeChange={onPageSizeChange}
        currentPage={currentPage}
        totalRecords={totalRecords}
        pageSizeLimit={pageSizeLimit}
      />
    );
  } else {
    result = (
      <Card style={{ marginTop: 20 }}>
        <p style={{ textAlign: "center" }}>{t("RMS_NO_PAUSED_FACILITIES_FOUND")}</p>
      </Card>
    );
  }

  return (
    <div className="inbox-container">
      <div className="filters-container">
        <RMSFacilitiesLink />
        <div style={{ paddingTop: "5px", paddingBottom: "0px" }}>
          <RMSPausedFilter onFilterChange={onFilterChange} type="desktop" searchParams={searchParams} />
        </div>
      </div>
      <div style={{ flex: 1, overflowX: "scroll", width: "100%" }}>
        <div style={{ marginLeft: "24px", flex: 1 }}>{result}</div>
      </div>
    </div>
  );
};

export default RMSPausedDesktop;
