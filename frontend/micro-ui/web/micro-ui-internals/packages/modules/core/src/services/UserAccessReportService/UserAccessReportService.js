import { Request } from "@egovernments/digit-ui-libraries";

// Application identifier tagged on every login-access report raised by this deployment.
const LOGIN_REPORT_APPLICATION = "SAURA_EMITRA";

export const UserAccessReportService = {
  // Reports a successful user login to the IM audit/access-report service.
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
