import { useQuery, useQueryClient } from "react-query";
import { ActivityService } from "../services/Activity";

const formatDate = (timestamp) => {
  if (!timestamp) return "";
  const date = new Date(timestamp);
  const month = String(date.getMonth() + 1).padStart(2, "0"); // months are 0-based
  const day = String(date.getDate()).padStart(2, "0");
  const year = date.getFullYear();
  return `${month}/${day}/${year}`;
};

const formatFacilities = (facilities) => {
  return facilities?.map((row) => ({
    id: row?.activityFacility?.id,
    activityType: row?.activityFacility?.activityType,
    projectId: row?.activityFacility?.fieldPlan?.project?.id,
    projectCode: row?.activityFacility?.fieldPlan?.project?.name,
    fieldPlanId: row?.activityFacility?.fieldPlan?.id,
    fieldPlanCode: row?.activityFacility?.fieldPlan?.name,
    activityStartDate: formatDate(row?.activityFacility?.activatedAt),
    activityEndDate: formatDate(row?.activityFacility?.completedAt),
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