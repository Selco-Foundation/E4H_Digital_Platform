import { useQuery, useQueryClient } from "react-query";
import { AMCService } from "../services/AMC";

const formatDate = (timestamp) => {
  const date = new Date(timestamp);
  const month = String(date.getMonth() + 1).padStart(2, "0"); // months are 0-based
  const day = String(date.getDate()).padStart(2, "0");
  const year = date.getFullYear();
  return `${month}/${day}/${year}`;
};

const formatMonths = (months) => {
  if (!months) return "";
  if (months >= 12) return `${months / 12} ${months / 12 > 1 ? "Years" : "Year"}`;
  return `${months} ${months > 1 ? "Months" : "Month"}`;
};

const fetchAMCAssetSerialNumbers = async (amcConfigurationId) => {
  const queryFilter = {
    searchCriteria: {
      amcConfigurationIds: [amcConfigurationId],
      tenantId: Digit.ULBService.getCurrentTenantId(),
    },
  };

  const amcAssetsResponse = await AMCService.fetchAMCAssets(queryFilter, 100, 0);

  return (amcAssetsResponse?.AssetAmcs || [])
    .map((assetData) => assetData?.asset?.serialNumber)
    .filter((serialNumber) => serialNumber);
}

const formatAMCConfigurations = async (amcConfigurations) => {
  if (!amcConfigurations?.length) return [];

  const formattedAMCConfigurations = [];

  for(let amcConfiguration of amcConfigurations) {
    formattedAMCConfigurations.push({
      id: amcConfiguration?.id,
      projectId: amcConfiguration?.projectId,
      projectName: amcConfiguration?.project?.name,
      amcStartDate: formatDate(amcConfiguration?.configurationStartDate),
      vendorId: amcConfiguration?.vendorId,
      vendorName: amcConfiguration?.vendor?.name,
      duration: formatMonths(amcConfiguration?.durationMonths),
      frequency: formatMonths(amcConfiguration?.visitFrequencyMonths),
      status: amcConfiguration?.status,
      completedVisits: `${amcConfiguration?.completedVisits}`,
      totalVisits: `${amcConfiguration?.totalVisits}`,
      assetSerialNumbers: await fetchAMCAssetSerialNumbers(amcConfiguration?.id),
    });
  }

  return formattedAMCConfigurations;
};

const fetchAMCConfigurations = async (filter, limit, offset) => {
  const amcConfigurationsResponse = await AMCService.fetchAMCConfigurations(filter, limit, offset);
  return {
    amcConfigurations: await formatAMCConfigurations(amcConfigurationsResponse.AmcConfigurations),
    totalCount: amcConfigurationsResponse.TotalCount,
  };
};

const useAMCConfiguration = (amcConfigurationQueryFilter, pageSize, pageOffset) => {
  const { facility } = amcConfigurationQueryFilter;

  const filter = {
    searchCriteria: {
      tenantId: Digit.ULBService.getCurrentTenantId(),
    },
  };

  if (facility?.facilityId?.length) {
    filter.searchCriteria.facilityIds = facility.facilityId;
  }

  const limit = pageSize || 10;
  const offset = pageOffset || 0;

  const queryClient = useQueryClient();
  const { isLoading, isFetching, isError, error, data } = useQuery(
    ["AMC", filter, limit, offset],
    () => fetchAMCConfigurations(filter, limit, offset)
  );

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["AMC"]),
  };
};

export default useAMCConfiguration;
