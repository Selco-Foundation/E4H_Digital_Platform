import {Request} from "@egovernments/digit-ui-libraries";

export const ActivityService = {

  assignActivity: async (activityAssignmentData) => {
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
  }

}