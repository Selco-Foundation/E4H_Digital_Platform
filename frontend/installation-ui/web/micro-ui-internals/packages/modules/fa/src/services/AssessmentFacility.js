import { Request } from "@egovernments/digit-ui-libraries";

export const AssessmentFacilityService = {

  fetchFacilityDetail: async (planFacilityId, tenantId) => {
    const endpoint = "/field-planner/assessment/v1/plan/facility/_detail";
    const headers = { "Content-Type": "application/json" };

    const response = await Request({
      url: endpoint,
      data: {
        tenantId: tenantId || Digit.ULBService.getStateId(),
        planFacilityId,
      },
      userService: true,
      method: "POST",
      auth: true,
      headers,
    });

    return response?.facility;
  },

};
