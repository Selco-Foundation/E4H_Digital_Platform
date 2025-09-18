import { useQuery, useQueryClient } from "react-query";
import {ProjectService} from "../services/Project";

const formatProjects = (rawProjects) => {
  return rawProjects.map((rawProject) => {
    return {
      ...rawProject.project,
      status: rawProject.status,
    }
  })
}

const fetchProject = async (filter, limit, offset, sortBy, sortDir) => {
  const response = await ProjectService.fetchProjects(filter, limit, offset, sortBy, sortDir);
  return {
    projects: formatProjects(response?.Project),
    totalCount: response.totalCount,
  };
}

const useProject = (queryFilter = {}, limit = 10, offset = 0, sortBy = null, sortDir = "DESC") => {

  const { id, name, subProjectTypeId } = queryFilter;

  const filter = {
    Project: {}
  }

  if (id?.length) {
    filter.Project.id = id;
  }

  if (name) {
    filter.Project.name = name;
  }

  if (subProjectTypeId) {
    filter.Project.subProjectTypeId = subProjectTypeId;
  }

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["PROJECT", filter, limit, offset, sortBy, sortDir],
    () => fetchProject(filter, limit, offset, sortBy, sortDir)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["PROJECT"])
  }

}

export default useProject;