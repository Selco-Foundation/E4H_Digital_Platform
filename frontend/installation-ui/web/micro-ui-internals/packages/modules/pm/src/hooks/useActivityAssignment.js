import {useQuery, useQueryClient} from "react-query";
import { ActivityService } from "../services/Activity";

const fetchActivityAssignment = async (filter, limit, offset) => {
  const response = await ActivityService.fetchActivityAssignments(filter, limit, offset);
  return {
    activityAssignments: response?.ActivityAssignment,
    totalCount: response?.TotalCount,
  };
}

const useActivityAssignment = (queryFilter = {}, limit = 1000, offset = 0) => {

  const { tenantId, fieldPlanIds } = queryFilter;

  const filter = {
    ActivityAssignment : {
      tenantId: Digit.ULBService.getCurrentTenantId(),
    }
  };

  if (tenantId) {
    filter.ActivityAssignment.tenantId = tenantId;
  }

  if (fieldPlanIds) {
    filter.ActivityAssignment.fieldPlanIds = fieldPlanIds;
  }

  const queryClient = useQueryClient();
  const {isLoading, isError, error, data} = useQuery(
    ["ACTIVITY_ASSIGNMENT", filter, limit, offset],
    () => fetchActivityAssignment(filter, limit, offset)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["ACTIVITY_ASSIGNMENT"])
  }

}

export default useActivityAssignment;