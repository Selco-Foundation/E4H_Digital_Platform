import {Request} from "@egovernments/digit-ui-libraries";
import {authHeaders} from "../components/Custom/CustomRequest";

const DEFAULT_COUNTRY = "India";

const normalizePart = (val) => {
  return (val || "").toString().trim().replace(/\s+/g, "_");
};

const startsWithIgnoreCase = (value, prefix) => {
  if (!value || !prefix) return false;
  return value.toLowerCase().indexOf(prefix.toLowerCase()) === 0;
};

const computeGeographyCodes = ({ country = DEFAULT_COUNTRY, state, district, block }) => {
  const c = normalizePart(country) || DEFAULT_COUNTRY;

  const stateRaw = normalizePart(state);
  const districtRaw = normalizePart(district);
  const blockRaw = normalizePart(block);

  const countryPrefix = c + "_";

  // STATE: ensure "India_<State>"
  let stateCode = stateRaw;
  if (stateCode && !startsWithIgnoreCase(stateCode, countryPrefix)) {
    stateCode = countryPrefix + stateCode;
  }

  // DISTRICT:
  // - if dropdown: already "India_State_District"
  // - if manual: "District" -> make "India_State_District"
  let districtCode = districtRaw;
  if (districtCode) {
    const statePrefix = stateCode + "_";

    if (stateCode && startsWithIgnoreCase(districtCode, statePrefix)) {
      // already "India_State_District"
    } else if (startsWithIgnoreCase(districtCode, countryPrefix) && districtCode.split("_").length >= 3) {
      // already full "India_*_*"
    } else if (stateCode) {
      districtCode = stateCode + "_" + districtCode;
    }
  }

  // BLOCK:
  // user enters "BlockName" -> make "India_State_District_BlockName"
  let blockCode = blockRaw;
  if (blockCode) {
    const districtPrefix = districtCode + "_";

    if (districtCode && startsWithIgnoreCase(blockCode, districtPrefix)) {
      // already full
    } else if (startsWithIgnoreCase(blockCode, countryPrefix) && blockCode.split("_").length >= 4) {
      // already full "India_*_*_*"
    } else if (districtCode) {
      blockCode = districtCode + "_" + blockCode;
    }
  }

  return {
    country: c,
    state: stateCode,
    district: districtCode,
    block: blockCode,
    code: blockCode, // code == full block code
    parent: stateCode, // parent == state (India_State) as requested
  };
};

export const BoundaryService = {
  computeGeographyCodes,
  fetchBoundaryRelations: async (boundaryType, codes) => {
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

    const headers = {"Content-Type": "application/json"};

    return await Request({
      url: endpoint,
      userService: true,
      method: "POST",
      auth: true,
      params,
      headers,
    });
  },

  fetchAllBoundaries: async ({
                               page = 0,
                               size = 10,
                               tenantId = "in",
                               hierarchyType = "SELCO",
                               boundaryType = "Block",
                             } = {}) => {
    return await Request({
      url: "/boundary-service/boundary/getAllBoundaries",
      userService: true,
      method: "GET",
      auth: true,
      params: {page, size, tenantId, hierarchyType, boundaryType},
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
        'tenantId': "in",
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
      headers: {"Content-Type": "application/json"},
    });
  },

  createBoundaryRelationship: async (relationshipData) => {
    return await Request({
      url: `/boundary-service/boundary-relationships/_create`,
      data: relationshipData,
      userService: true,
      method: "POST",
      auth: true,
      headers: {"Content-Type": "application/json"},
    });
  },
};
