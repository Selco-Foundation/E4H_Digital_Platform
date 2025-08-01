import { useQuery, useQueryClient } from "react-query";

const fetchFacilityProjects = async (filter) => {
  let facilityQueryFilter;
  const projectsResponse = await Digit.QCService.fetchProjects(filter);
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

  return Array.from(projectMap.values()).map((row, index) => ({
    id: index+1,
    facilityId: row?.facility?.facilityId,
    facilityName: row?.project?.name || row?.facility?.facilityId,
    facilityProjectId: row?.facility?.id,
    projectId:row?.project?.id,
    parentId:row?.project?.parent,
    block: row?.project?.address?.boundary || "-",
    district: row?.project?.address?.city || "-",
    assigned: row?.workflow?.assignes || "-",
    status: row?.status || "-",
  }));
}

const useFacility = (projectQueryFilter) => {

  const { Project } = projectQueryFilter;
  const filter = {};

  if (Project) {
    filter.Project = Project;
  }

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["facility", filter],
    () => fetchFacilityProjects(filter)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["facility", filter])
  }
}

export default useFacility;