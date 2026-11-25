import { useQuery, useQueryClient } from "react-query";
import { FacilityService } from "../services/Facility";
import useJurisdictions from "./useJurisdictions";
import { buildFacilitySearchCriteria, toFacilityRow } from "../utilities/FacilityList";

const fetchFacilities = async (queryFilter) => {

  const facilityResponse = await FacilityService.fetchFacilities(queryFilter);

  return {
    facilities: facilityResponse?.facilities?.map(toFacilityRow) || [],
    total: facilityResponse?.totalCount,
  };
};

/** The facilities this user may see, narrowed by whatever they have filtered and searched. */
const useFacility = (filter, limit = 10, offset = 0) => {

  const queryFilter = {
    ...buildFacilitySearchCriteria(filter, Digit.ULBService.getCurrentTenantId()),
    limit,
    offset,
  };

  // In the query key, not in the request: the server derives the ceiling from these itself, so a
  // grant added or removed changes the answer without changing anything we send. Without them a
  // page load that picks up a new grant would keep serving the pre-refresh list.
  const jurisdictions = useJurisdictions();

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["FACILITY", queryFilter, jurisdictions],
    () => fetchFacilities(queryFilter)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["FACILITY"])
  }
}

export default useFacility;
