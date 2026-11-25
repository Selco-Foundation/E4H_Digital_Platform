import { useQuery, useQueryClient } from "react-query";
import { VendorOrganisationService } from "../services/VendorOrganisation";
import { toVendorOptions } from "../utilities/VendorOrganisations";

/** The vendor organisations mapped to one facility, as `{code, name}` sorted by name. */
const useFacilityVendors = (facilityId) => {
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const queryClient = useQueryClient();
  const { isLoading, isFetching, isError, error, data } = useQuery(
    ["FA_FACILITY_VENDORS", tenantId, facilityId],
    async () => toVendorOptions((await VendorOrganisationService.searchFacilityVendors(tenantId, facilityId))?.organisations),
    { enabled: !!tenantId && !!facilityId }
  );
  return {
    isLoading,
    isFetching,
    isError,
    error,
    data: data || [],
    revalidate: () => queryClient.invalidateQueries(["FA_FACILITY_VENDORS"]),
  };
};

export default useFacilityVendors;
