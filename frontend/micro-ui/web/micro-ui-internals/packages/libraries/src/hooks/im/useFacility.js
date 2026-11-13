import { useQuery, useQueryClient } from "react-query";

const fetchFacilities = async (boundaryCodes, includeAllMappedFacilities) => {
  const queryFilter = {
    tenantId : [Digit.ULBService.getCurrentTenantId()],
    boundaryCodes: boundaryCodes,
    ...(!includeAllMappedFacilities ? { isOnmReady: true } : {}),
    sendNonPaginatedResponse: true,
  }

  const facilityResponse = await Digit.FacilityService.fetchFacilities(queryFilter);

  return {
    facilities:
      facilityResponse?.facilities?.map((facility) => ({
        boundaryCode: facility.boundaryCode,
        facilityId: facility.facility_id,
        facilityStatus: facility.facility_status,
        facilityName: facility.facility_name,
      })) || [],
    total: facilityResponse?.totalCount,
  };
}

const useFacility = (codes, { includeAllMappedFacilities = false } = {}) => {

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["FACILITY", codes, includeAllMappedFacilities],
    () => fetchFacilities(codes, includeAllMappedFacilities)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["FACILITY"])
  }

}

export default useFacility;
