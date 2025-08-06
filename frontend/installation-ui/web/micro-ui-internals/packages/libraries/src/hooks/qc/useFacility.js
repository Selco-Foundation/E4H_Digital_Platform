import { useQuery, useQueryClient } from "react-query";

const fetchFacilityProjects = async (filter, limit, offSet) => {
  let facilityQueryFilter;
  const projectsResponse = await Digit.QCService.fetchProjects(filter, limit, offSet);
  const projectMap = new Map();

  facilityQueryFilter = {
    ProjectFacility: {
      projectId: projectsResponse?.Project?.map((row) => row.project.id)
    }
  }
  projectsResponse?.Project?.forEach((row) => {
    projectMap.set(row.project.id, row);
  })

  if (facilityQueryFilter.ProjectFacility.projectId.length > 0) {
    const facilitiesResponse =  await Digit.QCService.fetchFacilities(facilityQueryFilter)
    facilitiesResponse?.ProjectFacilities?.forEach((row) => {
       projectMap.set(
         row.projectId,
         {...projectMap.get(row.projectId), facility: row}
       );
     })
  }

  return {
    facilities: Array.from(projectMap.values()).map((row, index) => ({
      id: index + 1,
      facilityId: row?.facility?.facilityId,
      facilityName: row?.project?.name || row?.facility?.facilityId,
      facilityProjectId: row?.facility?.id,
      projectId: row?.project?.id,
      parentId: row?.project?.parent,
      block: row?.project?.address?.boundary || "-",
      district: row?.project?.address?.city || "-",
      assigned: row?.workflow?.assignes || "-",
      status: row?.status || "-"
    })),
    totalCount: projectsResponse?.totalCount || 0,
  }
}

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
        projectType: [filter.Project.projectTypeId],
        parent:[filter.Project.parent],
        sortOrder: "DESC"
      },
      limit: limit,
      offset: offset,
    }
  }

  const projectsResponse = await Digit.QCService.fetchInboxData(requestData);

  return {
    facilities: projectsResponse?.items?.map((row) => {
      const facility = row.project.facility?.[0] || {};
      const address = row.project.address || {};

      return {
        id: row.project.id,
        facilityName: facility.name || row.project.name,
        facilityId: facility.id,
        status: row.project.status,
        projectId: row.project.id,
        block: address.boundary || "-",
        district: address.city || "-",
        assigned: "-",
      }
    }),
    totalCount: projectsResponse?.totalCount || 0,
  }
}

const useFacility = (projectQueryFilter, pageSize, pageOffset) => {

  const { Project } = projectQueryFilter;
  const filter = {};

  if (Project) {
    filter.Project = Project;
  }

  const limit = pageSize || 10;
  const offset = pageOffset || 0;

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["facility", filter, limit, offset],
    () => fetchInboxData(filter, limit, offset)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["facility", filter, limit, offset])
  }
}

export default useFacility;