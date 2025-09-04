import { useQuery, useQueryClient } from "react-query";
import {BoundaryService} from "../services/Boundary";

const fetchBoundaries = async (boundaryType, codes) => {

  const boundaryResponse = await BoundaryService.fetchBoundaryRelations(boundaryType, codes);

  const states = [];
  const districts = [];
  const blocks = [];

  boundaryResponse?.TenantBoundary?.[0]?.boundary?.forEach(stateObject => {
    states.push({
      code: stateObject?.code,
    });

    stateObject?.children?.forEach((district) => {
      districts.push({
        code: district?.code,
        stateCode: stateObject?.code,
      });
      district?.children?.forEach((block) => {
        blocks.push({
          code: block?.code,
          districtCode: district?.code,
          stateCode: stateObject?.code
        });
      });
    });
  });

  return {
    states,
    districts,
    blocks,
  }
}

const useBoundary = (boundaryType, codes) => {

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["BOUNDARY", boundaryType, codes],
    () => fetchBoundaries(boundaryType, codes)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["BOUNDARY"])
  }

}

export default useBoundary;