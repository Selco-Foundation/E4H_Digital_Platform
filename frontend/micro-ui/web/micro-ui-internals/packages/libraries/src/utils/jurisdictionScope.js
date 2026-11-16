/**
 * The signed-in user's jurisdictions, and what they mean for a set of roles.
 *
 * Written at login from HRMS **v2** and refreshed on every page load by `useJurisdictions`, so a
 * change an administrator makes takes effect on the user's next reload rather than their next
 * sign-in. The stored shape is the raw grant list - `{ boundary, boundaryType, roles: [{ code }] }`
 * - rather than a precomputed answer, so the interpretation lives here in one place.
 *
 * <h3>Why this takes a list of roles, unlike the Management Hub's copy</h3>
 * There, one module means one role, so its `boundariesForRole` is singular. An IM user holds
 * several IM roles at once and a facility must be visible to them under any of those hats, so
 * these take a list and union the grants - the same rule the server applies to `x-acting-role`.
 * Keep the two in step: if this unions and the header does not, a picker will offer a boundary the
 * search then refuses.
 *
 * The readers below are pure: they take the grant list rather than reading storage themselves.
 * That is what lets a refresh re-render the screens that depend on it - a function reading storage
 * on its own cannot tell React that the answer changed. `useJurisdictions` owns the storage and
 * the fetch; storage remains the first paint's source, so nothing flashes while a refresh is in
 * flight.
 *
 * **This narrows what a user is shown, not what they may fetch.** Enforcement is the server's job:
 * facility search resolves its own ceiling from HRMS and refuses anything outside it.
 */

const SESSION_KEY = "HRMS.Jurisdictions";

/**
 * The IM roles that can define a scope, and the single source for that list.
 *
 * Read by the facility service to fill `x-acting-role` and by the boundary pickers to decide what
 * to offer. Those two must agree: if the picker's list is wider than the header's, it offers a
 * boundary the search then refuses, and the user lands on an empty screen having chosen from their
 * own dropdown. That is why this is not duplicated per caller.
 *
 * Roles outside it are left out on purpose. The same person may be a PROJECT_MANAGER or
 * FACILITY_ADMIN in the Management Hub, and those grants must not widen anything in IM.
 */
export const IM_SCOPE_ROLES = [
  "COMPLAINT_FACILITATOR_1", // State SPOC
  "COMPLAINT_FACILITATOR_2", // Tech POC
  "COMPLAINT_ASSESSOR", // CRM
  "COMPLAINT_RESOLVER", // vendor
  "COMPLAINANT", // facility POC
];

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
 * Whether one grant applies to any of the given roles.
 *
 * A grant with no roles applies to every role the user holds, which is what every grant created
 * before role scoping looks like - so those count for every screen, not none. The server's
 * `appliesTo` reads them the same way; both have to, or the picker and the search disagree.
 *
 * An empty or missing role list means "no role filter", so every grant applies.
 */
const appliesToAnyRole = (jurisdiction, roleCodes) => {
  if (!roleCodes?.length) return true;
  const roles = jurisdiction?.roles || [];
  return !roles.length || roles.some((role) => roleCodes.includes(role?.code));
};

/**
 * The boundary codes the given roles are scoped to, flattened across levels.
 *
 * Returns null, not an empty array, when the user has no grant for these roles: the two mean
 * opposite things to a caller. Null is "we do not know, do not narrow"; empty would be "narrow to
 * nothing", which would blank the screen for anyone whose grants have not been read yet.
 */
export const boundariesForRoles = (roleCodes, jurisdictions) => {
  if (!Array.isArray(jurisdictions) || !jurisdictions.length) return null;

  const codes = [];
  jurisdictions.forEach((jurisdiction) => {
    if (!jurisdiction?.boundary) return;
    if (!appliesToAnyRole(jurisdiction, roleCodes)) return;
    if (!codes.includes(jurisdiction.boundary)) codes.push(jurisdiction.boundary);
  });
  return codes.length ? codes : null;
};

/**
 * The same grants, grouped by the level they were granted at:
 * `{ state: [...], district: [...], block: [...], facility: [...] }`.
 *
 * Lower-cased deliberately: that is the shape `Jurisdiction.Boundaries` has had since before role
 * scoping, and what `BoundaryUtil.facilitySearchFilters` and `aggregateBoundaryCodes` already
 * read. Returning `boundaryType` verbatim would be a second, incompatible convention for the same
 * idea. (The Management Hub's copy does return it verbatim, because nothing there consumes the
 * older shape.)
 *
 * Null means "do not narrow", as in `boundariesForRoles`, and covers two cases: no grants at all,
 * and a Country-level grant. A country grant covers everything and no list filter is that coarse,
 * so sending only the finer levels would narrow a user who should see all of it.
 */
export const boundariesByLevelForRoles = (roleCodes, jurisdictions) => {
  if (!Array.isArray(jurisdictions) || !jurisdictions.length) return null;

  const byLevel = {};
  let coversEverything = false;

  jurisdictions.forEach((jurisdiction) => {
    if (!jurisdiction?.boundary || !jurisdiction?.boundaryType) return;
    if (!appliesToAnyRole(jurisdiction, roleCodes)) return;
    if (jurisdiction.boundaryType === COUNTRY) {
      coversEverything = true;
      return;
    }
    const level = jurisdiction.boundaryType.toLowerCase();
    const codes = byLevel[level] || [];
    if (!codes.includes(jurisdiction.boundary)) {
      byLevel[level] = [...codes, jurisdiction.boundary];
    }
  });

  if (coversEverything) return null;
  return Object.keys(byLevel).length ? byLevel : null;
};

export const JurisdictionScope = {
  IM_SCOPE_ROLES,
  getJurisdictions,
  setJurisdictions,
  boundariesForRoles,
  boundariesByLevelForRoles,
};
