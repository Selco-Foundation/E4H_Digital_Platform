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

  downloadAssessmentFacilityDataTemplate : async (requestData) => {
    const endpoint = "/ingestion-service/template/assessmentPlanIncludeTemplate";
    const headers = {
      "Content-Type" : "application/json"
    }

    await CustomRequest({
      url : endpoint,
      data : requestData,
      userService : true,
      method : "POST",
      auth : true,
      headers : headers,
      fileDownload: true,
      responseType: "blob",
      defaultFilename: "download.xlsx"
    });
  },

  validateAssessmentPlanFacilityData: async (filledFacilityData) => {
    const endpoint = "/ingestion-service/ingest/assessmentPlanIncludeValidateData";

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

  uploadAssessmentPlanFacilityData : async (validatedFacilityData) => {
    const endpoint = "/ingestion-service/ingest/assessmentPlanIncludeApply";

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

  uploadICCReports : async (iccReportsData) => {
    const endpoint = "/ingestion-service/ingest/icc-reports";

    return await CustomRequest({
      url : endpoint,
      data : iccReportsData,
      userService : true,
      method : "POST",
      attachAuthHeaders: true,
      auth : true,
      attachRequestInfo: (data, RequestInfo) => {data.append("request_info", JSON.stringify(RequestInfo))},
    });
  },

  upsertICCReports : async (iccReportsData) => {
    const endpoint = "/ingestion-service/ingest/icc-reports/_update";

    return await CustomRequest({
      url : endpoint,
      data : iccReportsData,
      userService : true,
      method : "POST",
      attachAuthHeaders: true,
      auth : true,
      attachRequestInfo: (data, RequestInfo) => {data.append("request_info", JSON.stringify(RequestInfo))},
    });
  },

  downloadAMCFacilityDataTemplate : async (requestData) => {
    const endpoint = "/ingestion-service/template/amcConfigurationTemplate";
    const headers = {
      "Content-Type" : "application/json"
    }

    await CustomRequest({
      url : endpoint,
      data : requestData,
      userService : true,
      method : "POST",
      auth : true,
      headers : headers,
      fileDownload: true,
      responseType: "blob",
      defaultFilename: "download.xlsx"
    });
  },

  validateAMCFacilityData: async (filledFacilityData) => {
    const endpoint = "/ingestion-service/ingest/amcConfigurationValidateData";

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

  uploadAMCFacilityData : async (validatedFacilityData) => {
    const endpoint = "/ingestion-service/ingest/amcConfigurationBulkIngest";

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
