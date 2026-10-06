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

    if (fieldPlanData?.apiOperation === "UPDATE") {
      endpoint = "/field-planner/v1/field-plans/_update";
    }

    const response = await Request({
      url: endpoint,
      data: fieldPlanData,
      userService: true,
      method: "POST",
      auth: true,
      headers: headers,
    });

    return response?.FieldPlans;
  },

  searchFieldPlanTemplates: async (fieldPlanId) => {
    const endpoint = "/field-planner/v1/field-plan-templates/_search";
    const tenantId = Digit.ULBService.getCurrentTenantId();
    const headers = {
      "Content-Type": "application/json"
    }
    const data = {
      FieldPlanTemplate: {
        fieldPlanId: [fieldPlanId],
        tenantId: tenantId,
      },
    };

    const response = await Request({
      url: endpoint,
      data: data,
      userService: true,
      method: "POST",
      auth: true,
      params: {
        tenantId,
        limit: 10,
        offset: 0,
      },
      headers: headers,
    });

    const templates = response?.FieldPlanTemplates ||
      response?.FieldPlanTemplate ||
      response?.fieldPlanTemplates ||
      response?.fieldPlanTemplate ||
      response?.templates ||
      response?.data?.FieldPlanTemplates ||
      response?.data?.FieldPlanTemplate ||
      response?.data?.fieldPlanTemplates ||
      response?.data?.fieldPlanTemplate ||
      [];

    return Array.isArray(templates) ? templates : [templates];
  },

  searchFieldPlanFacilitySystemTypeCapacities: async (fieldPlanId) => {
    const endpoint = "/field-planner/v1/field-plans/facility/system_type_capacity/_search";
    const tenantId = Digit.ULBService.getCurrentTenantId();
    const headers = {
      "Content-Type": "application/json"
    };
    const data = {
      FieldPlanFacility: {
        fieldPlanId: [fieldPlanId],
      },
    };

    const response = await Request({
      url: endpoint,
      data,
      userService: true,
      method: "POST",
      auth: true,
      params: {
        tenantId,
        limit: 1000,
        offset: 0,
        includeDeleted: false,
      },
      headers,
    });

    const systemTypeCapacities = response?.systemTypeCapacities ||
      response?.SystemTypeCapacities ||
      response?.data?.systemTypeCapacities ||
      response?.data?.SystemTypeCapacities ||
      [];

    return Array.isArray(systemTypeCapacities) ? systemTypeCapacities : [systemTypeCapacities];
  },

}
