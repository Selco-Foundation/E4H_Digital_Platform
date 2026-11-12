// egov-user issues opaque tokens, so their expiry cannot be read on the client. Before the app renders, ask the
// server whether the stored token is still valid, so an expired session lands on the login page directly.
const AUTH_STORAGE_KEYS = ["token", "Employee.token", "Citizen.token", "user-info", "Employee.user-info", "Citizen.user-info"];

const clearStoredSession = () => {
  AUTH_STORAGE_KEYS.forEach((key) => window.localStorage.removeItem(key));
  window.Digit.SessionStorage.del("User");
};

/** Resolves once the stored session is checked; never rejects. Call after the session is restored from localStorage. */
export const validateStoredSession = async () => {
  const user = window.Digit.SessionStorage.get("User");
  const accessToken = user?.access_token;
  if (!accessToken) return;
  try {
    // The API gateway only accepts the token inside a JSON RequestInfo and rejects /user/_details from the browser,
    // so validate with the same HRMS search the employee login makes (allowed for every employee role).
    const query = new URLSearchParams({ tenantId: "in", ...(user?.info?.userName ? { codes: user.info.userName } : {}) });
    const response = await fetch(`/egov-hrms/employees/_search?${query}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ RequestInfo: { apiId: "Rainmaker", authToken: accessToken, userInfo: user?.info } }),
    });
    if (response.ok) return;
    // Log out only when the gateway says the token itself is invalid (the same signal the Axios interceptor in
    // Request.js uses); any other failure keeps the session.
    const body = await response.text();
    if (body.includes("InvalidAccessTokenException")) clearStoredSession();
  } catch (e) {
    // Network failure: the token cannot be checked, so keep it rather than logging an offline user out.
  }
};
