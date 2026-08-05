import { Request } from "@egovernments/digit-ui-libraries";
import { DUMMY_ASSESSMENT_FACILITIES, canAssignForOnSiteAssessment } from "../utilities/AssessmentPlanData";

// Facility search hits the real API below. The bulk-action mutations remain dummy/in-memory
// until the corresponding backend endpoints exist.

const mapAssessmentFacility = (facility) => ({
  ...facility,
  name: facility?.facilityName,
  remoteStatus: facility?.phoneStatus,
  onSiteStatus: facility?.fieldStatus,
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
      totalCount: response?.pagination?.total ?? facilities.length,
    };
  },

  assignForOnSiteAssessment: async (facilityIds) => {
    DUMMY_ASSESSMENT_FACILITIES.forEach((facility) => {
      if (facilityIds.includes(facility.id) && canAssignForOnSiteAssessment(facility)) {
        facility.onSiteStatus = "PENDING";
      }
    });

    return DUMMY_ASSESSMENT_FACILITIES.filter((facility) => facilityIds.includes(facility.id));
  },

  markAssessmentResult: async (facilityIds, result, reason, remarks) => {
    DUMMY_ASSESSMENT_FACILITIES.forEach((facility) => {
      if (!facilityIds.includes(facility.id)) return;

      facility.result = result;
      facility.resultSource = "MANUAL";

      if (result === "NOT_ELIGIBLE") {
        facility.notEligibleReason = reason || null;
        facility.notEligibleRemarks = remarks || null;
      } else {
        facility.notEligibleReason = null;
        facility.notEligibleRemarks = null;
      }
    });

    return DUMMY_ASSESSMENT_FACILITIES.filter((facility) => facilityIds.includes(facility.id));
  },

};
