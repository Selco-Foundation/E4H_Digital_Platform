import {Request} from "@egovernments/digit-ui-libraries";
import { applyRectificationMock } from "../utilities/rectificationMock";

export const ActivityService = {

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

    const response = await Request({
      url: endpoint,
      data: queryFilter,
      userService: true,
      method: "POST",
      auth: true,
      params : params,
      headers: headers,
    });

    return applyRectificationMock(response);
  },

}