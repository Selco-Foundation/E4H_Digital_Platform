import { useQuery, useQueryClient } from "react-query";
import { AMCService } from "../services/AMC";

const formatAMCConfigurations = (amcConfigurations = []) => {
  return amcConfigurations.map((amcConfiguration) => ({
    id: amcConfiguration?.id,
    name: amcConfiguration?.project?.name || "-",
    activities: [
      {
        code: "AMC",
        name: "AMC",
      },
    ],
    startDate: amcConfiguration?.configurationStartDate,
    endDate: amcConfiguration?.configurationEndDate,
    healthFacilityNumber: amcConfiguration?.facilityId ? 1 : 0,
    status: amcConfiguration?.status,
  }));
};

const fetchAMCConfigurations = async (filter, limit, offset) => {
  // Keep this server-paginated; fetching every AMC page here causes many duplicate _search calls.
  const response = await AMCService.fetchAMCConfigurations(filter, limit, offset);
  const amcConfigurations = response?.AmcConfigurations || [];

  return {
    amcConfigurations: formatAMCConfigurations(amcConfigurations),
    totalCount: response?.TotalCount !== undefined && response?.TotalCount !== null ? response.TotalCount : amcConfigurations.length,
  };
};

const useAMCConfiguration = (queryFilter = {}, limit = 10, offset = 0) => {

  const { tenantId, ids, projectIds } = queryFilter;

  const filter = {
    searchCriteria: {
      tenantId: tenantId || Digit.ULBService.getCurrentTenantId(),
    },
  };

  if (ids?.length) {
    filter.searchCriteria.ids = ids;
  }

  if (projectIds?.length) {
    filter.searchCriteria.projectIds = projectIds;
  }

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["AMC_CONFIGURATION", filter, limit, offset],
    () => fetchAMCConfigurations(filter, limit, offset),
    { keepPreviousData: true }
  );

  return {
    isLoading, isError, error, data,
    revalidate: async () => {
      await queryClient.invalidateQueries(["AMC_CONFIGURATION"]);
      return queryClient.getQueryData(["AMC_CONFIGURATION", filter, limit, offset]);
    }
  }
}

export default useAMCConfiguration;
