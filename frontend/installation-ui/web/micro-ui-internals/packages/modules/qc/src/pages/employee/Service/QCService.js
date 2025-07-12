import { Request } from "@egovernments/digit-ui-libraries";
import Axios from "axios";

export const QCService = {

  fetchProjects : async (queryFilter) => {
    const endpoint = "/project/v2/_search";
    const params = {
      tenantId : "in",
      offset : 0,
      limit : 100,
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

  updateProjectWorkflow : async (projectId, action, comment) => {
    const endpoint = "/project/v1/project/workflow/update";
    const queryObj = {
      projectId: projectId,
      workflow: {
        action: action,
        comment: comment
      },
      transactions: [
        {
          comments: [
            {
              commentMessage: comment
            }
          ]
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
  }

}