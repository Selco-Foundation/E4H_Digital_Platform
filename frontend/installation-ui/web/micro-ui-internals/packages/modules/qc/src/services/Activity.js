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

  fetchActivityFacilities: async (queryFilter, limit = 10, offset = 0) => {
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

  updateActivityFacilityWorkflow : async (activityFacilityId, action, comments, workflowComment, documents = []) => {
    const endpoint = "/activity/v1/activities/workflow/update";
    const queryObj = {
      activityFacilityId: activityFacilityId,
      workflow: {
        action: action,
        comment: workflowComment,
        documents: documents
      },
      transactions: [
        {
          comments: [...comments]
        }
      ]
    }
    const headers = {
      "Content-Type" : "application/json"
    }

    return await Request({
      url : endpoint,
      data : queryObj,
      method : "POST",
      userService : true,
      auth : true,
      headers : headers,
    });
  },

}