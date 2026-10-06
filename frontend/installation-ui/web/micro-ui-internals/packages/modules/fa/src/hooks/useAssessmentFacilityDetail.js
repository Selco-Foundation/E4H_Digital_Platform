import { useQuery, useQueryClient } from "react-query";
import { AssessmentFacilityService } from "../services/AssessmentFacility";

const useAssessmentFacilityDetail = (planFacilityId) => {

  const queryClient = useQueryClient();
  const { isLoading, isFetching, isError, error, data } = useQuery(
    ["ASSESSMENT_FACILITY_DETAIL", planFacilityId],
    () => AssessmentFacilityService.fetchFacilityDetail(planFacilityId),
    { enabled: !!planFacilityId }
  );

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["ASSESSMENT_FACILITY_DETAIL", planFacilityId])
  };
}

export default useAssessmentFacilityDetail;
