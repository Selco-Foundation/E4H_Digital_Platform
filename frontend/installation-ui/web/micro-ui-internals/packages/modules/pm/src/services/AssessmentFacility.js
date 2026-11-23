import { Request } from "@egovernments/digit-ui-libraries";

// Facility search and the bulk decision update below hit real APIs.

const mapAssessmentFacility = (facility) => ({
  ...facility,
  id: facility?.planFacilityId,
  name: facility?.facilityName,
  remoteStatus: facility?.phoneStatus || "NOT_INITIATED",
  onSiteStatus: facility?.fieldStatus || "NOT_INITIATED",
  result: facility?.overallStatus,
});

export const AssessmentFacilityService = {

  searchAssessmentFacilities: async (planId, filters, limit, offset) => {
    const endpoint = "/field-planner/assessment/v1/plan/facility/_search";
    const headers = { "Content-Type": "application/json" };

    const response = await Request({
      url: endpoint,
      data: {
        criteria: {
          planId,
          filters: filters || {},
          exportAll: false,
          includeResponseSummary: false,
        },
      },
      userService: true,
      method: "POST",
      auth: true,
      params: { offset, limit },
      headers,
    });

    const rawFacilities = response?.facilities ||
      response?.Facilities ||
      response?.data?.facilities ||
      [];

    const facilities = (Array.isArray(rawFacilities) ? rawFacilities : [rawFacilities]).map(mapAssessmentFacility);

    return {
      facilities,
      totalCount: response?.pagination?.total !== null && response?.pagination?.total !== undefined ? response.pagination.total : facilities.length,
    };
  },

  // decisions: [{ planFacilityId, overallStatus: "ELIGIBLE"|"NOT_ELIGIBLE", remarks? }, { planFacilityId, assignForField: true }, ...]
  bulkUpdateFacilityDecisions: async (planId, decisions) => {
    const endpoint = "/field-planner/assessment/v1/plan/facility/decision/_bulk-update";
    const headers = { "Content-Type": "application/json" };

    return await Request({
      url: endpoint,
      data: { planId, decisions },
      userService: true,
      method: "POST",
      auth: true,
      headers,
    });
  },

  // decisionFields: { overallStatus: "ELIGIBLE"|"NOT_ELIGIBLE", remarks? } or { assignForField: true }
  updateFacilityDecision: async (plan, planFacilityId, decisionFields) => {
    const endpoint = "/field-planner/assessment/v1/plan/facility/decision/_update";
    const headers = { "Content-Type": "application/json" };

    return await Request({
      url: endpoint,
      data: { plan, planFacilityId, ...decisionFields },
      userService: true,
      method: "POST",
      auth: true,
      headers,
    });
  },

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
