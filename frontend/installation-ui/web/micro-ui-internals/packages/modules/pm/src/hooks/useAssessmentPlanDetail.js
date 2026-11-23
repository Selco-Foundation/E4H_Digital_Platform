import { useQuery, useQueryClient } from "react-query";
import { AssessmentPlanService } from "../services/AssessmentPlan";

const useAssessmentPlanDetail = (planId) => {

  const queryClient = useQueryClient();
  const { isLoading, isFetching, isError, error, data } = useQuery(
    ["ASSESSMENT_PLAN_DETAIL", planId],
    () => AssessmentPlanService.fetchAssessmentPlanDetail(planId),
    { enabled: !!planId }
  );

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["ASSESSMENT_PLAN_DETAIL", planId])
  };
}

export default useAssessmentPlanDetail;
