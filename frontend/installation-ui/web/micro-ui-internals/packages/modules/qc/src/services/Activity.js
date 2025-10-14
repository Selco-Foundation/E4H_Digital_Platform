import {Request} from "@egovernments/digit-ui-libraries";

export const ActivityService = {

  fetchActivityAssignments: async (queryFilter, limit = 10, offset = 0) => {
    const endpoint = "/activity/v1/activities/assignment/_search";
    const headers = {
      "Content-Type": "application/json"
    }
    const params = {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      offset,
      limit,
    }

    return await Request({
      url: endpoint,
      data: queryFilter,
      userService: true,
      method: "POST",
      auth: true,
      params : params,
      headers: headers,
    });
  },

  fetchFacilityAssignments: async (queryFilter, limit = 10, offset = 0) => {
    const endpoint = "/activity/v1/activities/_search";
    const headers = {
      "Content-Type": "application/json"
    }
    const params = {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      offset,
      limit,
    }

    return await Request({
      url: endpoint,
      data: queryFilter,
      userService: true,
      method: "POST",
      auth: true,
      params : params,
      headers: headers,
    });
  },

}