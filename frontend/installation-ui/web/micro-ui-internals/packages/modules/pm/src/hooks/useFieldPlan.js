import {useQuery, useQueryClient} from "react-query";
import { FieldPlanService } from "../services/FieldPlan";

const fetchProject = async (filter, limit, offset) => {
  const response = await FieldPlanService.fetchFieldPlans(filter, limit, offset);
  return {
    fieldPlans: response?.FieldPlans,
    totalCount: response?.TotalCount,
    iccTemplates: response?.iccTemplates || response?.IccTemplates || [],
  };
}

const useFieldPlan = (queryFilter = {}, limit = 10, offset = 0, sortBy = null, sortDir = "DESC") => {

  const { tenantId, ids, projectIds, name } = queryFilter;

  const filter = {
    FieldPlans : {
      tenantId: Digit.ULBService.getCurrentTenantId(),
    }
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

  if (name) {
    filter.FieldPlans.name = name;
  }

  const queryClient = useQueryClient();
  const {isLoading, isError, error, data} = useQuery(
    ["FIELD_PLAN", filter, limit, offset],
    () => fetchProject(filter, limit, offset)
  );

  return {
    isLoading, isError, error, data,
    revalidate: async () => {
      await queryClient.invalidateQueries(["FIELD_PLAN"]);
      return queryClient.getQueryData(["FIELD_PLAN", filter, limit, offset]);
    }
  }

}

export default useFieldPlan;
