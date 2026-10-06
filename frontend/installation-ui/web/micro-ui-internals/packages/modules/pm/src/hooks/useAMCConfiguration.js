import { useQuery, useQueryClient } from "react-query";
import { AMCService } from "../services/AMC";

const getAMCPlans = (response) => response?.AmcPlans || [];

const getTotalCount = (response, fallbackCount) => response?.TotalCount || fallbackCount;

// Keep the AMC table contract unchanged while sourcing rows from the AMC plan search API.
const formatAMCPlans = (amcPlans = []) => {
  return amcPlans.map((amcPlan) => ({
    id: amcPlan?.id,
    name: amcPlan?.name || "-",
    activities: [
      {
        code: "AMC",
        name: "AMC",
      },
    ],
    startDate: amcPlan?.startDate,
    endDate: amcPlan?.endDate,
    healthFacilityNumber: amcPlan?.healthFacilityNumber || 0,
    status: amcPlan?.status,
  }));
};

const fetchAMCPlans = async (filter, limit, offset) => {
  const allAMCPlans = [];
  const pageLimit = limit || 10;
  let nextOffset = 0;
  let hasMoreRecords = true;
  let totalCount = 0;

  while (hasMoreRecords) {
    const response = await AMCService.fetchAMCPlans(filter, pageLimit, nextOffset);
    const amcPlans = getAMCPlans(response);

    totalCount = getTotalCount(response, allAMCPlans.length + amcPlans.length);
    allAMCPlans.push(...amcPlans);
    nextOffset += amcPlans.length;
    hasMoreRecords = amcPlans.length === pageLimit && allAMCPlans.length < totalCount;
  }

  const formattedAMCPlans = formatAMCPlans(allAMCPlans);
  const paginatedAMCPlans = formattedAMCPlans.slice(offset, offset + limit);

  return {
    amcConfigurations: paginatedAMCPlans,
    totalCount: totalCount || formattedAMCPlans.length,
  };
};

const useAMCConfiguration = (queryFilter = {}, limit = 10, offset = 0) => {

  const { tenantId, ids, projectIds, amcPlanIds } = queryFilter;

  const filter = {
    searchCriteria: {
      tenantId: tenantId || Digit.ULBService.getCurrentTenantId(),
    },
  };

  if (ids?.length) {
    filter.searchCriteria.ids = ids;
  }

  if (amcPlanIds?.length) {
    filter.searchCriteria.amcPlanIds = amcPlanIds;
  }

  if (projectIds?.length) {
    filter.searchCriteria.projectIds = projectIds;
  }

  const queryClient = useQueryClient();
  const { isLoading, isFetching, isError, error, data } = useQuery(
    ["AMC_PLAN", filter, limit, offset],
    () => fetchAMCPlans(filter, limit, offset)
  );

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: async () => {
      await queryClient.invalidateQueries(["AMC_PLAN"]);
      return queryClient.getQueryData(["AMC_PLAN", filter, limit, offset]);
    }
  }
}

export default useAMCConfiguration;
