import { useQuery, useQueryClient } from "react-query";
import {VisitService} from "../services/VisitService";
import {getFacilityGeography} from "../utilities/GeographyUtils";

const formatVisits = (visits) => {
  return visits?.map((row) => {
    const geography = getFacilityGeography(row?.facility);

    // Add normalized location fields for project-level AMC visit rows.
    return ({
      id: row?.id,
      facilityName: row?.facility?.facility_name,
      facilityId: row?.facility?.id,
      status: row?.status,
      state: geography.state,
      block: geography.block,
      district: geography.district,
      assigned: row?.assignments?.[0]?.user?.name,
    });
  });
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
