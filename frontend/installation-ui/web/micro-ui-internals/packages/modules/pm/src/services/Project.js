import { Request } from "@egovernments/digit-ui-libraries";

export const ProjectService = {

  fetchProjects : async (queryFilter, limit, offset) => {
    const endpoint = "/project/v2/_search";
    const params = {
      tenantId : "in",
      offset,
      limit,
      includeAncestors : false,
      includeDescendants : false
    }
    const headers = {
      "Content-Type" : "application/json"
    }

    return await Request({
      url : endpoint,
      data : queryFilter,
      userService : true,
      method : "POST",
      auth : true,
      params : params,
      headers : headers,
    });
  },

  upsertProject : async (projectData) => {
    let endpoint = "/project/v1/_create";
    const headers = {
      "Content-Type" : "application/json"
    }

    if (projectData.apiOperation === "UPDATE") {
      endpoint = "/project/v1/_update";
    }

    return await Request({
      url : endpoint,
      data: projectData,
      userService : true,
      method : "POST",
      auth : true,
      headers : headers,
    });
  }

}