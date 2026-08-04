import { Request } from "@egovernments/digit-ui-libraries";

// Facility-template handling remains dummy/in-memory until those endpoints exist on the backend.
// Plan create/search hit the real assessment-plan APIs below.

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

  downloadAssessmentFacilityDataTemplate: async (assessmentPlanId, boundaryData, t) => {
    await new Promise((resolve) => setTimeout(resolve, 400));

    const rows = [["Facility Name", "Facility Type", "District", "Block"]];
    boundaryData.districts.forEach((district) => {
      boundaryData.blocks
        .filter((block) => block.districtCode === district.code)
        .forEach((block) => {
          rows.push(["", "", t(`Boundary_${district.code}`), t(`Boundary_${block.code}`)]);
        });
    });

    const csvContent = rows
      .map((row) => row.map((cell) => `"${String(cell ?? "").replace(/"/g, '""')}"`).join(","))
      .join("\n");

    const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
    const downloadUrl = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = downloadUrl;
    link.download = `assessment-facility-template-${assessmentPlanId}.csv`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(downloadUrl);
  },

  uploadAssessmentFacilityDataTemplate: async (file, assessmentPlanId) => {
    await new Promise((resolve) => setTimeout(resolve, 400));

    uploadedAssessmentFacilityPlanIds.add(assessmentPlanId);

    return {
      file: {
        name: file.name,
        data: file,
      },
    };
  },

  hasUploadedAssessmentFacilityData: async (assessmentPlanId) => uploadedAssessmentFacilityPlanIds.has(assessmentPlanId),

  completeAssessmentPlan: async (assessmentPlan) => {
    // No backend "complete" endpoint exists yet for assessment plans; merge and return optimistically.
    return withAssessmentPlanDefaults({ ...assessmentPlan, status: "COMPLETED" });
  },

};
