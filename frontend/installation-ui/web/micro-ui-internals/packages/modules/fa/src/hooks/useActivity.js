import { useQuery, useQueryClient } from "react-query";
import { ActivityService } from "../services/Activity";

const formatFacilities = (facilities) => {
  return facilities?.map((row) => ({
    id: row?.activityFacility?.id,
    facilityName: row?.activityFacility?.facility?.facility_name,
    facilityId: row?.activityFacility?.facilityId,
    status: row?.activityFacility?.status,
    block: row?.activityFacility?.facility?.boundaryCode,
    district: row?.activityFacility?.facility?.additionalDetails?.district,
    assigned: row?.activityFacility?.assignedEmployeeUser?.name,
  }));
};

const fetchFacilities = async (filter, limit, offset) => {
  const facilitiesResponse = await ActivityService.fetchActivityFacilities(filter, limit, offset);
  return {
    facilities: formatFacilities(facilitiesResponse.facility),
    totalCount: facilitiesResponse.TotalCount,
  };
};

const useActivity = (projectQueryFilter, pageSize, pageOffset) => {

  const { project, facilityFilterQuery } = projectQueryFilter;

  const filter = {
    ActivityFacility: {
      tenantId: Digit.ULBService.getCurrentTenantId(),
    },
  };

  if (project?.facilityId?.length) {
    filter.ActivityFacility.facilityIds = project.facilityId;
  }

  if (facilityFilterQuery?.activityCode?.length) {
    filter.ActivityFacility.activityCodes = facilityFilterQuery.activityCode;
  }

  const limit = pageSize || 10;
  const offset = pageOffset || 0;

  const queryClient = useQueryClient();
  const { isLoading, isFetching, isError, error, data } = useQuery(
    ["ACTIVITY", filter, limit, offset],
    () => fetchFacilities(filter, limit, offset)
  );

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["ACTIVITY"]),
  };

};

export default useActivity;