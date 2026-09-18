import { useQuery, useQueryClient } from "react-query";
import { AMCConfigurationService } from "../services/AMCConfiguration";

const formatConfigurations = (amcConfigurations) => {
  return amcConfigurations?.map((amcConfiguration) => ({
    id: amcConfiguration?.id,
    facilityName: amcConfiguration?.facility?.facility_name,
    facilityId: amcConfiguration?.facility?.facility_id,
    projectName: amcConfiguration?.project?.name,
    assetTypes: amcConfiguration?.assetTypes || [],
    durationMonths: amcConfiguration?.durationMonths,
    visitFrequencyMonths: amcConfiguration?.visitFrequencyMonths,
    configurationStartDate: amcConfiguration?.configurationStartDate,
    configurationEndDate: amcConfiguration?.configurationEndDate,
    status: amcConfiguration?.status,
  }));
}

const fetchAMCConfigurations = async (filter, limit, offset) => {
  const response = await AMCConfigurationService.fetchAMCConfigurations(filter, limit, offset);
  return {
    amcConfigurations: formatConfigurations(response?.AmcConfigurations),
    totalCount: response?.TotalCount,
  };
}

const useAMCConfigurationList = (queryFilter = {}, limit = 10, offset = 0) => {

  const { tenantId, facilityIds, ids, projectIds } = queryFilter;

  const filter = {
    searchCriteria: {
      tenantId: Digit.ULBService.getCurrentTenantId(),
    }
  };

  if (tenantId) {
    filter.searchCriteria.tenantId = tenantId;
  }

  if (facilityIds?.length) {
    filter.searchCriteria.facilityIds = facilityIds;
  }

  if (ids?.length) {
    filter.searchCriteria.ids = ids;
  }

  if (projectIds?.length) {
    filter.searchCriteria.projectIds = projectIds;
  }

  const queryClient = useQueryClient();
  const { isLoading, isFetching, isError, error, data } = useQuery(
    ["AMC_CONFIGURATION_LIST", filter, limit, offset],
    () => fetchAMCConfigurations(filter, limit, offset)
  );

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["AMC_CONFIGURATION_LIST"])
  }
}

export default useAMCConfigurationList;
