import {Request} from "@egovernments/digit-ui-libraries";
import {authHeaders} from "../components/Custom/CustomRequest";

export const BoundaryService = {
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
};