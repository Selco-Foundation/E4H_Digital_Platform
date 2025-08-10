import { Request } from "@egovernments/digit-ui-libraries";
import Axios from "axios";

const url = "https://e4h-dev.selcofoundation.org/project/v2/_search";

export const QCService = {

  fetchFieldPlans : async (userInfo, authToken, ) => {
    const endpoint = "/project/v2/_search";
    const queryObj = {
      "RequestInfo" : {
        "apiId": "project-api",
        "ver": "1.0",
        "ts": Date.now(),
        "action": "search",
        "did": "1",
        "key": "",
        "authToken": authToken,
        "userInfo": userInfo?.info
      },
      "Project" : {
        "projectTypeId": "FieldPlan"
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
    console.log(queryObj);
    return await Request({
      url : endpoint,
      data : queryObj,
      method : "POST",
      auth : true,
      params : params,
      headers : headers,
    });
    // return await Axios({
    //   method : "POST",
    //   url : url,
    //   data : queryObj,
    //   params : params,
    //   headers : headers,
    //   auth : true,
    //   useCache : false
    // });
  },

  fetchFacilities : async (userInfo, authToken, projectIds) => {
    const endpoint = "/project/facility/v1/_search";
    const queryObj = {
      "RequestInfo" : {
        "apiId": "project-api",
        "ver": "1.0",
        "ts": Date.now(),
        "action": "search",
        "did": "1",
        "key": "",
        "authToken": authToken,
        "userInfo": userInfo?.info
      },
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
    console.log(queryObj);
    return await Request({
      url : endpoint,
      data : queryObj,
      method : "POST",
      auth : true,
      params : params,
      headers : headers,
    });
    // return await Axios({
    //   method : "POST",
    //   url : url,
    //   data : queryObj,
    //   params : params,
    //   headers : headers,
    //   auth : true,
    //   useCache : false
    // });
  },

  fetchAssets : async (userInfo, authToken, criteria) => {
    const endpoint = "/asset-registry/v1/asset/_search";
    const queryObj = {
      "RequestInfo" : {
        "authToken": authToken,
      },
      "criteria": {...criteria}
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
    console.log(queryObj);
    return await Request({
      url : endpoint,
      data : queryObj,
      method : "POST",
      auth : true,
      params : params,
      headers : headers,
    });
    // return await Axios({
    //   method : "POST",
    //   url : url,
    //   data : queryObj,
    //   params : params,
    //   headers : headers,
    //   auth : true,
    //   useCache : false
    // });
  }
}