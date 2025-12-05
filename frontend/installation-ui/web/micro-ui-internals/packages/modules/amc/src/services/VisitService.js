import {Request} from "@egovernments/digit-ui-libraries";
import { CustomRequest } from "../components/Custom/CustomRequest";

export const VisitService = {

  fetchVisits: async (queryFilter, limit = 10, offset = 0) => {
    const endpoint = "/asset-amc/v1/visit/_search";
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

  updateVisitWorkflow : async (visitId, action, workflowComment, documents = []) => {
    const endpoint = "/asset-amc/v1/visit/workflow/_update";
    const queryObj = {
      visitId: visitId,
      workflow: {
        action: action,
        comment: workflowComment,
        documents: documents
      },
      visitReport: {}
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