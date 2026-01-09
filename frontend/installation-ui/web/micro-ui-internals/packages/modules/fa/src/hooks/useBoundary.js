import { useQuery, useQueryClient } from "react-query";
import { BoundaryService } from "../services/Boundary";

const asCodesKey = (codes) => {
  if (!codes) return "";
  if (Array.isArray(codes)) return codes.filter(Boolean).join(",");
  return String(codes);
};

const fetchBoundaryHierarchy = async (boundaryType, codes) => {
  const boundaryResponse = await BoundaryService.fetchBoundaryRelations(boundaryType, codes);

  const states = [];
  const districts = [];
  const blocks = [];

  const top = boundaryResponse?.TenantBoundary?.[0]?.boundary || [];

  if (!boundaryType || boundaryType === "State") {
    top.forEach((stateObject) => {
      const stateCode = stateObject?.code;
      if (stateCode) states.push({ code: stateCode });

      (stateObject?.children || []).forEach((district) => {
        const districtCode = district?.code;
        if (districtCode) {
          districts.push({
            code: districtCode,
            stateCode,
          });
        }

        (district?.children || []).forEach((block) => {
          const blockCode = block?.code;
          if (blockCode) {
            blocks.push({
              code: blockCode,
              districtCode,
              stateCode,
            });
          }
        });
      });
    });

    return { states, districts, blocks };
  }

  if (boundaryType === "District") {
    const stateFromCodes = Array.isArray(codes) ? codes?.[0] : codes;

    top.forEach((districtObj) => {
      const districtCode = districtObj?.code;
      if (!districtCode) return;

      let stateCode = stateFromCodes || "";
      const parents = districtObj?.parents || districtObj?.parent || [];
      const parentsArr = Array.isArray(parents) ? parents : [parents];
      const maybeState = parentsArr.find((p) => p?.boundaryType === "State") || parentsArr[0];
      if (maybeState?.code) stateCode = maybeState.code;

      districts.push({ code: districtCode, stateCode });

      (districtObj?.children || []).forEach((blockObj) => {
        const blockCode = blockObj?.code;
        if (!blockCode) return;
        blocks.push({
          code: blockCode,
          districtCode,
          stateCode,
        });
      });
    });

    return { states, districts, blocks };
  }

  return { states, districts, blocks };
};

const fetchBoundaryTableRows = async (boundaryType = "Block", limit = 10, offset = 0) => {
  const res = await BoundaryService.fetchAllBoundaries({
    boundaryType,
    page: 0,
    size: 1000,
  });

  const list = Array.isArray(res) ? res : [];

  const normalized = list.map((r) => ({
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
    totalCount: normalized.length,
    boundaries: normalized.slice(offset, offset + limit),
  };
};

const useBoundary = (boundaryType, codes, limit, offset, queryOptions = {}) => {
  const queryClient = useQueryClient();
  const isTableMode = typeof limit === "number" && typeof offset === "number";

  const codesKey = asCodesKey(codes);
  const queryKey = isTableMode
    ? ["BOUNDARY_TABLE", boundaryType, limit, offset]
    : ["BOUNDARY", boundaryType, codesKey];

  const queryFn = () =>
    isTableMode
      ? fetchBoundaryTableRows(boundaryType || "Block", limit, offset)
      : fetchBoundaryHierarchy(boundaryType, codes);

  const qOpts = queryOptions || {};
  const enabled = typeof qOpts.enabled === "boolean" ? qOpts.enabled : true;
  const finalOpts = Object.assign({}, qOpts, { enabled });

  const { isLoading, isError, error, data } = useQuery(queryKey, queryFn, finalOpts);

  return {
    isLoading,
    isError,
    error,
    data,
    revalidate: () => queryClient.invalidateQueries(isTableMode ? ["BOUNDARY_TABLE"] : ["BOUNDARY"]),
  };
};

export default useBoundary;