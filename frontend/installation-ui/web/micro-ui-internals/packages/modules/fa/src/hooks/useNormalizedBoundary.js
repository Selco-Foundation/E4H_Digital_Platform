import { useQuery, useQueryClient } from "react-query";
import { BoundaryService } from "../services/Boundary";

const fetchBoundaryTableRows = async (filter, limit = 10, offset = 0) => {
  const boundaryResponse = await BoundaryService.fetchAllBoundaries(filter, limit, offset);

  const normalized = (boundaryResponse?.Boundary ||  []).map((r) => ({
    countryCode: r?.country || "in",
    stateCode: r?.state || "",
    stateName: r?.state || "",
    districtCode: r?.district || "",
    districtName: r?.district || "",
    blockCode: r?.block || "",
    blockName: r?.block || "",
    code: r?.code || r?.block || "",
  }));

  return {
    totalCount: boundaryResponse?.TotalCount,
    boundaries: normalized,
  };
};

const useNormalizedBoundary = (queryFilter, limit = 10, offset = 0) => {

  const { boundary, boundaryFilterQuery } = queryFilter;

  const filter = {
    criteria: {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      hierarchyType: "SELCO",
    },
  };

  if (boundary?.tenantId) {
    filter.criteria.tenantId = boundary.tenantId;
  }

  if (boundary?.hierarchyType) {
    filter.criteria.hierarchyType = boundary.hierarchyType;
  }

  if (boundary?.boundaryType) {
    filter.criteria.boundaryType = boundary.boundaryType;
  }

  if (boundaryFilterQuery?.boundary?.length) {
    filter.criteria.parentCodes = boundaryFilterQuery?.boundary;
  }

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["NORMALIZED_BOUNDARY", filter, limit, offset],
    () => fetchBoundaryTableRows(filter, limit, offset)
  );

  return {
    isLoading,
    isError,
    error,
    data,
    revalidate: () => queryClient.invalidateQueries(["NORMALIZED_BOUNDARY"]),
  };
};

export default useNormalizedBoundary;