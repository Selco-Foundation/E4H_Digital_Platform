import Axios from "axios";

Axios.interceptors.response.use(
  (res) => res,
  (err) => {
    const isEmployee = window.location.pathname.split("/").includes("employee");
    if (err?.response?.data?.Errors) {
      for (const error of err.response.data.Errors) {
        if (error.message.includes("InvalidAccessTokenException")) {
          localStorage.clear();
          sessionStorage.clear();
          window.location.href =
            (isEmployee ? `/${window?.contextPath}/employee/user/login` : `/${window?.contextPath}/citizen/login`) +
            `?from=${encodeURIComponent(window.location.pathname + window.location.search)}`;
        } else if (
          error?.message?.toLowerCase()?.includes("internal server error") ||
          error?.message?.toLowerCase()?.includes("some error occured")
        ) {
          window.location.href =
            (isEmployee ? `/${window?.contextPath}/employee/user/error` : `/${window?.contextPath}/citizen/error`) +
            `?type=maintenance&from=${encodeURIComponent(window.location.pathname + window.location.search)}`;
        } else if (error.message.includes("ZuulRuntimeException")) {
          window.location.href =
            (isEmployee ? `/${window?.contextPath}/employee/user/error` : `/${window?.contextPath}/citizen/error`) +
            `?type=notfound&from=${encodeURIComponent(window.location.pathname + window.location.search)}`;
        }
      }
    }
    throw err;
  }
);

const requestInfo = () => ({
  authToken: Digit.UserService.getUser()?.access_token || null,
});

export const authHeaders = () => ({
  "auth-token": Digit.UserService.getUser()?.access_token || null,
});

const userServiceData = () => ({ userInfo: Digit.UserService.getUser()?.info });

window.Digit = window.Digit || {};
window.Digit = { ...window.Digit, RequestCache: window.Digit.RequestCache || {} };
export const CustomRequest = ({
  method = "POST",
  url,
  data = {},
  headers = {},
  params = {},
  auth=true,
  urlParams = {},
  userService,
  locale = true,
  authHeader = false,
  setTimeParam = true,
  userDownload = false,
  noRequestInfo = false,
  reqTimestamp = false,
  responseType = null,
}) => {

  const ts = new Date().getTime();

  if (method.toUpperCase() === "POST") {
    data.RequestInfo = {
      apiId: "Rainmaker",
    };

    if (auth) {
      data.RequestInfo = { ...data.RequestInfo, ...requestInfo() };
    }
    if (userService) {
      data.RequestInfo = { ...data.RequestInfo, ...userServiceData() };
    }
    if (locale) {
      data.RequestInfo = { ...data.RequestInfo, msgId: `${ts}|${Digit.StoreData.getCurrentLanguage()}` };
    }

    if (noRequestInfo) {
      delete data.RequestInfo;
    }

    const privacy = Digit.Utils.getPrivacyObject();
    if (privacy && !url.includes("/edcr/rest/dcr/")) {
      if(!noRequestInfo){
        data.RequestInfo = { ...data.RequestInfo, plainAccessRequest: { ...privacy } };
      }
    }
  }

  const headers1 = {
    "Content-Type": "application/json",
    Accept: window?.globalConfigs?.getConfig("ENABLE_SINGLEINSTANCE") ? "application/pdf,application/json" : "application/pdf",
  };

  if (authHeader) headers = { ...headers, ...authHeaders() };

  if (userDownload) headers = { ...headers, ...headers1 };

  if (setTimeParam) {
    params._ = Date.now();
  }

  if (reqTimestamp) {
    data.RequestInfo = { ...data.RequestInfo, ts: Number(ts) };
  }

  let _url = url
    .split("/")
    .map((path) => {
      let key = path.split(":")?.[1];
      return urlParams[key] ? urlParams[key] : path;
    })
    .join("/");

  return Axios({ method, url: _url, data, params, headers, responseType: responseType });
};