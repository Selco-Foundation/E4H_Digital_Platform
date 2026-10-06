import { useQuery, useQueryClient } from "react-query";
import { filterFunctions } from "../useInboxGeneral/newFilterFn";

const MAPPED_VENDOR_BUSINESS_SERVICES = ["Incident_Low", "Incident_Medium", "Incident_High"];

const fetchMappedVendors = async ({ tenantId, filters }) => {
  const { workflowFilters, searchFilters } = filterFunctions.Incident({
    ...filters,
    services: MAPPED_VENDOR_BUSINESS_SERVICES,
  });
  delete searchFilters.mappedVendorName;

  const jurisdictionCurrentBoundaries = Digit.SessionStorage.get("Jurisdiction.CurrentBoundary") || {
    country: ["-"],
  };

  const mappedVendorResponse = await Digit.InboxGeneral.SearchMappedVendors({
    inbox: {
      tenantId,
      processSearchCriteria: workflowFilters,
      jurisdictionSearchCriteria: jurisdictionCurrentBoundaries,
      moduleSearchCriteria: searchFilters,
    },
  });

  return {
    vendors: mappedVendorResponse?.mappedVendors?.map((vendorName) => ({
      code: vendorName,
      name: vendorName,
    })),
    total: mappedVendorResponse?.totalCount,
  };
};

const useMappedVendors = ({ tenantId, filters, enabled = true }) => {
  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["MAPPED_VENDORS", tenantId, filters],
    () => fetchMappedVendors({ tenantId, filters }),
    { enabled }
  );

  return {
    isLoading,
    isError,
    error,
    data,
    revalidate: () => queryClient.invalidateQueries(["MAPPED_VENDORS"]),
  };
};

export default useMappedVendors;
