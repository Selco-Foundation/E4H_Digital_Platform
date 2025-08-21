import React, { useCallback, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { Loader, Header } from "@selco/digit-ui-react-components";
import DesktopInbox from "../../components/DesktopInbox";
import MobileInbox from "../../components/MobileInbox";
import { Link, useHistory, useLocation } from "react-router-dom";

const Inbox = () => {
  const { t } = useTranslation();
  let tenantId = Digit.ULBService.getCurrentTenantId();
  const stateTenantId = Digit.ULBService.getStateId();
  const { uuid } = Digit.UserService.getUser().info;
  const [totalRecords, setTotalRecords] = useState(0);
  const userRoles = Digit.SessionStorage.get("User")?.info?.roles || [];
  const { nearingSLA } = Digit.Hooks.useQueryParams();

  const isCodePresent = (array, codeToCheck) => array.some((item) => item.code === codeToCheck);

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
        wfFilters: { assignee: [{ code: isCodePresent(userRoles, "COMPLAINT_RESOLVER") ? uuid : "" }] },
      },
      search: "",
      sort: {},
    }
  );

  const [pageOffset, setPageOffset] = useState(parseInt(queryParams.get("pageOffset")) || 0);
  const [pageSize, setPageSize] = useState(parseInt(queryParams.get("pageSize")) || 10);
  const prevSearchParamsRef = useRef(JSON.stringify(searchParams));
  const prevPageSizeRef = useRef(pageSize);

  // ---- helper: send filter usage safely ----
  const sendFilterEvent = (filterName, params = {}) => {
    try {
      if (Digit?.Utils?.analytics?.trackFilterUsage) {
        Digit.Utils.analytics.trackFilterUsage(filterName, {
          page_name: "inbox",
          ...params,
        });
      } else if (typeof window !== "undefined" && typeof window.gtag === "function") {
        window.gtag("event", "filter_usage", {
          filter_name: filterName,
          page_name: "inbox",
          ...params,
        });
      }
    } catch (e) {
      console.warn("analytics: filter_usage failed", e);
    }
  };

  // ---- shared normalizers ----
  const toArray = (val) => {
    if (!val && val !== 0) return [];
    return Array.isArray(val) ? val : [val];
  };

  const normalizeCodes = (valArray) => {
    const codes = toArray(valArray)
      .map((v) => {
        if (v == null) return null;
        if (typeof v === "string" || typeof v === "number") return String(v);
        if (typeof v === "object") return v.code || v.key || v.name || v.tenantId || null;
        return null;
      })
      .filter(Boolean);
    return Array.from(new Set(codes)).sort();
  };

  // ---- Issue Type extractor (robust) ----
  const getIssueInfoFromFilters = (filtersObj = {}) => {
    const pf = filtersObj?.pgrfilters || {};
    const pq = filtersObj?.pgrQuery || {};

    const map = {
      "pgrfilters.issueType": pf?.issueType,
      "pgrfilters.serviceCode": pf?.serviceCode,
      "pgrfilters.serviceCodes": pf?.serviceCodes,
      "pgrfilters.serviceDefinition": pf?.serviceDefinition,
      "pgrfilters.complaintType": pf?.complaintType,
      "pgrfilters.incidentType": pf?.incidentType,

      "pgrQuery.issueType": pq?.issueType,
      "pgrQuery.serviceCode": pq?.serviceCode,
      "pgrQuery.serviceCodes": pq?.serviceCodes,
      "pgrQuery.serviceDefinition": pq?.serviceDefinition,
      "pgrQuery.complaintType": pq?.complaintType,
      "pgrQuery.incidentType": pq?.incidentType,
    };

    const presentKeys = Object.entries(map)
      .filter(([, v]) => v != null && (Array.isArray(v) ? v.length > 0 : v !== ""))
      .map(([k]) => k);

    const mergedValues = Object.values(map).reduce((acc, v) => acc.concat(toArray(v)), []);
    const codes = normalizeCodes(mergedValues);

    console.log("[Inbox][IssueType] present keys:", presentKeys);
    console.log("[Inbox][IssueType] normalized codes:", codes);

    return { codes, presentKeys };
  };

  // ---- Health Care Centre extractor (robust across aliases) ----
  const getPhcInfoFromFilters = (filtersObj = {}) => {
    const pf = filtersObj?.pgrfilters || {};
    const pq = filtersObj?.pgrQuery || {};

    // Common aliases we’ve seen in different screens:
    // phcType, healthCentre / healthCenter, centreCode, tenantId
    const map = {
      "pgrfilters.phcType": pf?.phcType,
      "pgrfilters.healthCentre": pf?.healthCentre || pf?.healthCenter,
      "pgrfilters.centreCode": pf?.centreCode,
      "pgrfilters.tenantId": pf?.tenantId,

      "pgrQuery.phcType": pq?.phcType,
      "pgrQuery.healthCentre": pq?.healthCentre || pq?.healthCenter,
      "pgrQuery.centreCode": pq?.centreCode,
      "pgrQuery.tenantId": pq?.tenantId,
    };

    const presentKeys = Object.entries(map)
      .filter(([, v]) => v != null && (Array.isArray(v) ? v.length > 0 : v !== ""))
      .map(([k]) => k);

    // Prefer the “most specific” candidate if multiple present; else merge
    const candidates = Object.values(map).filter((v) => v != null && (Array.isArray(v) ? v.length > 0 : v !== ""));

    // If you want “first present” behavior, use candidates[0]; to be safest, merge:
    const mergedValues = candidates.reduce((acc, v) => acc.concat(toArray(v)), []);
    const codes = normalizeCodes(mergedValues);

    console.log("[Inbox][PHC] present keys:", presentKeys);
    console.log("[Inbox][PHC] normalized codes:", codes);

    return { codes, presentKeys };
  };

  useEffect(() => {
    const nearing = queryParams.get("nearing");
    try {
      if (nearing === "1") {
        Digit.Utils.analytics?.trackPageView("nearing_sla_page", {
          page_path: window.location?.pathname || "/inbox",
          page_title: "Nearing SLA Page",
        });
      } else {
        Digit.Utils.analytics?.trackPageView("inbox_page", {
          page_path: window.location?.pathname || "/inbox",
          page_title: "Inbox",
        });
      }
    } catch (e) {
      console.warn("analytics: page_view tracking failed", e);
    }

    history.replace({
      pathname: location.pathname,
      search: `filter=${JSON.stringify(searchParams)}&pageSize=${pageSize}&pageOffset=${pageOffset}`,
    });

    (async () => {
      const userRoles = Digit.SessionStorage.get("User")?.info?.roles || [];
      const applicationStatus = searchParams?.filters?.pgrfilters?.applicationStatus?.map((e) => e.code).join(",");
      if (searchParams?.filters?.pgrQuery?.phcType) {
        tenantId = searchParams?.filters?.pgrQuery?.phcType;
      } else if (
        isCodePresent(userRoles, "COMPLAINT_RESOLVER") &&
        (!searchParams?.filters?.pgrQuery || searchParams?.filters?.pgrfilters?.phcType?.length == 0) &&
        Digit.SessionStorage.get("Employee.tenantId") == stateTenantId
      ) {
        const codes = Digit.SessionStorage.get("Tenants")
          .filter((item) => item.code !== stateTenantId)
          .map((item) => item.code)
          .join(",");
        tenantId = codes;
      }
    })();
  }, [searchParams, pageSize, pageOffset]); // eslint-disable-line react-hooks/exhaustive-deps

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

  // 🔹 FILTER TRACKING
  const handleFilterChange = (nextFilters) => {
    try {
      const old = searchParams?.filters || {};
      const next = nextFilters || {};

      // 10) Status Filter
      const oldStatuses = (old?.pgrfilters?.applicationStatus || []).map((e) => e.code).join(",") || "";
      const newStatuses = (next?.pgrfilters?.applicationStatus || []).map((e) => e.code).join(",") || "";
      if (oldStatuses !== newStatuses) {
        sendFilterEvent("status_filter", { value: newStatuses || "all" });
      }

      // 11) Assigned to Me vs All Tickets toggle
      const hadAssignee = (old?.wfFilters?.assignee || []).map((e) => e.code).filter(Boolean).length > 0;
      const hasAssignee = (next?.wfFilters?.assignee || []).map((e) => e.code).filter(Boolean).length > 0;
      if (hadAssignee !== hasAssignee) {
        sendFilterEvent("assigned_toggle", { selection: hasAssignee ? "assigned_to_me" : "all_tickets" });
      }

      // 13) Issue Type Filter
      const { codes: oldIssueCodes, presentKeys: oldIssueKeys } = getIssueInfoFromFilters(old);
      const { codes: newIssueCodes, presentKeys: newIssueKeys } = getIssueInfoFromFilters(next);
      console.log("[Inbox][IssueType] old codes:", oldIssueCodes, "old keys:", oldIssueKeys);
      console.log("[Inbox][IssueType] new codes:", newIssueCodes, "new keys:", newIssueKeys);
      if (JSON.stringify(oldIssueCodes) !== JSON.stringify(newIssueCodes)) {
        sendFilterEvent("issue_type_filter", {
          value: newIssueCodes.length ? newIssueCodes.join(",") : "all",
        });
      }

      // 14) Health Care Centre Filter (robust)
      const { codes: oldPhcCodes, presentKeys: oldPhcKeys } = getPhcInfoFromFilters(old);
      const { codes: newPhcCodes, presentKeys: newPhcKeys } = getPhcInfoFromFilters(next);
      console.log("[Inbox][PHC] old codes:", oldPhcCodes, "old keys:", oldPhcKeys);
      console.log("[Inbox][PHC] new codes:", newPhcCodes, "new keys:", newPhcKeys);
      if (JSON.stringify(oldPhcCodes) !== JSON.stringify(newPhcCodes)) {
        sendFilterEvent("health_care_centre_filter", {
          value: newPhcCodes.length ? newPhcCodes.join(",") : "all",
        });
      }
    } catch (e) {
      console.warn("analytics: filter diff failed", e);
    }

    setSearchParams({ ...searchParams, filters: nextFilters });
  };

  const onSearch = (params = "") => {
    Digit.Utils.analytics.trackButtonClick("search_submit", {
      page_name: "inbox",
      search_query: params || "empty",
    });
    setSearchParams({ ...searchParams, search: params });
  };

  let tenant = "";
  if (searchParams?.search?.phcType) {
    tenant = searchParams?.search?.phcType;
  }
  let isMobile = Digit.Utils.browser.isMobile();
  const allSearchParams = { ...searchParams, ...(nearingSLA === "1" && { nearingSLA: true }) };
  let { data: complaints, isLoading } = isMobile
    ? Digit.Hooks.pgr.useInboxData({ ...allSearchParams, offset: pageOffset, limit: pageSize })
    : Digit.Hooks.pgr.useInboxData({ ...allSearchParams, offset: pageOffset, limit: pageSize });

  useEffect(() => {
    if (complaints !== undefined && complaints.combinedRes?.length !== 0) {
      setTotalRecords(complaints.total);
    }
  }, [totalRecords, complaints]);

  if (complaints.length !== null) {
    if (isMobile) {
      return (
        <MobileInbox
          data={complaints}
          isLoading={isLoading}
          onFilterChange={handleFilterChange}
          onSearch={onSearch}
          searchParams={searchParams}
        />
      );
    } else {
      return (
        <div>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <Header>{t("ES_COMMON_INBOX")}</Header>
            <div style={{ color: "#9e1b32", marginBottom: "10px", textAlign: "right", marginRight: "15px" }}>
              <Link to={`/${window.contextPath}/employee`}>{t("CS_COMMON_BACK")}</Link>
            </div>
          </div>
          <DesktopInbox
            data={complaints}
            isLoading={isLoading}
            onFilterChange={handleFilterChange}
            onSearch={onSearch}
            searchParams={searchParams}
            onNextPage={fetchNextPage}
            onPrevPage={fetchPrevPage}
            onPageSizeChange={handlePageSizeChange}
            currentPage={Math.floor(pageOffset / pageSize)}
            totalRecords={totalRecords}
            pageSizeLimit={pageSize}
          />
        </div>
      );
    }
  } else {
    return <Loader />;
  }
};

export default Inbox;
