import { useQuery, useQueryClient } from "react-query";
import { BoundaryService } from "../services/Boundary";

const asCodesKey = (codes) => {
  if (!codes) return "";
  if (Array.isArray(codes)) return codes.filter(Boolean).join(",");
  return String(codes);
};

const asArrayKey = (arr) => {
  if (!Array.isArray(arr) || arr.length === 0) return "";
  return arr.filter(Boolean).join(",");
};

const fetchBoundaryHierarchy = async (boundaryType, codes) => {
  const boundaryResponse = await BoundaryService.fetchNormalizedBoundaryRelations(boundaryType, codes);

  const states = [];
  const districts = [];
  const blocks = [];

  const top = boundaryResponse?.TenantBoundary?.[0]?.boundary || [];

  if (!boundaryType || boundaryType === "State") {
    top.forEach((stateObject) => {
      const stateCode = stateObject?.code;
      if (stateCode) states.push({ code: stateCode, parentCode: "" });

      (stateObject?.children || []).forEach((district) => {
        const districtCode = district?.code;
        if (districtCode) {
          districts.push({
            code: districtCode,
            stateCode,
            parentCode: stateCode,
          });
        }

        (district?.children || []).forEach((block) => {
          const blockCode = block?.code;
          if (blockCode) {
            blocks.push({
              code: blockCode,
              districtCode: districtCode,
              stateCode,
              parentCode: districtCode,
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

      districts.push({ code: districtCode, stateCode, parentCode: stateCode });

      (districtObj?.children || []).forEach((blockObj) => {
        const blockCode = blockObj?.code;
        if (!blockCode) return;
        blocks.push({
          code: blockCode,
          districtCode,
          stateCode,
          parentCode: districtCode,
        });
      });
    });

    return { states, districts, blocks };
  }

  return { states, districts, blocks };
};

const fetchBoundaryTableRows = async (boundaryType = "Block", limit = 10, offset = 0, tableFilter = {}) => {
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

  const stateCodes = Array.isArray(tableFilter?.stateCodes) ? tableFilter.stateCodes : [];
  const districtCodes = Array.isArray(tableFilter?.districtCodes) ? tableFilter.districtCodes : [];
  const blockCodes = Array.isArray(tableFilter?.blockCodes) ? tableFilter.blockCodes : [];

  let filtered = normalized;

  // precedence: Block > District > State
  if (blockCodes.length > 0) filtered = filtered.filter((r) => blockCodes.includes(r.blockCode));
  else if (districtCodes.length > 0) filtered = filtered.filter((r) => districtCodes.includes(r.districtCode));
  else if (stateCodes.length > 0) filtered = filtered.filter((r) => stateCodes.includes(r.stateCode));

  return {
    totalCount: filtered.length,
    boundaries: filtered.slice(offset, offset + limit),
  };
};

const useNormalizedBoundary = (boundaryType, codes, limit, offset, queryOptions = {}) => {
  const queryClient = useQueryClient();
  const isTableMode = typeof limit === "number" && typeof offset === "number";

  const qOptsRaw = queryOptions || {};
  const tableFilter = qOptsRaw?._tableFilter || {};

  // remove custom option so react-query doesn't get unknown keys
  const qOpts = { ...qOptsRaw };
  if (qOpts._tableFilter !== undefined) delete qOpts._tableFilter;

  const codesKey = asCodesKey(codes);

  const tableFilterKey = isTableMode
    ? [asArrayKey(tableFilter?.stateCodes), asArrayKey(tableFilter?.districtCodes), asArrayKey(tableFilter?.blockCodes)].join("|")
    : "";

  const queryKey = isTableMode
    ? ["BOUNDARY_TABLE", boundaryType, limit, offset, tableFilterKey]
    : ["BOUNDARY", boundaryType, codesKey];

  const queryFn = () =>
    isTableMode ? fetchBoundaryTableRows(boundaryType || "Block", limit, offset, tableFilter) : fetchBoundaryHierarchy(boundaryType, codes);

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

export default useNormalizedBoundary;