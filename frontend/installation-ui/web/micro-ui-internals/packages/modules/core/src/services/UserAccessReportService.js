import { Request } from "@egovernments/digit-ui-libraries";

const LOGIN_REPORT_APPLICATION = "MANAGEMENT_HUB";

export const UserAccessReportService = {
  userLoginReport: async ({ User, application = LOGIN_REPORT_APPLICATION } = {}) =>
    await Request({
      url: "/im-services/user/login/_report",
      method: "POST",
      auth: true,
      userService: true,
      reqTimestamp: true,
      setTimeParam: false,
      data: {
        User,
        application,
      },
    }),
};
