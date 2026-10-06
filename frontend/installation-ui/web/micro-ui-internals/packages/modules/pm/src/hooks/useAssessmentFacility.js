import { useQuery, useQueryClient } from "react-query";
import { AssessmentFacilityService } from "../services/AssessmentFacility";

const useAssessmentFacility = (planId, projectQueryFilter, pageSize, pageOffset) => {

  const { facilityFilterQuery, facilitySearchQuery } = projectQueryFilter;

  const filters = {
    ...facilityFilterQuery,
    ...facilitySearchQuery,
  };

  const limit = pageSize || 10;
  const offset = pageOffset || 0;

  const queryClient = useQueryClient();
  const { isLoading, isFetching, isError, error, data } = useQuery(
    ["ASSESSMENT_FACILITY", planId, filters, limit, offset],
    () => AssessmentFacilityService.searchAssessmentFacilities(planId, filters, limit, offset),
    { enabled: !!planId }
  );

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["ASSESSMENT_FACILITY"])
  };
}

export default useAssessmentFacility;
