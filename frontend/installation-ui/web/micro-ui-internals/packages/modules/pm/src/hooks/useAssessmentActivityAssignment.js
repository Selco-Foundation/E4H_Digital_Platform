import { useQuery, useQueryClient } from "react-query";
import { AssessmentActivityService } from "../services/AssessmentActivity";

const fetchAssessmentActivityAssignment = async (filter) => {
  const response = await AssessmentActivityService.fetchActivityAssignments(filter);
  return {
    activityAssignments: response?.ActivityAssignment,
    totalCount: response?.TotalCount,
  };
}

const useAssessmentActivityAssignment = (queryFilter = {}) => {

  const { tenantId, assessmentPlanIds } = queryFilter;

  const filter = {
    AssessmentActivityAssignment: {
      tenantId: Digit.ULBService.getCurrentTenantId(),
    }
  };

  if (tenantId) {
    filter.AssessmentActivityAssignment.tenantId = tenantId;
  }

  if (assessmentPlanIds) {
    filter.AssessmentActivityAssignment.assessmentPlanIds = assessmentPlanIds;
  }

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["ASSESSMENT_ACTIVITY_ASSIGNMENT", filter],
    () => fetchAssessmentActivityAssignment(filter)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["ASSESSMENT_ACTIVITY_ASSIGNMENT"])
  }

}

export default useAssessmentActivityAssignment;
