import { useQuery, useQueryClient } from "react-query";
import { ActivityService } from "../services/Activity";

const formatFacilities = (facilities) => {
  return facilities?.map((row) => ({
      id: row?.activityFacility?.id,
      facilityName: row?.activityFacility?.facility?.facility_name,
      facilityId: row?.activityFacility?.facilityId,
      status: row?.activityFacility?.status,
      block: row?.activityFacility?.facility?.additionalDetails?.block,
      district: row?.activityFacility?.facility?.additionalDetails?.district,
      assigned: row?.activityFacility?.assignedEmployeeUser?.name,
  }));
}

const fetchFacilities = async (filter, limit, offset) => {
  const facilitiesResponse = await ActivityService.fetchActivityFacilities(filter, limit, offset);
  return {
    facilities: formatFacilities(facilitiesResponse.facility),
    totalCount: facilitiesResponse.TotalCount,
  }
}

const useFacility = (projectQueryFilter, pageSize, pageOffset) => {

  const { project, facilityFilterQuery, facilitySearchQuery } = projectQueryFilter;

  const filter = {
    ActivityFacility: {
      tenantId: Digit.ULBService.getCurrentTenantId(),
    },
    moduleSearchCriteria: {}
  };

  if (project?.fieldPlanId) {
    filter.ActivityFacility.fieldPlanIds = project.fieldPlanId;
  }

  if (project?.activityCode) {
    filter.ActivityFacility.activityCodes = project.activityCode;
  }

  if (project?.tenantId) {
    filter.ActivityFacility.tenantId = project.tenantId;
  }

  if (project?.id?.length) {
    filter.ActivityFacility.ids = project.id;
  }

  if (facilityFilterQuery?.boundary) {
    filter.ActivityFacility.boundaryCodes = facilityFilterQuery.boundary;
  }

  if (facilityFilterQuery?.status) {
    filter.ActivityFacility.statuses = facilityFilterQuery.status;
  }

  if (facilitySearchQuery?.name) {
    filter.ActivityFacility.facilityName = facilitySearchQuery.name;
  }

  const limit = pageSize || 10;
  const offset = pageOffset || 0;

  const queryClient = useQueryClient();
  const { isLoading, isFetching, isError, error, data } = useQuery(
    ["FACILITY", filter, limit, offset],
    () => fetchFacilities(filter, limit, offset)
  );

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["FACILITY"]),
    revalidateFacilityDetails: () => queryClient.invalidateQueries(["FACILITY_DETAILS"])
  }
}

export default useFacility;
