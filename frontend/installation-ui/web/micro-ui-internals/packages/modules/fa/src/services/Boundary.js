import {Request} from "@egovernments/digit-ui-libraries";

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

  fetchAllBoundaries: async (queryFilter, limit = 10, offset = 0) => {
    const endpoint = "/boundary-service/boundary/v2/getAllBoundaries";
    const headers = {
      "Content-Type": "application/json",
    };
    const params = {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      offset,
      limit,
    };

    return await Request({
      url: endpoint,
      data: queryFilter,
      userService: true,
      method: "POST",
      auth: true,
      params: params,
      headers: headers,
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
