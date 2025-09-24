import {Request} from "@egovernments/digit-ui-libraries";

export const FieldPlanService = {

  fetchFieldPlans: async (queryFilter, limit, offset) => {
    const endpoint = "/field-planner/v1/field-plans/_search";
    const params = {
      tenantId: "in",
      offset,
      limit,
    }

    const headers = {
      "Content-Type": "application/json"
    }

    return await Request({
      url: endpoint,
      data: queryFilter,
      userService: true,
      method: "POST",
      auth: true,
      params: params,
      headers: headers,
    });
  },

  upsertFieldPlan: async (fieldPlanData) => {
    let endpoint = "/field-planner/v1/field-plans/_create";
    const headers = {
      "Content-Type": "application/json"
    }

    if (fieldPlanData.apiOperation === "UPDATE") {
      endpoint = "/field-planner/v1/field-plans/_update";
    }

    return await Request({
      url: endpoint,
      data: fieldPlanData,
      userService: true,
      method: "POST",
      auth: true,
      headers: headers,
    });
  },

}