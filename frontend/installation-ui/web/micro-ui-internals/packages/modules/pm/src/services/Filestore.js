import { Request } from "@egovernments/digit-ui-libraries";
import { CustomRequest } from "../components/Custom/CustomRequest";

export const FilestoreService = {

  fetchDocumentFromFilestore: async (fileStoreId) => {
    const endpoint = "/filestore/v1/files/url";
    const params = {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      fileStoreIds: fileStoreId,
    };

    return await Request({
      url: endpoint,
      method: "GET",
      params: params,
    });
  },

  downloadFileFromFilestore: async (fileStoreId, defaultFilename = "icc-template.xlsx") => {
    const endpoint = "/filestore/v1/files/file";
    const params = {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      fileStoreId: fileStoreId,
    };

    return await CustomRequest({
      url: endpoint,
      method: "GET",
      params: params,
      fileDownload: true,
      responseType: "blob",
      defaultFilename: defaultFilename,
      attachAuthHeaders: true,
      setTimeParam: false,
      noRequestInfo: true,
    });
  },

}
