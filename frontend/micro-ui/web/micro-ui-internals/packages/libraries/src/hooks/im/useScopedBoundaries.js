import useJurisdictions from "./useJurisdictions";
import { IM_SCOPE_ROLES, boundariesByLevelForRoles } from "../../utils/jurisdictionScope";

/**
 * The boundaries to build a picker from, narrowed to the roles that picker is for.
 *
 * `Jurisdiction.Boundaries` and `Jurisdiction.CurrentBoundary` are role-agnostic: they are every
 * grant the user holds, flattened at login under any role, in any product. Feeding them to a
 * boundary picker in IM offers Management Hub areas - and because facility search *is* scoped per
 * role, the two then disagree. The user picks a district from their own dropdown and gets an empty
 * list back, which reads as a broken screen rather than as a boundary they may not act in.
 *
 * `onlyRoles` defaults to every IM role, matching what the facility service declares in
 * `x-acting-role`. Keep them in step - both read IM_SCOPE_ROLES for exactly that reason. Pass a
 * narrower list on a screen gated to one role, as ticket creation does with COMPLAINANT.
 *
 * Falls back to what the caller already had when the grants cannot tell the roles apart - which is
 * what a grant carrying no roles means, and what every grant written before role scoping looks
 * like. Narrowing to nothing there would empty the pickers for everyone who has not been migrated.
 */
const useScopedBoundaries = (fallbackBoundaries, onlyRoles) => {
  const jurisdictions = useJurisdictions();
  return boundariesByLevelForRoles(onlyRoles || IM_SCOPE_ROLES, jurisdictions) || fallbackBoundaries;
};

export default useScopedBoundaries;
