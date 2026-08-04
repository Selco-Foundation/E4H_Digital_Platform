import { Request } from "@egovernments/digit-ui-libraries";

// Plan create/search and facility-template download/upload hit real APIs (see PMService).
// hasUploadedAssessmentFacilityData/markAssessmentFacilityDataUploaded remain session-local in-memory
// tracking, since no backend status-check endpoint exists yet for whether a plan has facility data.

const uploadedAssessmentFacilityPlanIds = new Set();

const DEFAULT_ASSESSMENT_SUMMARY = {
  totalFacilities: 0,
  remoteAssessmentsCompleted: 0,
  onSiteAssessmentsCompleted: 0,
  eligibleCount: 0,
  notEligibleCount: 0,
};

const withAssessmentPlanDefaults = (assessmentPlan) => ({
  ...assessmentPlan,
  status: assessmentPlan?.status || "DRAFT",
  numberOfFacilities: assessmentPlan?.numberOfFacilities || 0,
  summary: assessmentPlan?.summary || DEFAULT_ASSESSMENT_SUMMARY,
});

const extractAssessmentPlans = (response) => {
  const plans = response?.plans ||
    response?.Plans ||
    response?.assessmentPlans ||
    response?.AssessmentPlans ||
    response?.data?.plans ||
    response?.data?.Plans ||
    [];

  return Array.isArray(plans) ? plans : [plans];
};

const extractAssessmentPlan = (response) => (
  response?.plan ||
  response?.Plan ||
  response?.assessmentPlan ||
  response?.AssessmentPlan ||
  extractAssessmentPlans(response)?.[0]
);

export const AssessmentPlanService = {

  fetchAssessmentPlans: async (queryFilter, limit = 10, offset = 0) => {
    const endpoint = "/field-planner/assessment/v1/plan/_search";
    const headers = { "Content-Type": "application/json" };

    const response = await Request({
      url: endpoint,
      data: { criteria: queryFilter?.criteria || {} },
      userService: true,
      method: "POST",
      auth: true,
      params: { offset, limit },
      headers,
    });

    const assessmentPlans = extractAssessmentPlans(response).filter(Boolean).map(withAssessmentPlanDefaults);

    return {
      AssessmentPlans: assessmentPlans,
      TotalCount: response?.totalCount ?? response?.TotalCount ?? assessmentPlans.length,
    };
  },

  upsertAssessmentPlan: async (assessmentPlanData) => {
    const [assessmentPlan] = assessmentPlanData.AssessmentPlans;

    if (assessmentPlanData.apiOperation === "UPDATE") {
      // No backend update endpoint exists yet for assessment plans; merge and return optimistically.
      return [withAssessmentPlanDefaults(assessmentPlan)];
    }

    const endpoint = "/field-planner/assessment/v1/plan/_create";
    const headers = { "Content-Type": "application/json" };

    const response = await Request({
      url: endpoint,
      data: { plan: assessmentPlan },
      userService: true,
      method: "POST",
      auth: true,
      headers,
    });

    const createdAssessmentPlan = extractAssessmentPlan(response) || assessmentPlan;
    return [withAssessmentPlanDefaults(createdAssessmentPlan)];
  },

  markAssessmentFacilityDataUploaded: (assessmentPlanId) => {
    uploadedAssessmentFacilityPlanIds.add(assessmentPlanId);
  },

  hasUploadedAssessmentFacilityData: async (assessmentPlanId) => uploadedAssessmentFacilityPlanIds.has(assessmentPlanId),

  completeAssessmentPlan: async (assessmentPlan) => {
    // No backend "complete" endpoint exists yet for assessment plans; merge and return optimistically.
    return withAssessmentPlanDefaults({ ...assessmentPlan, status: "COMPLETED" });
  },

};
