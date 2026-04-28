import { useQuery, useQueryClient } from "react-query";

const fetchRMSPausedFacilities = async (queryFilter, limit, offset) => {

  const rmsPausedFacilityResponse = await Digit.RMSService.fetchRMSPausedFacilities(queryFilter, limit, offset);

  return {
    rmsPausedFacilities:
      rmsPausedFacilityResponse?.pausedFacilities?.map((facility) => ({
        id: facility.id,
        boundaryCode: facility.boundaryCode,
        facilityName: facility.facilityName,
        facilityId: facility.facilityId,
        pausedUntil: facility.pausedUntil,
        pausedBy: facility.pausedBy,
      })) || [],
    total: rmsPausedFacilityResponse?.totalCount,
  };
};

const useRMSPausedFacility = (queryFilter, limit = 10, offset = 0) => {

  const filter = {
    Facility: {
      tenantId: [Digit.ULBService.getCurrentTenantId()],
      limit,
      offset,
    },
  };

  const jurisdictionCurrentBoundary = Digit.SessionStorage.get("Jurisdiction.CurrentBoundary") || {
    country: ["-"],
  };

  Object.keys(jurisdictionCurrentBoundary).forEach((key) => {
    filter.Facility[key] = [...(queryFilter?.filter?.[key] || []), ...jurisdictionCurrentBoundary[key]];
  })

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["RMS_PAUSED_FACILITY", filter, limit, offset],
    () => fetchRMSPausedFacilities(filter, limit, offset)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["RMS_PAUSED_FACILITY"]),
  };
};

export default useRMSPausedFacility;
