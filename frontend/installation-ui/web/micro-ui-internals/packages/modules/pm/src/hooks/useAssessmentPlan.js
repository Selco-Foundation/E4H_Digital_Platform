import { useQuery, useQueryClient } from "react-query";
import { DUMMY_ASSESSMENT_PLANS } from "../utilities/AssessmentPlanData";

// Dummy in-memory lookup until the Assessment Plan entity exists on the backend.
const fetchAssessmentPlans = async (filter) => {
  const assessmentPlans = filter.id?.length
    ? DUMMY_ASSESSMENT_PLANS.filter((plan) => filter.id.includes(plan.id))
    : DUMMY_ASSESSMENT_PLANS;

  return {
    assessmentPlans: assessmentPlans.length ? assessmentPlans : [DUMMY_ASSESSMENT_PLANS[0]],
    totalCount: assessmentPlans.length,
  };
}

const useAssessmentPlan = (queryFilter = {}) => {

  const { id } = queryFilter;
  const filter = { id };

  const queryClient = useQueryClient();
  const { isLoading, isFetching, isError, error, data } = useQuery(
    ["ASSESSMENT_PLAN", filter],
    () => fetchAssessmentPlans(filter)
  );

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["ASSESSMENT_PLAN"])
  };
}

export default useAssessmentPlan;
