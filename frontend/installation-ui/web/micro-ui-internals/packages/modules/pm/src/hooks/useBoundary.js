import { useQuery, useQueryClient } from "react-query";
import {BoundaryService} from "../services/Boundary";
import { boundariesForRole } from "../utilities/jurisdictionScope";
import { MODULE_ROLE } from "../constants/Roles";

/**
 * Every node of one level, wherever it sits in the response tree.
 *
 * Matched on `boundaryType` rather than on depth because the two calls behind this hook return
 * different roots: asking for a level gets that level at the top, while asking about specific
 * boundaries returns them wrapped in their ancestors, rooted at the Country. Reading the top
 * level positionally - as this did - would file a Country away as a State the moment anything
 * asked about a district.
 */
const collectLevel = (boundaries, boundaryType, found = []) => {
  (boundaries || []).forEach((boundary) => {
    if (boundary?.boundaryType === boundaryType) {
      found.push(boundary);
      return;
    }
    collectLevel(boundary?.children, boundaryType, found);
  });
  return found;
};

const childrenOfType = (boundary, boundaryType) => {
  return (boundary?.children || []).filter((child) => child?.boundaryType === boundaryType);
};

const fetchBoundaries = async (boundaryType, codes) => {

  const boundaryResponse = await BoundaryService.fetchBoundaryRelations(boundaryType, codes);

  const states = [];
  const districts = [];
  const blocks = [];

  collectLevel(boundaryResponse?.TenantBoundary?.[0]?.boundary, "State").forEach((stateObject) => {
    states.push({
      code: stateObject?.code,
    });

    childrenOfType(stateObject, "District").forEach((district) => {
      districts.push({
        code: district?.code,
        stateCode: stateObject?.code,
      });
      childrenOfType(district, "Block").forEach((block) => {
        blocks.push({
          code: block?.code,
          districtCode: district?.code,
          stateCode: stateObject?.code
        });
      });
    });
  });

  return {
    states,
    districts,
    blocks,
  }
}

/**
 * Boundaries for naming and for picking, unnarrowed.
 *
 * Most callers are filter bars that use this as a lookup table and then intersect it with the
 * geography already stored on the record they are showing - a field plan's districts, an
 * assessment plan's blocks. Those must keep seeing every boundary: a plan can legitimately span
 * an area the signed-in user holds no jurisdiction over, and narrowing here would empty their
 * filters with no error to explain it.
 *
 * Screens that let the user *choose* a new boundary want the opposite, and ask for it by name -
 * see `useJurisdictionBoundary` below.
 */
const useBoundary = (boundaryType, codes) => {

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["BOUNDARY", boundaryType, codes],
    () => fetchBoundaries(boundaryType, codes)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["BOUNDARY"])
  }

}

/**
 * The boundaries this user may create work in: the jurisdictions they hold as a Project Manager.
 *
 * Used by the create wizards, where the dropdown is an authoring choice rather than a way to read
 * back what is already stored. Narrowing it keeps a PM from scheduling work into an area that is
 * not theirs by accident - it is a convenience and a guard, not a privilege boundary, since the
 * same call can still be made directly with any boundary.
 *
 * Null grants mean the user's jurisdictions are unknown (an older session, or HRMS was down at
 * login), and nothing is narrowed. Showing everything is the right failure; an empty state
 * dropdown would block the wizard outright.
 */
export const useJurisdictionBoundary = () => {
  return useBoundary("State", boundariesForRole(MODULE_ROLE) || undefined);
};

export default useBoundary;
