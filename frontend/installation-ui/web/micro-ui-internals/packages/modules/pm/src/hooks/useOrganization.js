import {useQuery, useQueryClient} from "react-query";
import { VendorService } from "../services/Vendor";

const fetchOrganizations = async (filter, limit, offset) => {
  const response = await VendorService.fetchOrganizations(filter, limit, offset);
  return {
    organizations: response?.organisations,
    totalCount: response.TotalCount,
  };
}

const useOrganization = (queryFilter = {}, limit = 1000, offset = 0) => {

  const { } = queryFilter;

  const filter = {
    SearchCriteria: {
      tenantId: Digit.ULBService.getCurrentTenantId(),
    }
  }

  const queryClient = useQueryClient();
  const {isLoading, isError, error, data} = useQuery(
    ["ORGANISATION", filter, limit, offset],
    () => fetchOrganizations(filter, limit, offset),
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["ORGANISATION"])
  }
}

export default useOrganization;