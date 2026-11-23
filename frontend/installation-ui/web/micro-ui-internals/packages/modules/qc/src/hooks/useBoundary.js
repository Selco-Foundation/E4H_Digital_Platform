import { useQuery, useQueryClient } from "react-query";
import { BoundaryService } from "../services/Boundary";

const fetchBoundaries = async (codes, boundaryType) => {

  const boundaryResponse = await BoundaryService.fetchBoundaryRelations(codes, boundaryType);
  const stateObject = boundaryResponse?.TenantBoundary?.[0]?.boundary?.[0];

  const state = [
    {
      code: stateObject?.code,
    }
  ];
  const districts = [];
  const blocks = [];

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

  return {
    state,
    districts,
    blocks,
  };
}

const useBoundary = (codes, boundaryType) => {

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["BOUNDARY", codes, boundaryType],
    () => fetchBoundaries(codes, boundaryType)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["BOUNDARY"])
  }

}

export default useBoundary;
