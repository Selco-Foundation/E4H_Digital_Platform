import { useQuery, useQueryClient } from "react-query";
import { DUMMY_ASSESSMENT_FACILITIES, computeAssessmentSummary } from "../utilities/AssessmentPlanData";

// Dummy in-memory filtering/pagination until the Assessment Plan facility entity exists on the backend.
const fetchAssessmentFacilities = async (filter, limit, offset) => {
  const filtered = DUMMY_ASSESSMENT_FACILITIES.filter((facility) => {
    if (filter.district?.length && !filter.district.includes(facility.district)) {
      return false;
    }
    if (filter.facilityType?.length && !filter.facilityType.includes(facility.facilityType)) {
      return false;
    }
    if (filter.category?.length && !filter.category.includes(facility.category)) {
      return false;
    }
    if (filter.remoteStatus?.length && !filter.remoteStatus.includes(facility.remoteStatus)) {
      return false;
    }
    if (filter.onSiteStatus?.length && !filter.onSiteStatus.includes(facility.onSiteStatus)) {
      return false;
    }
    if (filter.result?.length && !filter.result.includes(facility.result)) {
      return false;
    }
    if (filter.name && !facility.name.toLowerCase().includes(filter.name.toLowerCase())) {
      return false;
    }
    return true;
  });

  return {
    facilities: filtered.slice(offset, offset + limit),
    totalCount: filtered.length,
    // The plan-level summary always reflects the full (unfiltered) facility set, matching the
    // InfoCard's role as an overview of the entire assessment plan rather than the current view.
    summary: computeAssessmentSummary(DUMMY_ASSESSMENT_FACILITIES),
  };
}

const useAssessmentFacility = (projectQueryFilter, pageSize, pageOffset) => {

  const { facilityFilterQuery, facilitySearchQuery } = projectQueryFilter;

  const filter = {
    ...facilityFilterQuery,
    ...facilitySearchQuery,
  };

  const limit = pageSize || 10;
  const offset = pageOffset || 0;

  const queryClient = useQueryClient();
  const { isLoading, isFetching, isError, error, data } = useQuery(
    ["ASSESSMENT_FACILITY", filter, limit, offset],
    () => fetchAssessmentFacilities(filter, limit, offset)
  );

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["ASSESSMENT_FACILITY"])
  };
}

export default useAssessmentFacility;
