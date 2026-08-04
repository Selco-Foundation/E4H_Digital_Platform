import { DUMMY_ASSESSMENT_PLANS } from "../utilities/AssessmentPlanData";

// Dummy in-memory persistence and facility-template handling until the Assessment Plan entity exists on the backend.

let nextAssessmentPlanId = DUMMY_ASSESSMENT_PLANS.length + 1;
const uploadedAssessmentFacilityPlanIds = new Set();

const DEFAULT_ASSESSMENT_SUMMARY = {
  totalFacilities: 0,
  remoteAssessmentsCompleted: 0,
  onSiteAssessmentsCompleted: 0,
  eligibleCount: 0,
  notEligibleCount: 0,
};

export const AssessmentPlanService = {

  fetchAssessmentPlans: async (queryFilter, limit = 10, offset = 0) => {
    const ids = queryFilter?.AssessmentPlans?.ids;
    const projectIds = queryFilter?.AssessmentPlans?.projectIds;

    let assessmentPlans = DUMMY_ASSESSMENT_PLANS;
    if (ids?.length) {
      assessmentPlans = assessmentPlans.filter((plan) => ids.includes(plan.id));
    }
    if (projectIds?.length) {
      assessmentPlans = assessmentPlans.filter((plan) => projectIds.includes(plan.projectId));
    }

    return {
      AssessmentPlans: assessmentPlans.slice(offset, offset + limit),
      TotalCount: assessmentPlans.length,
    };
  },

  upsertAssessmentPlan: async (assessmentPlanData) => {
    const [assessmentPlan] = assessmentPlanData.AssessmentPlans;

    if (assessmentPlanData.apiOperation === "UPDATE") {
      const index = DUMMY_ASSESSMENT_PLANS.findIndex((plan) => plan.id === assessmentPlan.id);
      if (index !== -1) {
        DUMMY_ASSESSMENT_PLANS[index] = { ...DUMMY_ASSESSMENT_PLANS[index], ...assessmentPlan };
        return [DUMMY_ASSESSMENT_PLANS[index]];
      }
    }

    const createdAssessmentPlan = {
      ...assessmentPlan,
      id: String(nextAssessmentPlanId++),
      status: "DRAFT",
      numberOfFacilities: assessmentPlan.numberOfFacilities || 0,
      summary: assessmentPlan.summary || DEFAULT_ASSESSMENT_SUMMARY,
    };
    DUMMY_ASSESSMENT_PLANS.push(createdAssessmentPlan);
    return [createdAssessmentPlan];
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

  completeAssessmentPlan: async (assessmentPlanId) => {
    const index = DUMMY_ASSESSMENT_PLANS.findIndex((plan) => plan.id === assessmentPlanId);
    if (index === -1) {
      return null;
    }

    DUMMY_ASSESSMENT_PLANS[index] = { ...DUMMY_ASSESSMENT_PLANS[index], status: "COMPLETED" };
    return DUMMY_ASSESSMENT_PLANS[index];
  },

};
