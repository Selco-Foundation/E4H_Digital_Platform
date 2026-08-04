import { DUMMY_ASSESSMENT_FACILITIES, canAssignForOnSiteAssessment } from "../utilities/AssessmentPlanData";

// Dummy in-memory mutation layer until the Assessment Plan facility entity exists on the backend.

export const AssessmentFacilityService = {

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
