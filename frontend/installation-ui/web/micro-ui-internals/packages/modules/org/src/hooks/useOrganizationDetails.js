import {useQuery, useQueryClient} from "react-query";
import { VendorService } from "../services/Vendor";

const fetchOrganizations = async (filter, limit, offset) => {
  const response = await VendorService.fetchOrganizations(filter, limit, offset);
  return {
    ...(response?.organisations?.[0] || {})
  };
}

// limit 1: this looks one organisation up by id and reads organisations[0]. The old default
// of 1000 was ignored as a query param; as Pagination it would now breach the server's cap.
const useOrganizationDetails = (queryFilter = {}, limit = 1, offset = 0) => {

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