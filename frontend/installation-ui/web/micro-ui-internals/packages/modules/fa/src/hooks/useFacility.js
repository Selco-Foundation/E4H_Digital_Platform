import { useQuery, useQueryClient } from "react-query";
import { FacilityService } from "../services/Facility";

const fetchFacilities = async (queryFilter) => {

  const facilityResponse = await FacilityService.fetchFacilities(queryFilter);

  return {
    facilities:
      facilityResponse?.facilities?.map((facility) => ({
        id: facility?.facility_id,
        facilityName: facility?.facility_name,
        pocName: facility?.facility_poc_name,
      })) || [],
    total: facilityResponse?.totalCount,
  };
};

const useFacility = (filter, limit = 10, offset = 0) => {

  const { facilityFilterQuery } = filter;

  const queryFilter = {
    tenantId: [Digit.ULBService.getCurrentTenantId()],
    limit,
    offset,
  };

  if (facilityFilterQuery?.boundary?.length) {
    queryFilter.boundaryCodes = facilityFilterQuery.boundary;
  }

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["FACILITY", queryFilter],
    () => fetchFacilities(queryFilter)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["FACILITY"])
  }
}

export default useFacility;