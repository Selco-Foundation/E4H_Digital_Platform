import { Request } from "@egovernments/digit-ui-libraries";

export const IngestionService = {

  downloadTemplateFile : async (boundaryCodes) => {
    const endpoint = "/ingestion-service/_download";
    const params = {
      tenantId : "in",
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
  },

  uploadTemplateFile : async (boundaryCodes, succeed, code) => {
    const endpoint = "/ingestion-service/_download";
    const params = {
      tenantId : "in",
    }
    const headers = {
      "Content-Type" : "application/json"
    }

    async function fakeApiCall(shouldSucceed = true, errorCode, delay = 4000) {
      return new Promise((resolve, reject) => {
        setTimeout(() => {
          if (shouldSucceed) {
            resolve({
              status: 200,
              message: "Request successful",
              data: { id: 1, name: "Dummy User" },
            });
          } else {
            reject({
              status: 400,
              message: "Something went wrong",
              data: {
                code: errorCode,
                invalidFacilitiesCount: 48
              }
            });
          }
        }, delay);
      });
    }

    return await fakeApiCall(succeed, code);
    // const { data: { files: fileStoreIds } = {} } = await Digit.UploadServices.MultipleFilesStorage(module, e.target.files, tenantId)
    // return await Request({
    //   url : endpoint,
    //   userService : true,
    //   method : "POST",
    //   auth : true,
    //   params : params,
    //   headers : headers,
    // });
  }

}