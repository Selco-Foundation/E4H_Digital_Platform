import { Request } from "@egovernments/digit-ui-libraries";

export const VendorService = {

  fetchOrganizations: async (queryObject, limit = 1000, offset = 0) => {
    const endpoint = "/vendor/organisation/v1/_search";
    const params = {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      limit,
      offset,
    }
    const headers = {
      "Content-Type" : "application/json"
    }

    return await Request({
      url : endpoint,
      data: queryObject,
      userService : true,
      method : "POST",
      auth : true,
      params : params,
      headers : headers,
    });
  },

  fetchOrganizationUsers: async (queryObject, limit = 1000, offset = 0) => {
    const endpoint = "/vendor/organisation/v1/user/_search";
    const params = {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      limit,
      offset,
    }
    const headers = {
      "Content-Type" : "application/json"
    }

    return await Request({
      url : endpoint,
      data: queryObject,
      userService : true,
      method : "POST",
      auth : true,
      params : params,
      headers : headers,
    });
  },

}