import { useQuery, useQueryClient } from "react-query";
import { BoundaryService } from "../services/Boundary";

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

const useBoundary = (codes) => {

  const queryFilter = {
    tenantId: Digit.ULBService.getCurrentTenantId(),
    includeChildren: true,
    includeParents: true,
    hierarchyType: "SELCO",
  };

  if (codes) {
    queryFilter.codes = codes;
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