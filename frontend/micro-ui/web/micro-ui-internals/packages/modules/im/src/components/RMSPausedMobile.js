import React, { useState } from "react";
import { Card, FilterAction, Loader, PopUp, SubmitBar, ActionBar } from "@selco/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import RMSPausedFilter from "./rmsPaused/Filter";
import RMSFacilitiesLink from "./rmsPaused/FacilitiesLink";

const RMSPausedMobile = ({ data, isLoading, onFilterChange, searchParams, onNextPage, onPrevPage, currentPage, pageSizeLimit, totalRecords }) => {
  const { t } = useTranslation();
  const [showFilter, setShowFilter] = useState(false);

  const getBoundaryLabel = (code) => {
    if (!code) return "-";
    const translated = t(`Boundary_${code}`);
    return translated === `Boundary_${code}` ? code : translated;
  };

  const totalPages = Math.ceil((totalRecords || 0) / pageSizeLimit);
  const canGoNext = currentPage + 1 < totalPages;
  const canGoPrev = currentPage > 0;

  return (
    <div style={{ padding: 0 }}>
      <div className="inbox-container">
        <div className="filters-container">
          <RMSFacilitiesLink isMobile={true} />
          <div className="searchBox">
            <FilterAction text="FILTER" handleActionClick={() => setShowFilter(true)} />
          </div>
          {isLoading && <Loader />}
          {!isLoading && !data?.rmsPausedFacilities?.length && (
            <Card style={{ marginTop: 20 }}>
              <p style={{ textAlign: "center" }}>{t("RMS_NO_PAUSED_FACILITIES_FOUND")}</p>
            </Card>
          )}
          {!isLoading &&
            data?.rmsPausedFacilities?.map((item, index) => (
              <Card key={`${item.facilityId || item.boundaryCode}-${index}`} style={{ marginTop: 16 }}>
                <div>
                  <b>{t("RMS_FACILITY_NAME")}:</b>{" "}
                  <Link
                    to={`/${window.contextPath}/employee/im/pause-rms?facilityId=${encodeURIComponent(row.original.facilityId)}`}
                    style={{ color: "#7a2829" }}
                  >
                    {item.facilityName || "-"}
                  </Link>
                </div>
                <div><b>{t("RMS_FACILITY_ID")}:</b> {item.facilityId || "-"}</div>
                <div><b>{t("RMS_FACILITY_BOUNDARY")}:</b> {getBoundaryLabel(item.boundaryCode)}</div>
                <div><b>{t("RMS_PAUSED_UNTIL")}:</b> {item.pausedUntil || "-"}</div>
                <div><b>{t("RMS_PAUSED_BY")}:</b> {item.pausedBy || "-"}</div>
              </Card>
            ))}
        </div>
      </div>

      {showFilter && (
        <PopUp>
          <div className="popup-module">
            <RMSPausedFilter type="mobile" onClose={() => setShowFilter(false)} onFilterChange={onFilterChange} searchParams={searchParams} />
          </div>
        </PopUp>
      )}

      {!isLoading && totalPages > 1 && (
        <ActionBar>
          <div style={{ display: "flex", width: "100%", gap: "0.5rem" }}>
            <SubmitBar label={t("CS_COMMON_PREV")} onSubmit={onPrevPage} disabled={!canGoPrev} />
            <SubmitBar label={t("CS_COMMON_NEXT")} onSubmit={onNextPage} disabled={!canGoNext} />
          </div>
        </ActionBar>
      )}
    </div>
  );
};

export default RMSPausedMobile;
