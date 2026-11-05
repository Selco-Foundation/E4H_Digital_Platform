import { useQuery, useQueryClient } from "react-query";
import { BoundaryService } from "../services/Boundary";
import { boundariesForRole } from "../utilities/jurisdictionScope";

/** The role this module gates on - see Module.js. The module's scope is this role's jurisdictions. */
const FACILITY_ADMIN_ROLE = "FACILITY_ADMIN";

const fetchBoundaries = async (queryFilter) => {

  const boundaryResponse = await BoundaryService.fetchBoundaryRelations(queryFilter);

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
 * The boundaries this module may work with.
 *
 * With no codes it no longer means "the whole country": it means the jurisdictions the signed-in
 * user holds as a Facility Administrator. Narrowing here rather than at each call site covers every
 * picker and filter in the module at once, and keeps the role out of the screens themselves.
 *
 * An explicit `codes` argument still wins - a caller asking for a specific subtree knows what it
 * wants. And `boundariesForRole` answers null when the user's grants are unknown (an older session,
 * or HRMS was down at login), in which case nothing is narrowed and the module behaves as before.
 * Showing everything is the right failure here; showing nothing would look like data loss.
 */
const useBoundary = (codes) => {

  const scopedCodes = codes || boundariesForRole(FACILITY_ADMIN_ROLE);

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
    ["BOUNDARY", queryFilter],
    () => fetchBoundaries(queryFilter)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["BOUNDARY"])
  }

}

export default useBoundary;