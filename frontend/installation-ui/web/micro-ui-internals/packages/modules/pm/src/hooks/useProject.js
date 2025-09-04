import { useQuery, useQueryClient } from "react-query";
import {ProjectService} from "../services/Project";

const fetchProject = async (filter, limit, offset) => {
  const response = await ProjectService.fetchProjects(filter, limit, offset);
  return response?.Project?.[0]?.project;
}

const useProject = (projectId) => {

  const filter = {
    Project: {
      id: [projectId],
    }
  }

  const limit = 1;
  const offset = 0;

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["PROJECT", filter, limit, offset],
    () => fetchProject(filter, limit, offset)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["PROJECT"])
  }

}

export default useProject;