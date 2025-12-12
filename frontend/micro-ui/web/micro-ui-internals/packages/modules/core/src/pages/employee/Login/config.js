export const loginConfig = [
  {
    texts: {
      submitButtonLabel: "CORE_COMMON_CONTINUE",
      secondaryButtonLabel: "",
    },
    inputs: [
      {
        label: "CORE_LOGIN_USERNAME",
        type: "text",
        name: "username",
        error: "ERR_HRMS_INVALID_USER_ID",
      },
      {
        label: "CORE_LOGIN_PASSWORD",
        type: "password",
        name: "password",
        error: "ERR_HRMS_WRONG_PASSWORD",
      },
    ],
  },
];
