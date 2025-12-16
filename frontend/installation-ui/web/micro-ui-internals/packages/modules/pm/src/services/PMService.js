import {IngestionService} from "./Ingestion";

const formatBoundaryData = (boundaryData, t) => {
  const formatDistricts = (districts, blocks) => {
    const formattedDistricts = [];

    districts.forEach((district) => {
      const formattedDistrict = {
        boundaryCode: district.code,
        type: "district",
        name: t(`Boundary_${district.code}`),
        children: [
          ...blocks
            .filter((block) => block.districtCode === district.code)
            .map((block) => ({
              boundaryCode: block.code,
              type: "block",
              name: t(`Boundary_${block.code}`),
            }))
        ]
      }

      formattedDistricts.push(formattedDistrict)
    })

    return formattedDistricts;
  }

  return {
    boundaryCode: "India",
    type: "country",
    name: "India",
    tenantId: "in",
    children: [
      {
        boundaryCode: boundaryData.state.code,
        type: "state",
        name: t(`Boundary_${boundaryData.state.code}`),
        children: [
          ...formatDistricts(boundaryData.districts, boundaryData.blocks)
        ]
      }
    ]
  };
}

const formatActivityOrganizationUsers = (activityAssignments = []) => {
  const formattedActivityOrganizationUsers = [];

  activityAssignments.forEach((activityAssignment) => {
    const activity = {
      code: activityAssignment.activity.code,
      name: activityAssignment.activity.name,
    }
    const organizationToUsersMap = new Map();
    for (const userAssignment of activityAssignment.users) {
      if (!userAssignment.organization?.value?.id) continue;
      const vendorObject = organizationToUsersMap.get(userAssignment.organization.value.id) || {
        vendorId: userAssignment.organization.value.id,
        vendor: userAssignment.organization.value.name,
        users: [],
      };

      vendorObject.users = [...vendorObject.users, userAssignment.email.value];
      organizationToUsersMap.set(userAssignment.organization.value.id, vendorObject);
    }

    formattedActivityOrganizationUsers.push({
      ...activity,
      organizationUsers: [...organizationToUsersMap.values()],
    })
  })

  return formattedActivityOrganizationUsers;
}

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

  downloadFieldPlanFacilityDataTemplate: async (projectId, fieldPlanId, boundaryData, t) => {
    return await IngestionService.downloadFieldPlanFacilityDataTemplate({
      boundary_data: formatBoundaryData(boundaryData, t),
      fieldplan_id: fieldPlanId,
      project_id: projectId,
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
      validationRequest.append("fieldplan_id", fieldPlanId);
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

  downloadAMCFacilityDataTemplate: async (projectId, amcFormData, t) => {

    const boundaryData = amcFormData.geographyDetails;
    const activityAssignments = amcFormData?.activityDetails?.activityUserAssignment;

    const formattedActivityOrganizationUsers = formatActivityOrganizationUsers(activityAssignments);
    const userInfoList = [];
    for (const formattedActivityOrganizationUser of formattedActivityOrganizationUsers) {
      formattedActivityOrganizationUser.organizationUsers.forEach((orgUser) => userInfoList.push(({
        ...orgUser,
        activityCode: formattedActivityOrganizationUser.code,
      })));
    }

    return await IngestionService.downloadAMCFacilityDataTemplate({
      boundary_data: formatBoundaryData(boundaryData, t),
      user_info_list: userInfoList,
      project_id: projectId,
    });
  },

  uploadAMCFacilityDataTemplate: async (file, projectId, amcFormData) => {
    const formattedActivityOrganizationUsers = formatActivityOrganizationUsers(amcFormData.activityDetails.activityUserAssignment);
    const userInfoList = [];
    for (const formattedActivityOrganizationUser of formattedActivityOrganizationUsers) {
      formattedActivityOrganizationUser.organizationUsers.forEach((orgUser) => userInfoList.push(({
        ...orgUser,
        activityCode: formattedActivityOrganizationUser.code,
      })));
    }

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
      validationRequest.append("amc_file", file);
      validationRequest.append("project_id", projectId);
      validationRequest.append("user_info_list", JSON.stringify(userInfoList));
      const validationResponse = await IngestionService.validateAMCFacilityData(validationRequest);

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
      uploadRequest.append("amc_file", validatedFile.data);
      uploadRequest.append("project_id", projectId);
      uploadRequest.append("user_info_list", JSON.stringify(userInfoList));
      const uploadResponse = await IngestionService.uploadAMCFacilityData(uploadRequest)

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