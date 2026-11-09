import { useQuery, useQueryClient } from "react-query";
import { BoundaryService } from "../services/Boundary";
import { boundariesForRole } from "../utilities/jurisdictionScope";
import { VENDOR_ADMIN } from "../constants/Roles";

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
 * The boundaries the org user form may assign.
 *
 * Its only caller is the jurisdiction picker, so narrowing here narrows exactly the thing that
 * should be narrowed: a vendor administrator can only give someone an area they administer
 * themselves. `includeChildren` means their own boundaries and everything beneath them, which is
 * the whole allowed set - a district administrator can still assign the blocks in their district.
 *
 * A platform administrator holds no VENDOR_ADMIN grants, so `boundariesForRole` answers null and
 * nothing is narrowed. That is the right outcome for them, and it is the same null that means
 * "we do not know this user's jurisdictions" for an older session - so do not turn null into an
 * empty list here, which would leave both of them unable to assign anything.
 *
 * The backend rejects an out-of-scope jurisdiction regardless. This only keeps the form from
 * offering one.
 */
const useBoundary = (codes) => {

  const scopedCodes = codes || boundariesForRole(VENDOR_ADMIN);

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