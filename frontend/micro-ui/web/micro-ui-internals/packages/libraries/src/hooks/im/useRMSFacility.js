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

  const jurisdictionCurrentBoundary = Digit.PersistantStorage.get("Jurisdiction.CurrentBoundary") || {
    country: ["-"],
  };

  const boundaryFieldsMap = {
    state: "state",
    district: "district",
    block: "block",
    facility: "boundaryCodes",
  };

  Object.keys(boundaryFieldsMap).forEach((key) => {
    const filterBoundaries = queryFilter?.filters?.[key]?.map((entity) => entity.code) || [];
    const jurisdictionBoundaries = jurisdictionCurrentBoundary[key] || [];
    const filterValues = [...new Set([...filterBoundaries, ...jurisdictionBoundaries])];
    if (filterValues.length) {
      filter.Facility[boundaryFieldsMap[key]] = filterValues;
    }
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
