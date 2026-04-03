import { Request } from "@egovernments/digit-ui-libraries";

export const BoundaryService = {

  fetchBoundaryRelations : async (codes, boundaryType) => {
    const endpoint = "/boundary-service/boundary-relationships/_search";
    const params = {
      tenantId : Digit.ULBService.getCurrentTenantId(),
      includeChildren : true,
      includeParents : false,
      hierarchyType: "SELCO",
      boundaryType,
      codes
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
  },

}