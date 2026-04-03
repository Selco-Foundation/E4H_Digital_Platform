import { useQuery, useQueryClient } from "react-query";
import {VisitService} from "../services/VisitService";

const formatVisits = (visits) => {
  return visits?.map((row) => ({
      id: row?.id,
      facilityName: row?.facility?.facility_name,
      facilityId: row?.facility?.id,
      status: row?.status,
      block: "",
      district: "",
      assigned: row?.processInstances?.[0]?.assignes?.[0]?.name,
  }));
}

const fetchVisits = async (filter, limit, offset) => {
  const visitsResponse = await VisitService.fetchVisits(filter, limit, offset);
  return {
    visits: formatVisits(visitsResponse.ScheduledVisits),
    totalCount: visitsResponse.TotalCount,
  }
}

const useVisit = (projectQueryFilter, pageSize, pageOffset) => {

  const { project, facilityFilterQuery, facilitySearchQuery } = projectQueryFilter;

  const filter = {
    searchCriteria: {
      tenantId: Digit.ULBService.getCurrentTenantId(),
    }
  };

  if (project?.projectId?.length) {
    filter.searchCriteria.projectsIds = project.projectId;
  }

  if (project?.tenantId) {
    filter.searchCriteria.tenantId = project.tenantId;
  }

  if (project?.id?.length) {
    filter.searchCriteria.ids = project.id;
  }

  if (facilityFilterQuery?.status) {
    filter.searchCriteria.statuses = facilityFilterQuery.status;
  }

  if (facilitySearchQuery?.name) {
    filter.searchCriteria.facilityName = facilitySearchQuery.name;
  }

  const limit = pageSize || 10;
  const offset = pageOffset || 0;

  const queryClient = useQueryClient();
  const { isLoading, isFetching, isError, error, data } = useQuery(
    ["VISIT", filter, limit, offset],
    () => fetchVisits(filter, limit, offset)
  );

  return {
    isLoading, isFetching, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["VISIT"]),
  }
}

export default useVisit;