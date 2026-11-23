import { useQuery, useQueryClient } from "react-query";
import { AssessmentPlanService } from "../services/AssessmentPlan";

const fetchAssessmentPlan = async (filter, limit, offset) => {
  const response = await AssessmentPlanService.fetchAssessmentPlans(filter, limit, offset);
  return {
    assessmentPlans: response?.AssessmentPlans,
    totalCount: response?.TotalCount,
  };
}

const useAssessmentPlan = (queryFilter = {}, limit = 10, offset = 0) => {

  const { id, projectIds } = queryFilter;

  const filter = {
    criteria: {
      tenantId: Digit.ULBService.getCurrentTenantId(),
    }
  };

  if (id?.length) {
    filter.criteria.ids = id;
  }

  if (projectIds?.length) {
    filter.criteria.projectId = projectIds[0];
  }

  const queryClient = useQueryClient();
  const { isLoading, isFetching, isError, error, data } = useQuery(
    ["ASSESSMENT_PLAN", filter, limit, offset],
    () => fetchAssessmentPlan(filter, limit, offset)
  );

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: async () => {
      await queryClient.invalidateQueries(["ASSESSMENT_PLAN"]);
      return queryClient.getQueryData(["ASSESSMENT_PLAN", filter, limit, offset]);
    }
  };
}

export default useAssessmentPlan;
