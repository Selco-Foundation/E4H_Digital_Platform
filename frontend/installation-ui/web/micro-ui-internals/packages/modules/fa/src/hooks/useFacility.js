import { useQuery, useQueryClient } from "react-query";
import { FacilityService } from "../services/Facility";
import useBoundary from "./useBoundary";

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

const useFacility = (filter, limit = 10, offset = 0) => {

  const { facilityFilterQuery } = filter;

  /*
   * The facilities this user may see, resolved to one level.
   *
   * `useBoundary` is already scoped to the module's role, so its `facilities` are exactly the
   * leaves of the user's jurisdictions - whether they were granted a state, a block or a single
   * facility. Resolving to facility codes avoids sending several level filters at once: a user
   * holding a state in one place and a block in another would otherwise produce `state=S AND
   * block=B`, which matches nothing.
   *
   * react-query serves this from the same cache entry the pickers use, so it costs no extra call.
   */
  const { data: scope } = useBoundary();
  const scopedFacilityCodes = scope?.facilities?.map((facility) => facility.code);

  const queryFilter = {
    tenantId: [Digit.ULBService.getCurrentTenantId()],
    limit,
    offset,
  };

  if (facilityFilterQuery?.state?.length) {
    queryFilter.state = facilityFilterQuery.state;
  }

  if (facilityFilterQuery?.district?.length) {
    queryFilter.district = facilityFilterQuery.district;
  }

  if (facilityFilterQuery?.block?.length) {
    queryFilter.block = facilityFilterQuery.block;
  }

  if (facilityFilterQuery?.facility?.length) {
    // The picker only offers facilities already inside the user's scope, so their choice is a
    // narrowing of it rather than a way out of it.
    queryFilter.boundaryCodes = facilityFilterQuery.facility;
  } else if (scopedFacilityCodes?.length) {
    queryFilter.boundaryCodes = scopedFacilityCodes;
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