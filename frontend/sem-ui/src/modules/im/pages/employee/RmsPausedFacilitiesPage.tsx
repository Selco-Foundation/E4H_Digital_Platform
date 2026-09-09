import { contextPath, employeeHomePath, translateOr, useTranslate } from "@/shared";
import { Pagination, TopBar } from "@/ui";
import { useMemo, useState } from "react";
import { RmsPausedFilter, type RmsPausedFilterState } from "../../components/rms/RmsPausedFilter";
import { RmsPausedTable } from "../../components/rms/RmsPausedTable";
import { IM_ROUTES } from "../../constants/routes";
import { useRmsPausedFacilities } from "../../hooks/use-rms-paused-facilities";
import type { PausedFacilityFilters } from "../../types/rms";

const EMPTY_FILTER: RmsPausedFilterState = { state: null, district: null, block: null };

export function RmsPausedFacilitiesPage() {
  const { t } = useTranslate();
  const basePath = `/${contextPath()}`;
  const homePath = employeeHomePath();
  const imRootPath = `${basePath}${IM_ROUTES.imRoot}`;
  const pauseRmsPath = `${basePath}${IM_ROUTES.pauseRms}`;

  const [filter, setFilter] = useState<RmsPausedFilterState>(EMPTY_FILTER);
  const [pageOffset, setPageOffset] = useState(0);
  const [pageSize, setPageSize] = useState(10);

  const apiFilters: PausedFacilityFilters = useMemo(
    () => ({
      ...(filter.state ? { state: [filter.state.code] } : {}),
      ...(filter.district ? { district: [filter.district.code] } : {}),
      ...(filter.block ? { block: [filter.block.code] } : {}),
    }),
    [filter],
  );

  const { data, isLoading } = useRmsPausedFacilities(apiFilters, pageSize, pageOffset);
  const facilities = data?.pausedFacilities ?? [];
  const totalCount = data?.totalCount ?? 0;

  function handleFilterChange(next: RmsPausedFilterState) {
    setFilter(next);
    setPageOffset(0);
  }

  return (
    <div className="space-y-6">
      <TopBar
        title={translateOr(t, "RMS_PAUSED_FACILITIES", "RMS Paused Facilities")}
        breadcrumbs={[
          { label: translateOr(t, "CORE_COMMON_OVERVIEW", "Overview"), to: homePath },
          { label: translateOr(t, "ES_IM_HEADER_INCIDENTS", "Tickets"), to: imRootPath },
          { label: translateOr(t, "RMS_PAUSED_FACILITIES", "RMS Paused Facilities") },
        ]}
      />

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[280px_1fr]">
        <RmsPausedFilter value={filter} onChange={handleFilterChange} pauseRmsPath={pauseRmsPath} />

        <div className="space-y-4">
          <RmsPausedTable facilities={facilities} isLoading={isLoading} />
          {totalCount > 0 ? (
            <Pagination
              currentPage={Math.floor(pageOffset / pageSize)}
              totalRecords={totalCount}
              pageSizeLimit={pageSize}
              onNextPage={() => setPageOffset((prev) => prev + pageSize)}
              onPrevPage={() => setPageOffset((prev) => Math.max(0, prev - pageSize))}
              onPageChange={(page) => setPageOffset(page * pageSize)}
              onPageSizeChange={(size) => {
                setPageSize(size);
                setPageOffset(0);
              }}
            />
          ) : null}
        </div>
      </div>
    </div>
  );
}
