/**
 * MH2-10 - "must this user change their password before doing anything else?"
 *
 * The ONLY file that knows where the backend's flag lives. egov-user (MH2-01 §4) returns it on the
 * login response when an admin created the account or reset the password. The frontend keeps no
 * role list of its own: the backend decides, the web app obeys, and the Field Assist mobile app
 * simply does not implement the prompt - which is what exempts field users.
 *
 * Same logic as micro-ui core `utilities/passwordChangeRequired.js` - a copy, because the two apps
 * are separate builds. If the flag name or its location changes, change both.
 *
 * Where the flag can be:
 *  1. Inside `UserRequest` -> stored as `user.info` and kept in localStorage ("Employee.user-info"),
 *     so it survives a page refresh on its own.
 *  2. At the top level of the token response -> stored next to `access_token`. On every page load
 *     web/src/index.js rebuilds the session as `{ access_token, info }` only, so a top-level flag is
 *     LOST on refresh. To stop F5 from skipping the screen we remember it in localStorage, tied to
 *     the user it belongs to. Logout and an expired token both clear localStorage, so it never
 *     outlives the session.
 */

export const PASSWORD_CHANGE_REQUIRED_FLAG = "passwordChangeRequired";

// localStorage key, namespaced like the other "Employee.*" keys login.js writes.
const STORAGE_KEY = "Employee.passwordChangeRequired";

/** The forced change screen (route: ChangePassword/index.js -> `${path}/required`). */
export const getForcedPasswordChangePath = () => `/${window?.contextPath}/employee/user/change-password/required`;

const getLoginPath = () => `/${window?.contextPath}/employee/user/login`;

/** true / false when the flag is present on `source`, undefined when it is not there at all. */
const readFlag = (source) => {
  const value = source?.[PASSWORD_CHANGE_REQUIRED_FLAG];
  if (value === true || value === "true") return true;
  if (value === false || value === "false") return false;
  return undefined;
};

/** What the current session object says: inside UserRequest first, then the top level. */
const readSessionFlag = (user) => {
  const fromUserRequest = readFlag(user?.info);
  return fromUserRequest !== undefined ? fromUserRequest : readFlag(user);
};

// localStorage can throw (private mode, storage disabled). A failure here must never break the
// app, so every access is wrapped; worst case the remembered flag is simply not available.
const getRemembered = () => {
  try {
    return JSON.parse(window.localStorage.getItem(STORAGE_KEY));
  } catch (e) {
    return null;
  }
};

const remember = (ownerId) => {
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify({ ownerId }));
  } catch (e) {
    /* see getRemembered */
  }
};

const forget = () => {
  try {
    window.localStorage.removeItem(STORAGE_KEY);
  } catch (e) {
    /* see getRemembered */
  }
};

/**
 * True when the signed-in employee must change their password now.
 * Anything missing (not logged in, backend not yet sending the flag) answers false, so the app
 * behaves exactly as before until MH2-01 is deployed.
 */
export const isPasswordChangeRequired = () => {
  const user = Digit.UserService.getUser();
  if (!user?.access_token || !user?.info) return false;

  // Tie the remembered flag to one account, so it can never leak to a different user who signs
  // in later in the same browser.
  const ownerId = user.info.uuid || user.info.userName;

  const sessionFlag = readSessionFlag(user);
  if (sessionFlag !== undefined) {
    // The session states it explicitly: that is the answer, and we keep localStorage in step.
    if (sessionFlag) remember(ownerId);
    else forget();
    return sessionFlag;
  }

  // The session no longer carries it (a refresh dropped the top-level field): use what we
  // remembered for this same user.
  const remembered = getRemembered();
  return !!remembered && !!ownerId && remembered.ownerId === ownerId;
};

/**
 * The route gate used by pages/employee/index.js.
 * Returns the path to redirect to, or null to let the current route render.
 *
 * Allowed while the flag is set:
 *  - the forced change screen itself;
 *  - the login pages, so signing in again (or as someone else) still works.
 * Everything else - module pages, home, profile, typed URLs, Back - goes to the change screen.
 * That includes the `/no-top-bar/employee/...` routes: they render the same EmployeeApp, and the
 * change screen always lives on the normal `/employee/...` path.
 */
export const getPasswordChangeRedirect = (pathname = "") => {
  if (!isPasswordChangeRequired()) return null;

  const forcedPath = getForcedPasswordChangePath();
  const normalised = (pathname || "").replace(/\/+$/, ""); // ignore a trailing slash
  if (normalised === forcedPath || normalised.startsWith(getLoginPath())) return null;

  return forcedPath;
};
