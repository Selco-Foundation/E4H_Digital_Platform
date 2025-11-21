import { Request } from "../atoms/Utils/Request";

export const BoundaryService = {

  fetchBoundaryRelations : async (params) => {
    const endpoint = "/boundary-service/boundary-relationships/_search";
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