import { useQuery, useQueryClient } from "react-query";
import { BoundaryUtil } from "../../utils/BoundaryUtil";
import useJurisdictions from "./useJurisdictions";

const fetchFacilities = async (filters, scopeRoles) => {
  const queryFilter = {
    tenantId : [Digit.ULBService.getCurrentTenantId()],
    ...filters,
    isOnmReady: true,
    sendNonPaginatedResponse: true,
  }

  const facilityResponse = await Digit.FacilityService.fetchFacilities(queryFilter, scopeRoles);

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
 * The boundary filter is sent only when the user has actually narrowed something. What callers
 * pass is `Jurisdiction.CurrentBoundary`, which starts life at login as a copy of the user's whole
 * jurisdiction and is only ever narrowed from there by ChangeCity - so "nothing selected" and
 * "everything selected" arrive here identically, and sending it unconditionally just echoed the
 * user's own HRMS grants back at the server that derived them. Worse, it echoed a *stale* copy:
 * micro-ui writes jurisdiction at login and never again, so a grant added mid-session would be
 * filtered out by the client even though the server allows it. The inbox already does this, in
 * `BoundaryUtil.explicitNarrowing`; facility search simply never got the same treatment.
 *
 * `onlyRoles` narrows the declared scope to one screen's role, for a page gated to it. It is part
 * of the query key: without that, ticket creation's COMPLAINANT-only list and the inbox's
 * all-IM-roles list share the same filters and would serve each other's cached answer - the
 * narrower one looking correct right up until the moment it is the wrong list.
 */
const useFacility = (jurisdictionBoundaries, onlyRoles) => {

  // Compared against the full set, not against "is it empty": ChangeCity puts the whole
  // jurisdiction back for "All", which must read as no narrowing rather than as a selection.
  const narrowing = BoundaryUtil.explicitNarrowing(jurisdictionBoundaries, Digit.SessionStorage.get("Jurisdiction.Boundaries"));
  const filters = BoundaryUtil.facilitySearchFilters(narrowing);
  const scopeRoles = Digit.FacilityService.imScopeRoles(onlyRoles);

  // In the key, not in the request: the server derives the ceiling from these itself, so they
  // change the answer without changing anything we send. Without them a refresh that picks up a
  // new grant would keep serving the pre-refresh list. react-query dedupes the lookup across
  // every caller of this hook, so asking for it here costs one request per page load.
  const jurisdictions = useJurisdictions();

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["FACILITY", filters, scopeRoles, jurisdictions],
    () => fetchFacilities(filters, scopeRoles)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["FACILITY"])
  }

}

export default useFacility;
