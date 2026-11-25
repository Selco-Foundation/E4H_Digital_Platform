import { Request } from "@egovernments/digit-ui-libraries";

/** vendor-registry rejects a Pagination.limit above 100 (@DecimalMax on Pagination.limit). */
export const VENDOR_SEARCH_PAGE_SIZE = 100;

/**
 * Vendor organisations, and which of them a facility is mapped to. The Facility Admin works with
 * organisations only; health-facility-registry maps each to its first active Complaint Resolver.
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

  /** The organisations mapped to a facility: `{facilityId, organisations: [{id, code, name}]}`. */
  searchFacilityVendors: async (tenantId, facilityId) => {
    return await Request({
      url: "/facility-service/v2/facility/vendor-organisations/_search",
      userService: true,
      method: "POST",
      auth: true,
      data: { tenantId, facilityId },
      headers: { "Content-Type": "application/json" },
    });
  },

  /**
   * Sets the organisations mapped to a facility. `organisationCodes` is the full desired list; an
   * empty list unmaps them all. Returns the resulting mapping, in the same shape as the search.
   */
  updateFacilityVendors: async (tenantId, facilityId, organisationCodes) => {
    return await Request({
      url: "/facility-service/v2/facility/vendor-organisations/_update",
      userService: true,
      method: "POST",
      auth: true,
      data: { tenantId, facilityId, organisationCodes },
      headers: { "Content-Type": "application/json" },
    });
  },
};
