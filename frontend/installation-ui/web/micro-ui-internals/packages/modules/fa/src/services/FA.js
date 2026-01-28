import axios from "axios";
import { IngestionService } from "./Ingestion";

const extractBlobFile = (response) => {
  const disposition = response.headers["content-disposition"];
  const filename = disposition?.split("filename=")[1]?.replace(/"/g, "");

  const blobData = new Blob([response.data], {
    type: response.headers["content-type"],
  });

  return {
    name: filename,
    data: blobData,
  };
};

export const FAService = {
  fetchDocumentDetails: async (fileUrl) => {
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
      "application/zip": "zip",
    };

    const contentType = response.headers["content-type"];
    const fileExtension = mimeToExt[contentType] || "unknown";

    const sizeInBytes = response.headers["content-length"];
    let humanReadable = "";
    if (sizeInBytes) {
      const size = Number(sizeInBytes);
      const i = Math.floor(Math.log(size) / Math.log(1024));
      humanReadable = (size / Math.pow(1024, i)).toFixed(2) * 1 + " " + ["B", "KB", "MB", "GB", "TB"][i];
    }

    return {
      name: fileName,
      fileType: fileExtension,
      size: humanReadable,
    };
  },

  uploadFacilityDataTemplate: async (file) => {

    let validatedFile;
    try {
      const validationRequest = new FormData();
      validationRequest.append("facility_file", file);
      const validationResponse = await IngestionService.validateFacilityData(validationRequest);

      validatedFile = extractBlobFile(validationResponse);
      const errorCount = parseInt(validationResponse.headers["x-error-count"] || "0", 10);
      if (errorCount) {
        return {
          errorCode: "INVALID_DATA",
          file: validatedFile,
          errorCount: errorCount,
        };
      }

    } catch (error) {
      console.error("Error validating facility data", error);

      if (error?.response?.status === 400) {
        return {
          errorCode: "INVALID_TEMPLATE",
        };
      }

      throw error;
    }

    try {
      const uploadRequest = new FormData();
      uploadRequest.append("facility_file", validatedFile.data);
      uploadRequest.append("are_facilities_onm_ready", false);
      const uploadResponse = await IngestionService.uploadFacilityData(uploadRequest);

      const uploadedFile = extractBlobFile(uploadResponse);
      return {
        file: uploadedFile,
      };

    } catch (error) {
      console.error("Error validating facility data", error);
      throw error;
    }
  },
};
