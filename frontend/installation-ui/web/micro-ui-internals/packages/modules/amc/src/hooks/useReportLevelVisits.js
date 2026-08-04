import {useQuery, useQueryClient} from "react-query";
import {VisitService} from "../services/VisitService";
import {AMCService} from "../services/AMC";
import {getFacilityGeography} from "../utilities/GeographyUtils";

const SUBMITTED_STATUSES = ["PENDING_APPROVAL", "APPROVED", "REJECTED"];
// Backend search max limit is 200, used for searchable filter options.
const REPORT_LEVEL_FETCH_LIMIT = 200;

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

const getAssignedVendor = (visit, vendorByConfigurationId) => {
  return (
    visit?.amcConfiguration?.vendor?.name ||
    vendorByConfigurationId?.[visit?.amcConfigurationId]?.name ||
    vendorByConfigurationId?.[visit?.amcConfigurationId]?.id ||
    visit?.amcConfiguration?.vendorId ||
    ""
  );
};

const sortReportLevelVisits = (visits = []) => {
  return [...visits].sort((first, second) => {
    const firstSubmitted = hasSubmittedReport(first);
    const secondSubmitted = hasSubmittedReport(second);

    if (firstSubmitted !== secondSubmitted) {
      return firstSubmitted ? -1 : 1;
    }

    if (firstSubmitted) {
      return (getSubmittedOnTimestamp(second) || 0) - (getSubmittedOnTimestamp(first) || 0);
    }

    return (first?.scheduledDate || 0) - (second?.scheduledDate || 0);
  });
};

// Format the API response for report-level table columns.
const formatVisits = (visits, vendorByConfigurationId = {}) => {
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
      assignedVendor: getAssignedVendor(visit, vendorByConfigurationId),
      assignedVendorId: visit?.amcConfiguration?.vendorId || vendorByConfigurationId?.[visit?.amcConfigurationId]?.id || "",
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

// Visit search has vendor id; configuration search gives vendor name.
const fetchVendorByConfigurationId = async (visits = []) => {
  const amcConfigurationIds = [...new Set(visits.map((visit) => visit?.amcConfigurationId).filter(Boolean))];

  if (!amcConfigurationIds.length) {
    return {};
  }

  const tenantId = Digit.ULBService.getCurrentTenantId();
  const response = await AMCService.fetchAMCConfigurations(
    {
      searchCriteria: {
        tenantId,
        ids: amcConfigurationIds,
      },
    },
    amcConfigurationIds.length,
    0
  );

  return (response?.AmcConfigurations || []).reduce((vendorByConfigurationId, configuration) => {
    vendorByConfigurationId[configuration.id] = {
      id: configuration.vendorId,
      name: configuration.vendor?.name,
    };
    return vendorByConfigurationId;
  }, {});
};

// Fetch all AMC visits for reviewer report-level view.
const fetchReportLevelVisits = async (filter, limit, offset, searchableFilters) => {
  const visitsResponse = await VisitService.fetchVisits(filter, REPORT_LEVEL_FETCH_LIMIT, 0);
  const scheduledVisits = visitsResponse?.ScheduledVisits || [];
  const vendorByConfigurationId = await fetchVendorByConfigurationId(scheduledVisits);
  const visits = formatVisits(scheduledVisits, vendorByConfigurationId);
  const filteredVisits = applySearchableFilters(visits, searchableFilters);

  return {
    visits: filteredVisits.slice(offset, offset + limit),
    totalCount: filteredVisits.length,
    filterOptions: getFilterOptions(visits, searchableFilters),
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
    () => fetchReportLevelVisits(filter, limit, offset, searchableFilters)
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
