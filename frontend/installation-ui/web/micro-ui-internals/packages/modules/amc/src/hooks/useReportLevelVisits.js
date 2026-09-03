import {useQuery, useQueryClient} from "react-query";
import {VisitService} from "../services/VisitService";
import {getFacilityGeography} from "../utilities/GeographyUtils";

const SUBMITTED_STATUSES = ["PENDING_APPROVAL", "APPROVED", "REJECTED"];

const formatDate = (timestamp) => {
  if (!timestamp) return "";
  const date = new Date(timestamp);
  const month = date.toLocaleString("en-US", { month: "long" });
  const day = String(date.getDate()).padStart(2, "0");
  const year = date.getFullYear();
  return `${day} ${month} ${year}`;
};

const getFilterOption = (code, type) => {
  return code ? {
    code,
    name: `Boundary_${code}`,
    type,
  } : null;
};

const getSubmittedOnTimestamp = (visit) => {
  return visit?.visitReport?.submittedAt || visit?.actualVisitDate || "";
};

// Submitted reports should appear before scheduled AMC rows.
const hasSubmittedReport = (visit) => {
  return !!getSubmittedOnTimestamp(visit) || SUBMITTED_STATUSES.includes(visit?.status);
};

const getEpochMilliseconds = (date) => {
  if (!date) return 0;
  const epochMilliseconds = new Date(date).getTime();
  return Number.isNaN(epochMilliseconds) ? 0 : epochMilliseconds;
};

const getAssignedVendor = (visit) => visit?.amcConfiguration?.vendor?.name || "";

const sortReportLevelVisits = (visits = []) => {
  return [...visits].sort((first, second) => {
    const firstSubmitted = hasSubmittedReport(first);
    const secondSubmitted = hasSubmittedReport(second);

    if (firstSubmitted !== secondSubmitted) {
      return firstSubmitted ? -1 : 1;
    }

    if (firstSubmitted) {
      return getEpochMilliseconds(getSubmittedOnTimestamp(second)) - getEpochMilliseconds(getSubmittedOnTimestamp(first));
    }

    return getEpochMilliseconds(first?.scheduledDate) - getEpochMilliseconds(second?.scheduledDate);
  });
};

// Format the API response for report-level table columns.
const formatVisits = (visits) => {
  return sortReportLevelVisits(visits)?.map((visit) => {
    const geography = getFacilityGeography(visit?.facility);
    const submittedOn = getSubmittedOnTimestamp(visit);

    return {
      id: visit?.id,
      facilityName: visit?.facilityName || visit?.facility?.facility_name || visit?.facility?.facilityName || "-",
      state: geography.state,
      district: geography.district,
      block: geography.block,
      status: visit?.status || "",
      submittedOn,
      submittedOnFormatted: formatDate(submittedOn),
      // Vendor is enriched by visit search under ScheduledVisits[].amcConfiguration.vendor.
      assignedVendor: getAssignedVendor(visit),
      assignedVendorId: visit?.amcConfiguration?.vendorId || "",
    };
  }) || [];
};

const getUniqueOptions = (visits, key, type) => {
  const options = {};

  visits.forEach((visit) => {
    const option = getFilterOption(visit?.[key], type);
    if (option) {
      options[option.code] = option;
    }
  });

  return Object.values(options).sort((first, second) => first.code.localeCompare(second.code));
};

const getVendorOptions = (visits) => {
  const options = {};

  visits.forEach((visit) => {
    const code = visit?.assignedVendorId || visit?.assignedVendor;
    if (code) {
      options[code] = {
        code,
        name: visit?.assignedVendor || code,
      };
    }
  });

  return Object.values(options).sort((first, second) => first.name.localeCompare(second.name));
};

const getVisitsByLocation = (visits, filters = {}) => {
  return visits.filter((visit) => {
    if (filters.state?.code && visit.state !== filters.state.code) return false;
    if (filters.district?.code && visit.district !== filters.district.code) return false;
    return true;
  });
};

const getFilterOptions = (visits, filters = {}) => {
  // District list depends on selected state.
  const districtVisits = filters.state?.code ? getVisitsByLocation(visits, { state: filters.state }) : visits;
  // Block and vendor lists depend on selected state and district.
  const blockVisits = getVisitsByLocation(visits, filters);

  // District and block dropdowns depend on selected parent location.
  return {
    states: getUniqueOptions(visits, "state", "boundary"),
    districts: getUniqueOptions(districtVisits, "district", "boundary"),
    blocks: getUniqueOptions(blockVisits, "block", "boundary"),
    vendors: getVendorOptions(blockVisits),
  };
};

const applySearchableFilters = (visits, filters = {}) => {
  return visits.filter((visit) => {
    if (filters.state?.code && visit.state !== filters.state.code) return false;
    if (filters.district?.code && visit.district !== filters.district.code) return false;
    if (filters.block?.code && visit.block !== filters.block.code) return false;
    if (filters.vendor?.code && ![visit.assignedVendorId, visit.assignedVendor].includes(filters.vendor.code)) return false;
    return true;
  });
};

const hasSearchableFilters = (filters = {}) => {
  return ["state", "district", "block", "vendor"].some((key) => filters?.[key]?.code);
};

// Fetch AMC visits for reviewer report-level view.
const fetchReportLevelVisits = async (filter, limit, offset, searchableFilters) => {
  // Fetch only the visible table page; pagination controls provide limit and offset.
  const visitsResponse = await VisitService.fetchVisits(filter, limit, offset);
  const scheduledVisits = visitsResponse?.ScheduledVisits || [];
  const visits = formatVisits(scheduledVisits);
  const filterOptions = getFilterOptions(visits, searchableFilters);
  const activeSearchableFilters = hasSearchableFilters(searchableFilters);
  const filteredVisits = activeSearchableFilters ? applySearchableFilters(visits, searchableFilters) : visits;

  return {
    visits: filteredVisits,
    totalCount: activeSearchableFilters
      ? filteredVisits.length
      : visitsResponse?.TotalCount !== undefined && visitsResponse?.TotalCount !== null
        ? visitsResponse.TotalCount
        : scheduledVisits.length,
    filterOptions,
  };
};

const useReportLevelVisits = (pageSize, pageOffset, statuses = [], searchableFilters = {}) => {
  // No project filter here because report-level view shows all AMC visits.
  const filter = {
    searchCriteria: {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      sort_direction: "DESC",
    },
  };

  if (statuses.length) {
    // Status checkboxes are sent as backend status filters.
    filter.searchCriteria.statuses = statuses;
  }

  const limit = pageSize || 10;
  const offset = pageOffset || 0;

  const queryClient = useQueryClient();
  const { isLoading, isFetching, isError, error, data } = useQuery(
    ["AMC_REPORT_LEVEL_VISITS", filter, limit, offset, searchableFilters],
    () => fetchReportLevelVisits(filter, limit, offset, searchableFilters),
    {
      // Keep the existing rows visible during pagination and avoid refetches when DevTools/window focus changes.
      keepPreviousData: true,
      refetchOnWindowFocus: false,
    }
  );

  return {
    isLoading,
    isFetching,
    isError,
    error,
    data,
    revalidate: () => queryClient.invalidateQueries(["AMC_REPORT_LEVEL_VISITS"]),
  };
};

export default useReportLevelVisits;
