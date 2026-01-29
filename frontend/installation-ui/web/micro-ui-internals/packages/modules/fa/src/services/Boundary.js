import {Request} from "@egovernments/digit-ui-libraries";
import {authHeaders} from "../components/Custom/CustomRequest";

const DEFAULT_COUNTRY = "India";

const normalizePart = (val) => {
  return (val || "").toString().trim().replace(/\s+/g, "");
};

export const BoundaryService = {

  fetchBoundaryRelations: async (queryFilter) => {
    const endpoint = "/boundary-service/boundary-relationships/v2/_search";
    const headers = {
      "Content-Type": "application/json",
    };
    const data = {
      BoundaryRelationship: queryFilter,
    };

    return await Request({
      url: endpoint,
      userService: true,
      method: "POST",
      auth: true,
      data: data,
      headers: headers,
    });
  },

  computeGeographyCodes: ({ country = DEFAULT_COUNTRY, state, district, block, isStateTextMode, isDistrictTextMode }) => {
    const countryCode = normalizePart(country) || DEFAULT_COUNTRY;

    let stateCode, districtCode, blockCode;

    if (isStateTextMode) {
      stateCode = countryCode + "_" + normalizePart(state);
      districtCode = stateCode + "_" + normalizePart(district);
      blockCode = districtCode + "_" + normalizePart(block);
    } else if (isDistrictTextMode) {
      stateCode = state;
      districtCode = stateCode + "_" + normalizePart(district);
      blockCode = districtCode + "_" + normalizePart(block);
    } else {
      stateCode = state;
      districtCode = district;
      blockCode = districtCode + "_" + normalizePart(block);
    }

    return {
      country: countryCode,
      state: stateCode,
      district: districtCode,
      block: blockCode,
    };
  },

  fetchNormalizedBoundaryRelations: async (boundaryType, codes) => {
    const endpoint = "/boundary-service/boundary-relationships/_search";
    const normalizedCodes = Array.isArray(codes) ? codes.filter(Boolean) : codes ? [codes] : null;

    const params = {
      tenantId: "in",
      includeChildren: true,
      includeParents: boundaryType !== "State",
      hierarchyType: "SELCO",
      boundaryType,
    };

    if (normalizedCodes && normalizedCodes.length) {
      params.codes = normalizedCodes.join(",");
    }

    const headers = { "Content-Type": "application/json" };

    return await Request({
      url: endpoint,
      userService: true,
      method: "POST",
      auth: true,
      params,
      headers,
    });
  },

  fetchAllBoundaries: async ({ page = 0, size = 10, tenantId = "in", hierarchyType = "SELCO", boundaryType = "Block" } = {}) => {
    return await Request({
      url: "/boundary-service/boundary/getAllBoundaries",
      userService: true,
      method: "GET",
      auth: true,
      params: { page, size, tenantId, hierarchyType, boundaryType },
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
        tenantId: "in",
        ...authHeaders(),
      },
    });
  },

  createBoundary: async (boundaryData) => {
    return await Request({
      url: "/boundary-service/boundary/_create",
      userService: true,
      method: "POST",
      auth: true,
      data: boundaryData,
      headers: { "Content-Type": "application/json" },
    });
  },

  createBoundaryRelationship: async (relationshipData) => {
    return await Request({
      url: `/boundary-service/boundary-relationships/_create`,
      data: relationshipData,
      userService: true,
      method: "POST",
      auth: true,
      headers: { "Content-Type": "application/json" },
    });
  },
};
