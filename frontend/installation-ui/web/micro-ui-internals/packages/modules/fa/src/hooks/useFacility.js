import { useQuery, useQueryClient } from "react-query";
import { FacilityService } from "../services/Facility";
import { boundariesByLevelForRole } from "../utilities/jurisdictionScope";
import { MODULE_ROLE } from "../constants/Roles";

const fetchFacilities = async (queryFilter) => {

  const facilityResponse = await FacilityService.fetchFacilities(queryFilter);

  return {
    facilities:
      facilityResponse?.facilities?.map((facility) => ({
        id: facility?.facility_id,
        facilityName: facility?.facility_name,
        pocName: facility?.facility_poc_name,
        state: facility?.boundary?.state,
        district: facility?.boundary?.district,
        block: facility?.boundary?.block,
      })) || [],
    total: facilityResponse?.totalCount,
  };
};

/**
 * Sets one filter, preferring the user's own choice over their jurisdiction at that level.
 *
 * The pickers only offer values already inside the user's scope, so a chosen value narrows the
 * scope rather than escaping it - which is why the choice replaces the scope outright instead of
 * being combined with it. Nothing is set when neither is present, so an unscoped user with no
 * filters sends no boundary criteria at all.
 */
const applyFilter = (queryFilter, key, chosen, scoped) => {
  const value = chosen?.length ? chosen : scoped;
  if (value?.length) queryFilter[key] = value;
};

const useFacility = (filter, limit = 10, offset = 0) => {

  const { facilityFilterQuery } = filter;

  /*
   * The facilities this user may see, as the levels their jurisdictions were granted at.
   *
   * Sent per level rather than resolved down to facility codes: facility search already expands
   * `state`, `district` and `block` through boundary-service, so one state code does the work of
   * every facility code beneath it, and the lower levels stay free for the user's own filters.
   *
   * Null means the user holds no grant for this role, or holds the whole country - either way,
   * nothing is narrowed.
   */
  const scope = boundariesByLevelForRole(MODULE_ROLE);

  const queryFilter = {
    tenantId: [Digit.ULBService.getCurrentTenantId()],
    limit,
    offset,
  };

  applyFilter(queryFilter, "state", facilityFilterQuery?.state, scope?.State);
  applyFilter(queryFilter, "district", facilityFilterQuery?.district, scope?.District);
  applyFilter(queryFilter, "block", facilityFilterQuery?.block, scope?.Block);
  applyFilter(queryFilter, "boundaryCodes", facilityFilterQuery?.facility, scope?.Facility);

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
