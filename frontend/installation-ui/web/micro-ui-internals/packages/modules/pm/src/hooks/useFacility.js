import { useQuery, useQueryClient } from "react-query";
import { FacilityService } from "../services/Facility";
import useJurisdictions from "./useJurisdictions";

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
 * Sets one filter from the user's own choice, or leaves it unset.
 *
 * The user's own jurisdiction is deliberately not sent as a fallback. Facility search is scoped
 * on the server from the `x-acting-role` header this module sends and the caller's HRMS grants,
 * so echoing those same grants back narrowed nothing the server was not already narrowing. What
 * it could do is narrow *more*: a stale or half-written client copy silently dropped rows the
 * user is entitled to see, and that failure looks identical to having no such facilities.
 *
 * The pickers only offer values already inside the user's scope, so a chosen value narrows within
 * the server's ceiling rather than escaping it.
 */
const applyFilter = (queryFilter, key, chosen) => {
  if (chosen?.length) queryFilter[key] = chosen;
};

/**
 * The facilities this user may see, narrowed by whatever they have filtered.
 *
 * With no filters this now sends no boundary criteria at all and lets the server scope the list.
 * That makes the server the only thing scoping it: with `facility.jurisdiction.scope.mode` set to
 * `off` or `warn`, this search returns every facility in the country.
 */
const useFacility = (filter, limit = 10, offset = 0) => {

  const { facilityFilterQuery } = filter;

  const queryFilter = {
    tenantId: [Digit.ULBService.getCurrentTenantId()],
    limit,
    offset,
  };

  applyFilter(queryFilter, "state", facilityFilterQuery?.state);
  applyFilter(queryFilter, "district", facilityFilterQuery?.district);
  applyFilter(queryFilter, "block", facilityFilterQuery?.block);
  applyFilter(queryFilter, "boundaryCodes", facilityFilterQuery?.facility);

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
