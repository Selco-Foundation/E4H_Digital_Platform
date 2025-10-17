import { Request } from "@egovernments/digit-ui-libraries";

export const FilestoreService = {

  fetchDocumentFromFilestore : async (fileStoreId) => {
    const endpoint = "/filestore/v1/files/url";
    const params = {
      tenantId : Digit.ULBService.getCurrentTenantId(),
      fileStoreIds : fileStoreId
    }

    return await Request({
      url : endpoint,
      method : "GET",
      params : params,
    })
  },

}