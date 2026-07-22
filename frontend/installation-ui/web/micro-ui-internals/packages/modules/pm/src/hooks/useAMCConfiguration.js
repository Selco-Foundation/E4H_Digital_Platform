import { useQuery, useQueryClient } from "react-query";
import { AMCService } from "../services/AMC";

const getAMCGroupKey = (amcConfiguration) => {
  return [
    amcConfiguration?.projectId,
    amcConfiguration?.vendorId,
    amcConfiguration?.configurationStartDate,
    amcConfiguration?.configurationEndDate,
    amcConfiguration?.durationMonths,
    amcConfiguration?.visitFrequencyMonths,
    amcConfiguration?.status,
  ].join("|");
};

const formatAMCConfigurations = (amcConfigurations = []) => {
  const amcConfigurationGroupMap = new Map();

  amcConfigurations.forEach((amcConfiguration) => {
    const groupKey = getAMCGroupKey(amcConfiguration);
    const existingGroup = amcConfigurationGroupMap.get(groupKey);
    const facilityIds = new Set(existingGroup?.facilityIds || []);

    if (amcConfiguration?.facilityId) {
      facilityIds.add(amcConfiguration.facilityId);
    }

    amcConfigurationGroupMap.set(groupKey, {
      id: existingGroup?.id || amcConfiguration?.id,
      name: amcConfiguration?.project?.name || existingGroup?.name || "-",
      activities: [
        {
          code: "AMC",
          name: "AMC",
        },
      ],
      startDate: amcConfiguration?.configurationStartDate || existingGroup?.startDate,
      endDate: amcConfiguration?.configurationEndDate || existingGroup?.endDate,
      healthFacilityNumber: facilityIds.size,
      status: amcConfiguration?.status || existingGroup?.status,
      facilityIds: [...facilityIds],
    });
  });

  return [...amcConfigurationGroupMap.values()].map(({ facilityIds, ...amcConfiguration }) => amcConfiguration);
};

const fetchAMCConfigurations = async (filter, limit, offset) => {
  const response = await AMCService.fetchAMCConfigurations(filter, 100, 0);
  const formattedAMCConfigurations = formatAMCConfigurations(response?.AmcConfigurations);
  const paginatedAMCConfigurations = formattedAMCConfigurations.slice(offset, offset + limit);

  return {
    amcConfigurations: paginatedAMCConfigurations,
    totalCount: formattedAMCConfigurations.length,
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
    () => fetchAMCConfigurations(filter, limit, offset)
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
