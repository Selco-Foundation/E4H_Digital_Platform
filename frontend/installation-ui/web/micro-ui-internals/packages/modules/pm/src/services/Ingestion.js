import { CustomRequest } from "../components/Custom/CustomRequest";

export const IngestionService = {

  downloadTemplateFile : async (boundaryData) => {
    const endpoint = "/ingestion-service/template/facilityIngestionTemplateWithData";
    const headers = {
      "Content-Type" : "application/json"
    }

    await CustomRequest({
      url : endpoint,
      data : boundaryData,
      userService : true,
      method : "POST",
      auth : true,
      headers : headers,
      fileDownload: true,
      responseType: "blob",
      defaultFilename: "download.xlsx"
    });
  },

  validateFacilityDataTemplate: async (filledFacilityData) => {
    const endpoint = "/ingestion-service/ingest/facilitiesValidateData";

    return await CustomRequest({
      url : endpoint,
      data : filledFacilityData,
      userService : true,
      method : "POST",
      auth : true,
      customRequestInfo: (data, RequestInfo) => {data.append("request_info", JSON.stringify(RequestInfo))},
      responseType: "blob",
    })
  },

  uploadTemplateFile : async (validatedFacilityData) => {
    const endpoint = "/ingestion-service/ingest/createFacilityAndUpdateProject";

    return await CustomRequest({
      url : endpoint,
      data : validatedFacilityData,
      userService : true,
      method : "POST",
      auth : true,
      customRequestInfo: (data, RequestInfo) => {data.append("request_info", JSON.stringify(RequestInfo))},
      responseType: "blob",
    });
  }

}