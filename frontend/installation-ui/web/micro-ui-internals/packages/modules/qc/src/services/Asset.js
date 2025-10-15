import { Request } from "@egovernments/digit-ui-libraries";
import axios from "axios";
import { CustomRequest } from "../components/CustomRequest";

export const AssetService = {

  fetchAssets : async (queryFilter) => {
    const endpoint = "/asset-registry/v1/asset/_search";
    const params = {
      tenantId : Digit.ULBService.getCurrentTenantId(),
    }
    const headers = {
      "Content-Type" : "application/json"
    }

    return await Request({
      url : endpoint,
      data : queryFilter,
      method : "POST",
      userService : true,
      auth : true,
      params : params,
      headers : headers,
    });
  },

}