/**
 * The signed-in user's jurisdictions, and what they mean for one module.
 *
 * Identical copies live in core, fa, pm and org — keep them in sync. (Modules may not import from each
 * other; the DIGIT convention here is a copy per module, as with CustomRequest and CommonUtils.)
 *
 * Written at login from HRMS v2 and refreshed on every page load by `useJurisdictions`, so a change
 * an administrator makes takes effect on the user's next reload rather than their next sign-in. The
 * stored shape is the raw grant list — `{ boundary, boundaryType, roles: [{ code }] }` — rather than
 * a precomputed answer, so the interpretation lives here in one place.
 *
 * The readers below are pure: they take the grant list rather than reading storage themselves. That
 * is what lets a refresh re-render the screens that depend on it — a function reading storage on its
 * own cannot tell React that the answer changed. `useJurisdictions` owns the storage and the fetch;
 * storage remains the first paint's source, so nothing flashes while the refresh is in flight.
 *
 * **This narrows what a user is shown, not what they may fetch.** A scoped picker is a convenience
 * and a guard against acting on the wrong area by accident; anyone can still call the API directly
 * with another boundary. Enforcement is the backend's job (phase 5 of the jurisdiction plan).
 */

const SESSION_KEY = "HRMS.Jurisdictions";

/** The level a grant covering everything sits at. No list filter is this coarse. */
const COUNTRY = "Country";

/** Every active grant the user holds, as last stored. Empty when it was never fetched. */
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
 * `jurisdictions` comes from `useJurisdictions`. Returns null, not an empty array, when the user
 * has no grant for this role: the two mean opposite things to a caller. Null is "we do not know,
 * do not narrow"; empty would be "narrow to nothing", which would blank the screen for anyone
 * whose grants have not been read yet.
 */
export const boundariesForRole = (roleCode, jurisdictions) => {
  if (!Array.isArray(jurisdictions) || !jurisdictions.length) return null;

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
 * Mixed levels — a state here, a block there — send both keys, and facility search composes
 * them rather than keeping only the lowest, so neither grant is silently dropped.
 */
export const boundariesByLevelForRole = (roleCode, jurisdictions) => {
  if (!Array.isArray(jurisdictions) || !jurisdictions.length) return null;

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
