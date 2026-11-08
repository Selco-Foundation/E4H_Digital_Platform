/**
 * The signed-in user's jurisdictions, and what they mean for one module.
 *
 * Identical copies live in core, fa and pm — keep them in sync. (Modules may not import from each
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

/** The level a grant covering everything sits at. No list filter is this coarse. */
const COUNTRY = "Country";

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
 * Whether one grant applies to a given role.
 *
 * A grant with no roles applies to every role the user holds, which is what every grant created
 * before role scoping looks like — so those count for every module, not none.
 */
const appliesToRole = (jurisdiction, roleCode) => {
  const roles = jurisdiction?.roles || [];
  return !roles.length || roles.some((role) => role?.code === roleCode);
};

/**
 * The boundary codes a given role is scoped to, flattened across levels.
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
    if (!appliesToRole(jurisdiction, roleCode)) return;
    if (!codes.includes(jurisdiction.boundary)) codes.push(jurisdiction.boundary);
  });
  return codes.length ? codes : null;
};

/**
 * The same grants, grouped by the level they were granted at:
 * `{ State: [...], District: [...], Block: [...], Facility: [...] }`.
 *
 * This is what to send to an API that filters per level — facility search takes `state`,
 * `district` and `block` — rather than resolving every grant down to its facility leaves. One
 * state code says what thousands of facility codes would, and it leaves the lower-level filters
 * free for the user's own choices instead of spending them on the scope.
 *
 * Null means "do not narrow", as in `boundariesForRole`, and covers two cases: the role has no
 * grants at all, and the role holds a Country-level grant. A country grant covers everything and
 * no list filter is that coarse, so sending only the finer levels would narrow a user who should
 * see all of it.
 *
 * Known gap: a user holding two different levels — a state here, a block there — gets both keys
 * sent, and facility search currently keeps only the lowest one (`BoundaryUtil.detectLowestLevel`)
 * and silently drops the state. Closed when that backend intersects levels instead of picking one.
 */
export const boundariesByLevelForRole = (roleCode) => {
  const jurisdictions = getJurisdictions();
  if (!jurisdictions.length) return null;

  const byLevel = {};
  let coversEverything = false;

  jurisdictions.forEach((jurisdiction) => {
    if (!jurisdiction?.boundary || !jurisdiction?.boundaryType) return;
    if (!appliesToRole(jurisdiction, roleCode)) return;
    if (jurisdiction.boundaryType === COUNTRY) {
      coversEverything = true;
      return;
    }
    const codes = byLevel[jurisdiction.boundaryType] || [];
    if (!codes.includes(jurisdiction.boundary)) {
      byLevel[jurisdiction.boundaryType] = [...codes, jurisdiction.boundary];
    }
  });

  if (coversEverything) return null;
  return Object.keys(byLevel).length ? byLevel : null;
};
