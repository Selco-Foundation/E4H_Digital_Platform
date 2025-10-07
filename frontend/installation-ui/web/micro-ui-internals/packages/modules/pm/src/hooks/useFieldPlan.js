import {useQuery, useQueryClient} from "react-query";
import { FieldPlanService } from "../services/FieldPlan";

const fetchProject = async (filter, limit, offset) => {
  const response = await FieldPlanService.fetchFieldPlans(filter, limit, offset);
  return {
    fieldPlans: response?.FieldPlans,
    totalCount: response?.TotalCount,
  };
}

const useFieldPlan = (queryFilter = {}, limit = 10, offset = 0, sortBy = null, sortDir = "DESC") => {

  const { tenantId, ids, projectIds } = queryFilter;

  const filter = {
    FieldPlans : {}
  };

  if (tenantId) {
    filter.FieldPlans.tenantId = tenantId;
  }

  if (ids?.length) {
    filter.FieldPlans.ids = ids;
  }

  if (projectIds?.length) {
    filter.FieldPlans.projectIds = projectIds;
  }

  const queryClient = useQueryClient();
  const {isLoading, isError, error, data} = useQuery(
    ["FIELD_PLAN", filter, limit, offset],
    () => fetchProject(filter, limit, offset)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["FIELD_PLAN"])
  }

}

export default useFieldPlan;