import { useQuery, useQueryClient } from "react-query";
import { QCService } from "../services/QC";
import { ActivityService } from "../services/Activity";

const fetchInboxData = async (filter, limit, offset) => {

  const requestData = {
    inbox: {
      tenantId: "in",
      processSearchCriteria: {
        businessService: [
          "Project"
        ],
        moduleName: "Project",
        tenantId: "in"
      },
      moduleSearchCriteria: {
        ...filter.project,
        ...filter.moduleSearchCriteria,
        sortOrder: "ASC"
      },
      limit: limit,
      offset: offset,
    }
  }

  const projectsResponse = await QCService.fetchInboxData(requestData);

  return {
    facilities: projectsResponse?.items?.map((row) => {
      const facility = row.project.additionalDetails.facility || {};
      const address = row.project.address || {};
      const additionalDetails = row.project.additionalDetails || {};
      const assigneeDetails = row.project.additionalDetails.assignedTo || {};

      return {
        id: row.project.id,
        facilityName: facility.name || row.project.name,
        facilityId: facility.facility_id,
        status: row.project.additionalDetails.status,
        projectId: row.project.id,
        block: address.boundary,
        district: additionalDetails.district,
        assigned: assigneeDetails.name,
      }
    }),
    totalCount: projectsResponse?.totalCount || 0,
  }
}

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
    filter.moduleSearchCriteria.boundary = facilityFilterQuery.boundary;
  }

  if (facilityFilterQuery?.status) {
    filter.moduleSearchCriteria.status = facilityFilterQuery.status;
  }

  if (facilitySearchQuery?.name) {
    filter.moduleSearchCriteria.name = facilitySearchQuery.name;
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