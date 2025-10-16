import { Request } from "@egovernments/digit-ui-libraries";
import axios from "axios";

export const QCService = {

  fetchImageFromFileStore : async (fileStoreId) => {
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

  fetchDocumentDetails : async (fileUrl) => {
    const response = await axios.get(fileUrl);

    const contentDisposition = response.headers["content-disposition"];
    let fileName = decodeURIComponent(fileUrl.split("/").pop().split("?")[0]);
    if (contentDisposition) {
      const match = contentDisposition.match(/filename="?([^"]+)"?/);
      if (match) fileName = match[1];
    }

    const mimeToExt = {
      "application/pdf": "pdf",
      "application/msword": "doc",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document": "docx",
      "application/vnd.ms-excel": "xls",
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": "xlsx",
      "text/csv": "csv",
      "image/png": "png",
      "image/jpeg": "jpg",
      "application/zip": "zip"
    };

    const contentType = response.headers["content-type"];
    const fileExtension = mimeToExt[contentType] || "unknown";

    const sizeInBytes = response.headers["content-length"];
    let humanReadable = "";
    if (sizeInBytes) {
      const size = Number(sizeInBytes);
      const i = Math.floor(Math.log(size) / Math.log(1024));
      humanReadable =
        (size / Math.pow(1024, i)).toFixed(2) * 1 +
        " " +
        ["B", "KB", "MB", "GB", "TB"][i];
    }

    return {
      name : fileName,
      fileType: fileExtension,
      size: humanReadable,
    };
  },

  fetchBoundaryRelations : async (codes, boundaryType) => {
    const endpoint = "/boundary-service/boundary-relationships/_search";
    const params = {
      tenantId : "in",
      includeChildren : true,
      includeParents : false,
      hierarchyType: "SELCO",
      boundaryType,
      codes
    }
    const headers = {
      "Content-Type" : "application/json"
    }

    return await Request({
      url : endpoint,
      userService : true,
      method : "POST",
      auth : true,
      params : params,
      headers : headers,
    });
  }

}