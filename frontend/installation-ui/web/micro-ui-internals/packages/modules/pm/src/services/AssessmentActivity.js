// Dummy in-memory activity-assignment store until the Assessment Plan entity exists on the backend.

let assessmentActivityAssignments = [];
let nextAssessmentActivityAssignmentId = 1;

export const AssessmentActivityService = {

  fetchActivityAssignments: async (queryFilter) => {
    const assessmentPlanIds = queryFilter?.AssessmentActivityAssignment?.assessmentPlanIds;

    const filtered = assessmentPlanIds?.length
      ? assessmentActivityAssignments.filter((assignment) => assessmentPlanIds.includes(assignment.assessmentPlanId))
      : assessmentActivityAssignments;

    return {
      ActivityAssignment: filtered,
      TotalCount: filtered.length,
    };
  },

  createActivityAssignment: async (activityAssignmentData) => {
    const created = activityAssignmentData.map((assignment) => ({
      ...assignment,
      id: String(nextAssessmentActivityAssignmentId++),
      auditDetails: { createdTime: Date.now() },
    }));
    assessmentActivityAssignments.push(...created);
    return created;
  },

  updateActivityAssignment: async (activityAssignmentData) => {
    activityAssignmentData.forEach((assignment) => {
      const index = assessmentActivityAssignments.findIndex((existing) => existing.id === assignment.id);
      if (index !== -1) {
        assessmentActivityAssignments[index] = { ...assessmentActivityAssignments[index], ...assignment };
      }
    });
    return activityAssignmentData;
  },

  deleteActivityAssignment: async (activityAssignmentData) => {
    const idsToDelete = activityAssignmentData.map((assignment) => assignment.id).filter(Boolean);
    assessmentActivityAssignments = assessmentActivityAssignments.filter((assignment) => !idsToDelete.includes(assignment.id));
    return activityAssignmentData;
  },

};
