/**
 * The signed-in user's jurisdictions, and what they mean for one module.
 *
 * Identical copies live in core and fa — keep them in sync. (Modules may not import from each
 * other; the DIGIT convention here is a copy per module, as with CustomRequest and CommonUtils.)
 *
 * Written once at login from HRMS v2 and read from session storage thereafter. The stored shape is
 * the raw grant list — `{ boundary, boundaryType, roles: [{ code }] }` — rather than a precomputed
 * answer, so the interpretation lives here in one place and can change without a re-login.
 *
 * **This narrows what a user is shown, not what they may fetch.** A scoped picker is a convenience
 * and a guard against acting on the wrong area by accident; anyone can still call the API directly
 * with another boundary. Enforcement is the backend's job (phase 5 of the jurisdiction plan).
 */

const SESSION_KEY = "HRMS.Jurisdictions";

/** Every active grant the user holds, as stored at login. Empty when it was never fetched. */
export const getJurisdictions = () => {
  const stored = Digit.SessionStorage.get(SESSION_KEY);
  if (Array.isArray(stored)) return stored;
  try {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch (error) {
    console.error("Could not read stored jurisdictions", error);
    return [];
  }
};

export const setJurisdictions = (jurisdictions) => {
  const value = Array.isArray(jurisdictions) ? jurisdictions : [];
  Digit.SessionStorage.set(SESSION_KEY, value);
  try {
    localStorage.setItem(SESSION_KEY, JSON.stringify(value));
  } catch (error) {
    console.error("Could not persist jurisdictions", error);
  }
};

/**
 * The boundary codes a given role is scoped to.
 *
 * A grant with no roles applies to every role the user holds, which is what every grant created
 * before role scoping looks like — so those count for every module, not none.
 *
 * Returns null, not an empty array, when the user has no grant for this role: the two mean
 * opposite things to a caller. Null is "we do not know, do not narrow"; empty would be "narrow to
 * nothing", which would blank the screen for anyone whose login predates the jurisdiction fetch.
 */
export const boundariesForRole = (roleCode) => {
  const jurisdictions = getJurisdictions();
  if (!jurisdictions.length) return null;

  const codes = [];
  jurisdictions.forEach((jurisdiction) => {
    if (!jurisdiction?.boundary) return;
    const roles = jurisdiction.roles || [];
    const appliesToEveryRole = !roles.length;
    if (appliesToEveryRole || roles.some((role) => role?.code === roleCode)) {
      if (!codes.includes(jurisdiction.boundary)) codes.push(jurisdiction.boundary);
    }
  });
  return codes.length ? codes : null;
};
