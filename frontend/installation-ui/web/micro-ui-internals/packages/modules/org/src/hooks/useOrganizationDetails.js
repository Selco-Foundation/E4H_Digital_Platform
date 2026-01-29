import {useQuery, useQueryClient} from "react-query";
import { VendorService } from "../services/Vendor";

const fetchOrganizations = async (filter, limit, offset) => {
  const response = await VendorService.fetchOrganizations(filter, limit, offset);
  return {
    ...(response?.organisations?.[0] || {})
  };
}

const useOrganizationDetails = (queryFilter = {}, limit = 1000, offset = 0) => {

  const { tenantId, id } = queryFilter;

  const filter = {
    SearchCriteria: {
      tenantId: Digit.ULBService.getCurrentTenantId(),
    }
  }

  if (tenantId) {
    filter.SearchCriteria.tenantId = tenantId;
  }

  if (id) {
    filter.SearchCriteria.id = id;
  }

  const queryClient = useQueryClient();
  const {isLoading, isError, error, data} = useQuery(
    ["ORGANISATION_DETAILS", filter, limit, offset],
    () => fetchOrganizations(filter, limit, offset),
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["ORGANISATION_DETAILS"])
  }
}

export default useOrganizationDetails;