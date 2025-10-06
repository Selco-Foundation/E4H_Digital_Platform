import { IngestionService } from "./Ingestion";

export const PMService = {

  downloadProjectFacilityDataTemplate: async (projectId, boundaryData, t) => {

    const formatDistricts = (districts, blocks) => {
      const formattedDistricts = [];

      districts.forEach((district) => {
        const formattedDistrict = {
          boundaryCode: district.code,
          type: "district",
          name: t(district.name),
          children: [
            ...blocks
              .filter((block) => block.districtCode === district.code)
              .map((block) => ({
                boundaryCode: block.code,
                type: "block",
                name: t(block.name),
              }))
          ]
        }

        formattedDistricts.push(formattedDistrict)
      })

      return formattedDistricts;
    }

    const formattedBoundaryData = {
      boundaryCode: "India",
      type: "country",
      name: "India",
      tenantId: "in",
      children: [
        {
          boundaryCode: boundaryData.state.code,
          type: "state",
          name: t(boundaryData.state.name),
          children: [
            ...formatDistricts(boundaryData.districts, boundaryData.blocks)
          ]
        }
      ]
    }

    return await IngestionService.downloadProjectFacilityDataTemplate({
      boundary_data: formattedBoundaryData,
      project_id: projectId,
    });
  },

  uploadProjectFacilityDataTemplate: async (file, projectId) => {

    const extractBlobFile = (response) => {
      const disposition = response.headers["content-disposition"];
      const filename = disposition?.split("filename=")[1]?.replace(/"/g, "");

      const blobData = new Blob([response.data], {
        type: response.headers["content-type"],
      });

      return {
        name: filename,
        data: blobData,
      }
    }

    let validatedFile;

    try {
      const validationRequest = new FormData();
      validationRequest.append("facility_file", file);
      validationRequest.append("project_id", projectId);
      const validationResponse = await IngestionService.validateProjectFacilityData(validationRequest);

      validatedFile = extractBlobFile(validationResponse);
      const errorCount = parseInt(validationResponse.headers["x-error-count"] || "0", 10);
      if (errorCount) {
        return {
          errorCode: "INVALID_DATA",
          file: validatedFile,
          errorCount: errorCount
        };
      }

    } catch (error) {
      console.error("Error validating facility data", error);

      if (error?.response?.status === 400) {
        return {
          errorCode: "INVALID_TEMPLATE",
        }
      }

      throw error;
    }

    try {
      const uploadRequest = new FormData();
      uploadRequest.append("facility_file", validatedFile.data);
      uploadRequest.append("project_id", projectId);
      const uploadResponse = await IngestionService.uploadProjectFacilityData(uploadRequest)

      const uploadedFile = extractBlobFile(uploadResponse);
      return {
        file: uploadedFile,
      };

    } catch (error) {
      console.error("Error uploading facility data", error);
      throw error;
    }
  },

  downloadFieldPlanFacilityDataTemplate: async (fieldPlanId, boundaryData, t) => {

    const formatDistricts = (districts, blocks) => {
      const formattedDistricts = [];

      districts.forEach((district) => {
        const formattedDistrict = {
          boundaryCode: district.code,
          type: "district",
          name: t(`DISTRICT_${district.code.toUpperCase()}`),
          children: [
            ...blocks
              .filter((block) => block.districtCode === district.code)
              .map((block) => ({
                boundaryCode: block.code,
                type: "block",
                name: t(`BLOCK_${block.code.toUpperCase()}`),
              }))
          ]
        }

        formattedDistricts.push(formattedDistrict)
      })

      return formattedDistricts;
    }

    const formattedBoundaryData = {
      boundaryCode: "India",
      type: "country",
      name: "India",
      tenantId: "in",
      children: [
        {
          boundaryCode: boundaryData.state.code,
          type: "state",
          name: t(`STATE_${boundaryData.state.code.toUpperCase()}`),
          children: [
            ...formatDistricts(boundaryData.districts, boundaryData.blocks)
          ]
        }
      ]
    }

    return await IngestionService.downloadFieldPlanFacilityDataTemplate({
      boundary_data: formattedBoundaryData,
      field_plan_id: fieldPlanId,
    });
  },

  uploadFieldPlanFacilityDataTemplate: async (file, fieldPlanId) => {

    const extractBlobFile = (response) => {
      const disposition = response.headers["content-disposition"];
      const filename = disposition?.split("filename=")[1]?.replace(/"/g, "");

      const blobData = new Blob([response.data], {
        type: response.headers["content-type"],
      });

      return {
        name: filename,
        data: blobData,
      }
    }

    let validatedFile;

    try {
      const validationRequest = new FormData();
      validationRequest.append("facility_file", file);
      const validationResponse = await IngestionService.validateFieldPlanFacilityData(validationRequest);

      validatedFile = extractBlobFile(validationResponse);
      const errorCount = parseInt(validationResponse.headers["x-error-count"] || "0", 10);
      if (errorCount) {
        return {
          errorCode: "INVALID_DATA",
          file: validatedFile,
          errorCount: errorCount
        };
      }

    } catch (error) {
      console.error("Error validating facility data", error);

      if (error?.response?.status === 400) {
        return {
          errorCode: "INVALID_TEMPLATE",
        }
      }

      throw error;
    }

    try {
      const uploadRequest = new FormData();
      uploadRequest.append("facility_file", validatedFile.data);
      uploadRequest.append("fieldplan_id", fieldPlanId);
      const uploadResponse = await IngestionService.uploadFieldPlanFacilityData(uploadRequest)

      const uploadedFile = extractBlobFile(uploadResponse);
      return {
        file: uploadedFile,
      };

    } catch (error) {
      console.error("Error uploading facility data", error);
      throw error;
    }
  },
}