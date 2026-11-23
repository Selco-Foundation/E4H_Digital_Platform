import { useQuery, useQueryClient } from "react-query";
import { AMCVisitService } from "../services/AMCVisit";

const formatVisits = (visits) => {
  return visits?.map((row) => ({
      id: row?.id,
      facilityName: row?.facility?.facility_name,
      facilityId: row?.facility?.id,
      status: row?.status,
      block: row?.facility?.additionalDetails?.boundary?.block,
      district: row?.facility?.additionalDetails?.boundary?.district,
      assigned: row?.assignments?.[0]?.user?.name,
  }));
}

const fetchVisits = async (filter, limit, offset) => {
  const visitsResponse = await AMCVisitService.fetchVisits(filter, limit, offset);
  return {
    visits: formatVisits(visitsResponse?.ScheduledVisits),
    totalCount: visitsResponse?.TotalCount,
  }
}

const useAMCVisit = (projectQueryFilter, pageSize, pageOffset) => {

  const { configuration, facilityFilterQuery, facilitySearchQuery } = projectQueryFilter;

  const filter = {
    searchCriteria: {
      tenantId: Digit.ULBService.getCurrentTenantId(),
    }
  };

  if (configuration?.amcConfigurationId?.length) {
    filter.searchCriteria.amcConfigurationIds = configuration.amcConfigurationId;
  }

  if (configuration?.tenantId) {
    filter.searchCriteria.tenantId = configuration.tenantId;
  }

  if (configuration?.id?.length) {
    filter.searchCriteria.ids = configuration.id;
  }

  if (facilityFilterQuery?.status) {
    filter.searchCriteria.statuses = facilityFilterQuery.status;
  }

  if (facilitySearchQuery?.name) {
    filter.searchCriteria.facilityName = facilitySearchQuery.name;
  }

  const limit = pageSize || 10;
  const offset = pageOffset || 0;

  const queryClient = useQueryClient();
  const { isLoading, isFetching, isError, error, data } = useQuery(
    ["AMC_VISIT_LIST", filter, limit, offset],
    () => fetchVisits(filter, limit, offset)
  );

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["AMC_VISIT_LIST"]),
  }
}

export default useAMCVisit;
