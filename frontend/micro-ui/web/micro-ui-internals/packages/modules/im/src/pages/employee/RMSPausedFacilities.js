import React, { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { Header, Loader } from "@selco/digit-ui-react-components";
import RMSPausedDesktop from "../../components/RMSPausedDesktop";
import RMSPausedMobile from "../../components/RMSPausedMobile";
import { Link, useHistory, useLocation } from "react-router-dom";

const RMSPausedFacilities = () => {

  const { t } = useTranslation();
  const [totalRecords, setTotalRecords] = useState(0);
  const history = useHistory();
  const location = useLocation();
  const queryParams = new URLSearchParams(window.location.search);

  const [searchParams, setSearchParams] = useState(
    (() => {
      try {
        const filterParam = queryParams.get("filter");
        return filterParam ? JSON.parse(filterParam) : null;
      } catch (error) {
        console.error("Failed to parse filter parameter:", error);
        return null;
      }
    })() || {
      filters: {
        state: [],
        district: [],
        block: [],
      },
      sort: {},
    }
  );

  const [pageOffset, setPageOffset] = useState(parseInt(queryParams.get("pageOffset")) || 0);
  const [pageSize, setPageSize] = useState(parseInt(queryParams.get("pageSize")) || 10);
  const prevSearchParamsRef = useRef(JSON.stringify(searchParams));
  const prevPageSizeRef = useRef(pageSize);
  const prevUrlSearchRef = useRef(window.location.search);

  useEffect(() => {
    const nextSearch = `?filter=${encodeURIComponent(JSON.stringify(searchParams))}&pageSize=${pageSize}&pageOffset=${pageOffset}`;
    if (prevUrlSearchRef.current !== nextSearch) {
      history.replace({
        pathname: location.pathname,
        search: nextSearch,
      });
      prevUrlSearchRef.current = nextSearch;
    }
  }, [history, location.pathname, searchParams, pageSize, pageOffset]);

  useEffect(() => {
    const prevSearchParams = prevSearchParamsRef.current;
    const currentSearchParams = JSON.stringify(searchParams);

    if (prevSearchParams !== currentSearchParams || prevPageSizeRef.current !== pageSize) {
      setPageOffset(0);
      prevSearchParamsRef.current = currentSearchParams;
      prevPageSizeRef.current = pageSize;
    }
  }, [searchParams, pageSize]);

  const fetchNextPage = () => setPageOffset((prevState) => prevState + pageSize);
  const fetchPrevPage = () => setPageOffset((prevState) => prevState - pageSize);
  const handlePageSizeChange = (e) => setPageSize(Number(e.target.value));

  const handleFilterChange = (nextFilters) => {
    setSearchParams((prev) => {
      const prevFiltersString = JSON.stringify(prev?.filters || {});
      const nextFiltersString = JSON.stringify(nextFilters || {});
      if (prevFiltersString === nextFiltersString) return prev;
      return { ...prev, filters: nextFilters };
    });
  };

  const isMobile = Digit.Utils.browser.isMobile();
  const selectedFilters = searchParams?.filters || {};
  const queryFilter = {
    ...(selectedFilters?.state?.length ? { state: selectedFilters.state.map((item) => item.code).join(",") } : {}),
    ...(selectedFilters?.district?.length ? { district: selectedFilters.district.map((item) => item.code).join(",") } : {}),
    ...(selectedFilters?.block?.length ? { block: selectedFilters.block.map((item) => item.code).join(",") } : {}),
  };
  const { data: pausedFacilitiesData, isLoading } = Digit.Hooks.im.useRMSPausedFacility(searchParams, pageSize, pageOffset);

  useEffect(() => {
    if (pausedFacilitiesData !== undefined) {
      setTotalRecords(pausedFacilitiesData?.total || 0);
    }
  }, [pausedFacilitiesData]);

  if (!pausedFacilitiesData && isLoading) {
    return <Loader />;
  }

  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <Header>{t("RMS_PAUSED_FACILITIES")}</Header>
        <div style={{ color: "#9e1b32", marginBottom: "10px", textAlign: "right", marginRight: "15px" }}>
          <Link to={`/${window.contextPath}/employee`}>{t("CS_COMMON_BACK")}</Link>
        </div>
      </div>
      {isMobile ? (
        <RMSPausedMobile
          data={pausedFacilitiesData}
          isLoading={isLoading}
          onFilterChange={handleFilterChange}
          searchParams={searchParams}
          onNextPage={fetchNextPage}
          onPrevPage={fetchPrevPage}
          currentPage={Math.floor(pageOffset / pageSize)}
          pageSizeLimit={pageSize}
          totalRecords={totalRecords}
        />
      ) : (
        <RMSPausedDesktop
          data={pausedFacilitiesData}
          isLoading={isLoading}
          onFilterChange={handleFilterChange}
          searchParams={searchParams}
          onNextPage={fetchNextPage}
          onPrevPage={fetchPrevPage}
          onPageSizeChange={handlePageSizeChange}
          currentPage={Math.floor(pageOffset / pageSize)}
          totalRecords={totalRecords}
          pageSizeLimit={pageSize}
        />
      )}
    </div>
  );
};

export default RMSPausedFacilities;
