import {Request} from "@egovernments/digit-ui-libraries";

export const ActivityService = {

  fetchActivityAssignments: async (queryFilter, limit = 1000, offset = 0) => {
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

  createActivityAssignment: async (activityAssignmentData) => {
    const endpoint = "/activity/v1/activities/_assign-activity";
    const headers = {
      "Content-Type": "application/json"
    }

    const assignmentData = {
      ActivityAssignment: activityAssignmentData,
    }

    return await Request({
      url: endpoint,
      data: assignmentData,
      userService: true,
      method: "POST",
      auth: true,
      headers: headers,
    });
  },

  updateActivityAssignment: async (activityAssignmentData) => {
    const endpoint = "/activity/v1/activities/assignment/_update";
    const headers = {
      "Content-Type": "application/json"
    }

    const assignmentData = {
      ActivityAssignment: activityAssignmentData,
    }

    return await Request({
      url: endpoint,
      data: assignmentData,
      userService: true,
      method: "POST",
      auth: true,
      headers: headers,
    });
  },

  deleteActivityAssignment: async (activityAssignmentData) => {
    const endpoint = "/activity/v1/activities/_unassign-activity";
    const headers = {
      "Content-Type": "application/json"
    }

    const assignmentData = {
      ActivityAssignment: activityAssignmentData,
    }

    return await Request({
      url: endpoint,
      data: assignmentData,
      userService: true,
      method: "POST",
      auth: true,
      headers: headers,
    });
  }

}