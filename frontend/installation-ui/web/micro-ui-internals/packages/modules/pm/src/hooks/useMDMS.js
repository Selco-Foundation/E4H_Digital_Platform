import {useQuery, useQueryClient} from "react-query";

const useMDMS = (tenantId, module, mastersList, config = {}) => {

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["MDMS", tenantId, module, mastersList],
    () => Digit.MDMSService.getMultipleTypes(tenantId, module, mastersList),
    config
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["MDMS"])
  }
}

export default useMDMS;