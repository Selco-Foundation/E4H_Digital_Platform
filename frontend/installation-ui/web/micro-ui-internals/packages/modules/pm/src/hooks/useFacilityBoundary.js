import { useQuery, useQueryClient } from "react-query";
import { BoundaryService } from "../services/Boundary";
import { boundariesForRole } from "../utilities/jurisdictionScope";
import { MODULE_ROLE } from "../constants/Roles";

const fetchBoundaries = async (queryFilter) => {

  const boundaryResponse = await BoundaryService.fetchBoundaryHierarchy(queryFilter);

  const extractBoundaries = (boundaries) => {
    const compiledBoundaries = {};

    const compileBoundaries = (boundaries, parentCode, compiledObject) => {
      if (!boundaries?.length) return;
      for (let boundary of boundaries) {
        const existingBoundaries = compiledObject[boundary?.boundaryType] || [];
        if (!existingBoundaries.some((boundaryData) => boundaryData?.code === boundary?.code)) {
          compiledObject[boundary?.boundaryType] = [
            ...existingBoundaries,
            {
              code: boundary?.code,
              parentCode: parentCode,
            },
          ];
          compileBoundaries(boundary?.children, boundary?.code, compiledObject);
        }
      }
    };

    compileBoundaries(boundaries, "", compiledBoundaries);
    return compiledBoundaries;
  };

  const { Country: countries, State: states, District: districts, Block: blocks, Facility: facilities } = extractBoundaries(
    boundaryResponse?.TenantBoundary?.[0]?.boundary
  );

  return {
    countries,
    states,
    districts,
    blocks,
    facilities,
  };
};

/**
 * The boundaries behind the facility filter bar.
 *
 * With no codes it no longer means "the whole country": it means the jurisdictions the signed-in
 * user holds as a Project Manager. This has to match what `useFacility` asks the backend for -
 * a filter bar wider than the list behind it offers states that return nothing, which reads as a
 * broken screen rather than as a boundary the user has no claim to.
 *
 * An explicit `codes` argument still wins, and `boundariesForRole` answers null when the user's
 * grants are unknown (an older session, or HRMS was down at login), in which case nothing is
 * narrowed. Showing everything is the right failure here; showing nothing would look like data
 * loss.
 */
const useFacilityBoundary = (codes) => {

  const scopedCodes = codes || boundariesForRole(MODULE_ROLE);

  const queryFilter = {
    tenantId: Digit.ULBService.getCurrentTenantId(),
    includeChildren: true,
    includeParents: true,
    hierarchyType: "SELCO",
  };

  if (scopedCodes) {
    queryFilter.codes = scopedCodes;
  }

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["FACILITY_BOUNDARY", queryFilter],
    () => fetchBoundaries(queryFilter)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["FACILITY_BOUNDARY"])
  }

}

export default useFacilityBoundary;
