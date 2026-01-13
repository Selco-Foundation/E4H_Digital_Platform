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

const authHeaders = () => ({
  "auth-token": Digit.UserService.getUser()?.access_token || null,
  tenantId: Digit.ULBService.getCurrentTenantId(),
});

const userServiceData = () => ({ userInfo: Digit.UserService.getUser()?.info });

window.Digit = window.Digit || {};
window.Digit = { ...window.Digit, RequestCache: window.Digit.RequestCache || {} };

const getFilename = (disposition, fallback) => {
  if (!disposition || typeof disposition !== "string") {
    return fallback;
  }

  try {
    // Try RFC5987 first (filename*=UTF-8''...)
    const rfc5987Match = disposition.match(/filename\*=UTF-8''([^;,\s]+)/i);
    if (rfc5987Match) {
      return decodeURIComponent(rfc5987Match[1]);
    }

    // Try RFC2183/RFC2616 (filename="..." or filename=...)
    const standardMatch = disposition.match(/filename\s*=\s*"?([^";,\s]+)"?/i);
    if (standardMatch) {
      return standardMatch[1].trim();
    }

    return fallback;
  } catch (error) {
    console.warn("Error parsing Content-Disposition filename:", error);
    return fallback;
  }
};

export const CustomRequest = async ({
  method = "POST",
  url,
  data = {},
  headers = {},
  params = {},
  auth = true,
  urlParams = {},
  userService,
  locale = true,
  attachAuthHeaders = false,
  setTimeParam = true,
  fileDownload = false,
  defaultFilename,
  responseType,
  noRequestInfo = false,
  reqTimestamp = false,
  attachRequestInfo = (data, RequestInfo) => {
    data.RequestInfo = RequestInfo;
  },
}) => {
  const ts = new Date().getTime();

  if (method.toUpperCase() === "POST" && !noRequestInfo) {
    let RequestInfo = {
      apiId: "Rainmaker",
    };

    if (auth) {
      RequestInfo = { ...RequestInfo, ...requestInfo() };
    }
    if (userService) {
      RequestInfo = { ...RequestInfo, ...userServiceData() };
    }
    if (locale) {
      RequestInfo = { ...RequestInfo, msgId: `${ts}|${Digit.StoreData.getCurrentLanguage()}` };
    }
    if (reqTimestamp) {
      RequestInfo = { ...RequestInfo, ts: Number(ts) };
    }

    const privacy = Digit.Utils.getPrivacyObject();
    if (privacy && !url.includes("/edcr/rest/dcr/")) {
      RequestInfo = { ...RequestInfo, plainAccessRequest: { ...privacy } };
    }

    attachRequestInfo(data, RequestInfo);
  }

  if (attachAuthHeaders) headers = { ...headers, ...authHeaders() };

  if (setTimeParam) {
    params._ = Date.now();
  }

  let _url = url
    .split("/")
    .map((path) => {
      let key = path.split(":")?.[1];
      return urlParams[key] ? urlParams[key] : path;
    })
    .join("/");

  const response =
    fileDownload || responseType
      ? await Axios({ method, url: _url, data, params, headers, responseType: responseType || "blob" })
      : await Axios({ method, url: _url, data, params, headers });

  if (fileDownload) {
    const blob = new Blob([response.data], {
      type: response.headers["content-type"],
    });

    const filename = getFilename(response.headers["content-disposition"], defaultFilename);

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", filename);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }

  return response;
};
