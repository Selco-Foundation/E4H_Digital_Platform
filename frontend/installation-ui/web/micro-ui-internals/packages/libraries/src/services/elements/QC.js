import { Request } from "../atoms/Utils/Request";
import axios from "axios";

export const QCService = {

  fetchProjects : async (queryFilter, limit, offset) => {
    const endpoint = "/project/v2/_search";
    const params = {
      tenantId : "in",
      offset,
      limit,
      includeAncestors : false,
      includeDescendants : false
    }
    const headers = {
      "Content-Type" : "application/json"
    }

    return await Request({
      url : endpoint,
      data : queryFilter,
      userService : true,
      method : "POST",
      auth : true,
      params : params,
      headers : headers,
    });
  },

  fetchFacilities : async (queryFilter) => {
    const endpoint = "/project/facility/v1/_search";
    const params = {
      tenantId : "in",
      offset : 0,
      limit : 100,
      includeDeleted : false
    }
    const headers = {
      "Content-Type" : "application/json"
    }

    return await Request({
      url : endpoint,
      data : queryFilter,
      method : "POST",
      userService : true,
      auth : true,
      params : params,
      headers : headers,
    });
  },

  fetchInboxData: async (queryFilter) => {
    const endpoint = "/inbox/v2/project/_search";
    const headers = {
      "Content-Type" : "application/json"
    }

    return await Request({
      url : endpoint,
      data : queryFilter,
      method : "POST",
      userService : true,
      auth : true,
      headers : headers,
    });
  },

  fetchAssets : async (facilityID) => {
    const endpoint = "/asset-registry/v1/asset/_search";
    const queryObj = {
      "criteria": {
        "tenantId": "in",
        "facilityID": facilityID
      }
    };
    const params = {
      tenantId : "in",
    }
    const headers = {
      "Content-Type" : "application/json"
    }

    return await Request({
      url : endpoint,
      data : queryObj,
      method : "POST",
      userService : true,
      auth : true,
      params : params,
      headers : headers,
    });
  },

  fetchImageFromFileStore : async (fileStoreId) => {
    const endpoint = "/filestore/v1/files/url";
    const params = {
      tenantId : "in",
      fileStoreIds : fileStoreId
    }

    return await Request({
      url : endpoint,
      method : "GET",
      params : params,
    })
  },

  fetchDocumentSize : async (fileUrl) => {
    const response = await axios.get(fileUrl);
    const sizeInBytes = response.headers["content-length"];

    if (!sizeInBytes) {
      console.info("Content-Length not available");
      return;
    }

    const size = Number(sizeInBytes);
    const i = Math.floor(Math.log(size) / Math.log(1024));
    const humanReadable =
      (size / Math.pow(1024, i)).toFixed(2) * 1 +
      " " +
      ["B", "KB", "MB", "GB", "TB"][i];

    console.info(`File size: ${humanReadable} (${size} bytes)`);
    return humanReadable;
  },

  updateProjectWorkflow : async (projectId, action, comments, workflowComment) => {
    const endpoint = "/project/v1/project/workflow/update";
    const queryObj = {
      projectId: projectId,
      workflow: {
        action: action,
        comment: workflowComment
      },
      transactions: [
        {
          comments: [...comments]
        }
      ]
    }
    const headers = {
      "Content-Type" : "application/json"
    }

    return await Request({
      url : endpoint,
      data : queryObj,
      method : "POST",
      userService : true,
      auth : true,
      headers : headers,
    });
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