import { CustomRequest } from "../components/Custom/CustomRequest";

export const IngestionService = {

  downloadFacilityDataTemplate: async () => {
    const endpoint = "/ingestion-service/template/facilityIngestion";
    const headers = {
      "Content-Type": "application/json"
    }

    await CustomRequest({
      url: endpoint,
      userService: true,
      data: new FormData(),
      method: "POST",
      auth: true,
      headers: headers,
      attachAuthHeaders: true,
      fileDownload: true,
      responseType: "blob",
      attachRequestInfo: (data, RequestInfo) => {
        data.append("request_info", JSON.stringify(RequestInfo));
      },
      defaultFilename: "download.xlsx",
    });
  },

  uploadFacilityData: async (filledFacilityData) => {
    const endpoint = "/ingestion-service/ingest/facilities";

    return await CustomRequest({
      url: endpoint,
      data: filledFacilityData,
      userService: true,
      method: "POST",
      attachAuthHeaders: true,
      auth: true,
      attachRequestInfo: (data, RequestInfo) => {
        data.append("request_info", JSON.stringify(RequestInfo));
      },
      responseType: "blob",
    });
  },

}