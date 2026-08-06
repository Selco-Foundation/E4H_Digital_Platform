import { Request } from "@egovernments/digit-ui-libraries";

// Plan create/search/update/complete and facility-template download/upload hit real APIs (see PMService for the latter).
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
  numberOfFacilities: assessmentPlan?.healthFacilityCount ?? assessmentPlan?.numberOfFacilities ?? 0,
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

  fetchAssessmentPlanDetail: async (planId) => {
    const endpoint = "/field-planner/assessment/v1/plan/_detail";
    const headers = { "Content-Type": "application/json" };

    const response = await Request({
      url: endpoint,
      data: { planId },
      userService: true,
      method: "POST",
      auth: true,
      headers,
    });

    const assessmentPlan = response?.plan || response?.Plan || response;
    const metrics = assessmentPlan?.metrics || {};

    return {
      totalFacilities: assessmentPlan?.healthFacilityCount ?? 0,
      remoteAssessmentDone: metrics?.remoteAssessmentDone ?? 0,
      remoteAssessmentTotal: metrics?.remoteAssessmentTotal ?? 0,
      onSiteAssessmentDone: metrics?.onSiteAssessmentDone ?? 0,
      onSiteAssessmentAssigned: metrics?.onSiteAssessmentAssigned ?? 0,
      eligible: metrics?.eligible ?? 0,
      notEligible: metrics?.notEligible ?? 0,
    };
  },

  upsertAssessmentPlan: async (assessmentPlanData) => {
    const [assessmentPlan] = assessmentPlanData.AssessmentPlans;
    const headers = { "Content-Type": "application/json" };

    if (assessmentPlanData.apiOperation === "UPDATE") {
      const endpoint = "/field-planner/assessment/v1/plan/_update";
      const data = { plan: assessmentPlan };
      if (assessmentPlanData.assessors) {
        data.assessors = assessmentPlanData.assessors;
      }

      const response = await Request({
        url: endpoint,
        data,
        userService: true,
        method: "POST",
        auth: true,
        headers,
      });

      const updatedAssessmentPlan = extractAssessmentPlan(response) || assessmentPlan;
      return [withAssessmentPlanDefaults(updatedAssessmentPlan)];
    }

    const endpoint = "/field-planner/assessment/v1/plan/_create";

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
    const endpoint = "/field-planner/assessment/v1/plan/_mark-complete";
    const headers = { "Content-Type": "application/json" };

    const response = await Request({
      url: endpoint,
      data: {
        planId: assessmentPlan?.id,
        tenantId: assessmentPlan?.tenantId || Digit.ULBService.getStateId(),
      },
      userService: true,
      method: "POST",
      auth: true,
      headers,
    });

    const updatedAssessmentPlan = extractAssessmentPlan(response) || { ...assessmentPlan, status: "COMPLETED" };
    return withAssessmentPlanDefaults(updatedAssessmentPlan);
  },

};
