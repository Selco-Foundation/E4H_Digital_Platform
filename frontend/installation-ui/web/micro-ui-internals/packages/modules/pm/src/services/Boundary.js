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

  /**
   * Boundaries and their descendants.
   *
   * `boundaryType` asks for a whole level; `codes` asks about specific boundaries. They are
   * mutually exclusive in this endpoint's params, so only one is sent. Parents come back only
   * when asking about codes - that is what turns a district- or block-level jurisdiction into
   * the state containing it, without splitting the boundary code on underscores.
   */
  fetchBoundaryRelations : async (boundaryType, codes) => {
    const endpoint = "/boundary-service/boundary-relationships/_search";
    const scoped = !!codes?.length;
    const params = {
      tenantId : "in",
      includeChildren : true,
      includeParents : scoped,
      hierarchyType: "SELCO",
      ...(scoped ? { codes : Array.isArray(codes) ? codes.join(",") : codes } : { boundaryType }),
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