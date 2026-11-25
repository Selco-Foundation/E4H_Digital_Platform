import { Request } from "@egovernments/digit-ui-libraries";

/** vendor-registry rejects a Pagination.limit above 100 (@DecimalMax on Pagination.limit). */
export const VENDOR_SEARCH_PAGE_SIZE = 100;

/**
 * Vendor organisations. The Facility Admin works with organisations only; health-facility-registry
 * maps each to its first active Complaint Resolver.
 */
export const VendorOrganisationService = {
  /** One page of active vendor organisations, from vendor-registry. */
  searchActiveVendors: async (tenantId, offset = 0) => {
    return await Request({
      url: "/vendor/organisation/v1/_search",
      userService: true,
      method: "POST",
      auth: true,
      // Pagination goes in the body: this endpoint ignores limit/offset query params.
      params: { tenantId },
      data: {
        SearchCriteria: { tenantId, orgType: "VENDOR", orgStatus: "ACTIVE" },
        Pagination: { limit: VENDOR_SEARCH_PAGE_SIZE, offset },
      },
      headers: { "Content-Type": "application/json" },
    });
  },
};
