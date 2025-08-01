import { useQuery, useQueryClient } from "react-query";

const useFieldPlan = (queryFilter) => {

  const { Project } = queryFilter;
  const filter = {};

  if (Project) {
    filter.Project = Project;
  }

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["fieldPlan", filter],
    () => Digit.QCService.fetchProjects(filter)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["fieldPlan", filter])
  };
}

export default useFieldPlan;