import { Request } from "@egovernments/digit-ui-libraries";
import Axios from "axios";

export const QCService = {

  fetchFieldPlans : async () => {
    const endpoint = "/project/v2/_search";
    const queryObj = {
      Project : {
        projectTypeId: "FieldPlan"
      }
    };
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
      data : queryObj,
      userService : true,
      method : "POST",
      auth : true,
      params : params,
      headers : headers,
    });
  },

  fetchFacilities : async (...projectIds) => {
    const endpoint = "/project/facility/v1/_search";
    const queryObj = {
      "ProjectFacility": {
        "projectId": [...projectIds]
      }
    };
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
      data : queryObj,
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
      auth : true,
      params : params,
      headers : headers,
    });
  },

  fetchImageFromFileStore : async (fileStoreId) => {
    const endpoint = `/filestore/v1/files/url`;
    const params = {
      tenantId : "in",
      fileStoreIds : fileStoreId
    }

    return await Request({
      url : endpoint,
      method : "GET",
      params : params,
    })
  }

}