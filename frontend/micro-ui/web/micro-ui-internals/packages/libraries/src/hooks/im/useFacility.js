import { useQuery, useQueryClient } from "react-query";

const fetchFacilities = async (boundaryCodes) => {
  const queryFilter = {
    tenantId : ["in"],
    boundaryCodes: boundaryCodes,
    isOnmReady: true,
    sendNonPaginatedResponse: true,
  }

  const facilityResponse = await Digit.FacilityService.fetchFacilities(queryFilter);

  return {
    facilities: facilityResponse?.facilities?.map(facility => ({
      boundaryCode: facility.boundaryCode,
      facilityId: facility.facility_id,
    })) || [],
    total: facilityResponse?.totalCount,
  };
}

const useFacility = (codes) => {

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["FACILITY", codes],
    () => fetchFacilities(codes)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["FACILITY"])
  }

}

export default useFacility;