import { CustomRequest } from "../components/Custom/CustomRequest";

export const ICCService = {

  searchICCTemplates: async (systemType) => {
    const endpoint = "/field-planner/v1/field-plans/icc-report/_search";
    const ts = Date.now();
    const data = {
      RequestInfo: {
        apiId: "project-api",
        ver: "1.0",
        ts: ts,
        action: "create",
        did: "device-id",
        key: "api-key",
        msgId: `msg-${ts}`,
        authToken: Digit.UserService.getUser()?.access_token || null,
        correlationId: `corr-id-${ts}`,
        plainAccessRequest: null,
        userInfo: Digit.UserService.getUser()?.info,
      },
    };

    if (systemType) {
      data.systemType = systemType;
    }

    const response = await CustomRequest({
      url: endpoint,
      data: data,
      userService: true,
      method: "POST",
      auth: true,
      headers: {
        "Content-Type": "application/json",
      },
      attachAuthHeaders: true,
      setTimeParam: false,
      noRequestInfo: true,
    });

    return response?.data?.iccTemplates || response?.iccTemplates || [];
  },

}
