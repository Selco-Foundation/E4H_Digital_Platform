import { useQuery } from "react-query";
import { VENDOR_SEARCH_PAGE_SIZE, VendorOrganisationService } from "../services/VendorOrganisation";
import { toVendorOptions } from "../utilities/VendorOrganisations";

/** Every active vendor organisation, as `{code, name}` options sorted by name. */
const fetchAllActiveVendors = async (tenantId) => {
  const organisations = [];
  for (let offset = 0; ; offset += VENDOR_SEARCH_PAGE_SIZE) {
    const page = (await VendorOrganisationService.searchActiveVendors(tenantId, offset))?.organisations || [];
    organisations.push(...page);
    if (page.length < VENDOR_SEARCH_PAGE_SIZE) return toVendorOptions(organisations);
  }
};

const useVendorOrganisations = ({ enabled = true } = {}) => {
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const { isLoading, isFetching, isError, error, data, refetch } = useQuery(
    ["FA_VENDOR_ORGANISATIONS", tenantId],
    () => fetchAllActiveVendors(tenantId),
    { enabled: !!tenantId && enabled, staleTime: 5 * 60 * 1000 }
  );
  return { isLoading, isFetching, isError, error, data: data || [], revalidate: refetch };
};

export default useVendorOrganisations;
