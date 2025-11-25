import { useQuery, useQueryClient } from "react-query";

const fetchBoundaries = async (codes) => {
  const queryFilter = {
    tenantId : "in",
    includeChildren : true,
    includeParents : true,
    hierarchyType: "SELCO",
    codes: codes,
  }

  const boundaryResponse = await Digit.BoundaryService.fetchBoundaryRelations(queryFilter);

  const extractBoundaries = (boundaries) => {
    const compiledBoundaries = {};

    const compileBoundaries = (boundaries, parentCode, compiledObject) => {
      if (!boundaries?.length) return;
      for (let boundary of boundaries) {
        compiledObject[boundary?.boundaryType] = [...(compiledObject[boundary?.boundaryType] || []), {
          code: boundary?.code,
          parentCode: parentCode,
        }]
        compileBoundaries(boundary?.children, boundary?.code, compiledObject);
      }
    }

    compileBoundaries(boundaries, "", compiledBoundaries);
    return compiledBoundaries;
  }

  const {
    State: states,
    District: districts,
    Block: blocks,
    Facility: facilities,
  } = extractBoundaries(boundaryResponse?.TenantBoundary?.[0]?.boundary);

  return {
    states,
    districts,
    blocks,
    facilities,
  }
}

const useBoundary = (codes) => {

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["BOUNDARY", codes],
    () => fetchBoundaries(codes)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["BOUNDARY"])
  }

}

export default useBoundary;