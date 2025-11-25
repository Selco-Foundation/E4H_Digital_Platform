import { Request } from "../atoms/Utils/Request";

export const BoundaryService = {

  fetchBoundaryRelations : async (queryFilter) => {
    const endpoint = "/boundary-service/boundary-relationships/v2/_search";
    const headers = {
      "Content-Type" : "application/json"
    }
    const data = {
      "BoundaryRelationship": queryFilter,
    }

    return await Request({
      url : endpoint,
      userService : true,
      method : "POST",
      auth : true,
      data : data,
      headers : headers,
    });
  },

}