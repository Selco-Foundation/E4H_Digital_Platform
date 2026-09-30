import { useQuery, useQueryClient } from "react-query";
import { BoundaryService } from "../services/Boundary";

const useStateOptions = () => {
  const queryClient = useQueryClient();
  const query = useQuery(["AMC_STATE_OPTIONS"], () => BoundaryService.fetchStates(), {
    refetchOnWindowFocus: false,
  });

  return {
    ...query,
    revalidate: () => queryClient.invalidateQueries(["AMC_STATE_OPTIONS"]),
  };
};

export default useStateOptions;
