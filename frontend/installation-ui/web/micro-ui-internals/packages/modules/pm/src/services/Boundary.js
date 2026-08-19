import { Request } from "@egovernments/digit-ui-libraries";

export const BoundaryService = {

  fetchBoundaryHierarchy: async (queryFilter) => {
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

  fetchBoundaryRelations : async (boundaryType, codes) => {
    const endpoint = "/boundary-service/boundary-relationships/_search";
    const params = {
      tenantId : "in",
      includeChildren : true,
      includeParents : false,
      hierarchyType: "SELCO",
      boundaryType,
      codes,
    }
    const headers = {
      "Content-Type" : "application/json"
    }

    return await Request({
      url : endpoint,
      userService : true,
      method : "POST",
      auth : true,
      params : params,
      headers : headers,
    });
  }

}