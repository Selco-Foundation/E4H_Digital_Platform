import { CustomRequest } from "../components/Custom/CustomRequest";

export const IngestionService = {

  downloadProjectFacilityDataTemplate : async (boundaryData) => {
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

  validateProjectFacilityData: async (filledFacilityData) => {
    const endpoint = "/ingestion-service/ingest/facilitiesValidateData";

    return await CustomRequest({
      url : endpoint,
      data : filledFacilityData,
      userService : true,
      method : "POST",
      attachAuthHeaders: true,
      auth : true,
      attachRequestInfo: (data, RequestInfo) => {data.append("request_info", JSON.stringify(RequestInfo))},
      responseType: "blob",
    })
  },

  uploadProjectFacilityData : async (validatedFacilityData) => {
    const endpoint = "/ingestion-service/ingest/createFacilityAndUpdateProject";

    return await CustomRequest({
      url : endpoint,
      data : validatedFacilityData,
      userService : true,
      method : "POST",
      attachAuthHeaders: true,
      auth : true,
      attachRequestInfo: (data, RequestInfo) => {data.append("request_info", JSON.stringify(RequestInfo))},
      responseType: "blob",
    });
  },

  downloadFieldPlanFacilityDataTemplate : async (boundaryData) => {
    const endpoint = "/ingestion-service/template/fieldplanFacilityIngestionTemplate";
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

  validateFieldPlanFacilityData: async (filledFacilityData) => {
    const endpoint = "/ingestion-service/ingest/fieldPlanfacilitiesValidateData";

    return await CustomRequest({
      url : endpoint,
      data : filledFacilityData,
      userService : true,
      method : "POST",
      attachAuthHeaders: true,
      auth : true,
      attachRequestInfo: (data, RequestInfo) => {data.append("request_info", JSON.stringify(RequestInfo))},
      responseType: "blob",
    })
  },

  uploadFieldPlanFacilityData : async (validatedFacilityData) => {
    const endpoint = "/ingestion-service/ingest/createFieldPlanFacility";

    return await CustomRequest({
      url : endpoint,
      data : validatedFacilityData,
      userService : true,
      method : "POST",
      attachAuthHeaders: true,
      auth : true,
      attachRequestInfo: (data, RequestInfo) => {data.append("request_info", JSON.stringify(RequestInfo))},
      responseType: "blob",
    });
  },
}