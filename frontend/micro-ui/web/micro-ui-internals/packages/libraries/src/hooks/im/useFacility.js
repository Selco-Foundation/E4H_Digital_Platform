import { useQuery, useQueryClient } from "react-query";
import { BoundaryUtil } from "../../utils/BoundaryUtil";

const fetchFacilities = async (filters) => {
  const queryFilter = {
    tenantId : [Digit.ULBService.getCurrentTenantId()],
    ...filters,
    isOnmReady: true,
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

/**
 * The facilities in the caller's jurisdiction.
 *
 * Takes the jurisdiction as stored at login - keyed by the level each boundary was granted at -
 * and sends it at that level. Callers used to walk the boundary tree down to every facility
 * beneath their jurisdiction and send those codes instead, which made the request body as large
 * as the user's area and repeated an expansion facility search already does.
 */
const useFacility = (jurisdictionBoundaries) => {

  const filters = BoundaryUtil.facilitySearchFilters(jurisdictionBoundaries);

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["FACILITY", filters],
    () => fetchFacilities(filters)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["FACILITY"])
  }

}

export default useFacility;
