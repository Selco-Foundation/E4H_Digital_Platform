
export const BoundaryUtil = {

  aggregateBoundaryCodes: (boundaries) => {
    const boundaryCodes = [];
    if (boundaries) {
      Object.values(boundaries).forEach((value) => {
        boundaryCodes.push(...(value || []));
      })
    }

    return boundaryCodes;
  },

  /**
   * The boundary filter to send, or undefined when the user has not narrowed anything.
   *
   * `Jurisdiction.CurrentBoundary` starts out as a copy of `Jurisdiction.Boundaries` - every
   * boundary the user holds, written at login - and ChangeCity replaces it with one level when
   * they pick a city, or puts the full set back for "All".
   *
   * Sending the full set used to be harmless, because the server took it as the whole truth.
   * It no longer is: the inbox now derives scope from HRMS and treats this as a *narrowing*
   * filter, so a session that predates a new grant would quietly hide the tickets it covers
   * until the user logged out and back in. Send it only when it says something the server does
   * not already know.
   */
  explicitNarrowing: (current, full) => {
    const normalize = (boundaries) => {
      if (!boundaries) return "";
      return Object.keys(boundaries)
        .sort()
        .map((key) => `${key}:${[...(boundaries[key] || [])].sort().join(",")}`)
        .join("|");
    };
    if (!current) return undefined;
    return normalize(current) === normalize(full) ? undefined : current;
  },

  /**
   * The facility-search filter for a set of jurisdictions, keyed by the level each was granted at.
   *
   * Facility search resolves state, district and block through the boundary hierarchy itself, so
   * one state code does the work of every facility code beneath it. Sending the expanded leaves
   * instead means a request body of thousands of codes, and duplicates work the server does
   * anyway. Facility-level grants are already leaves and go to `boundaryCodes`, which is matched
   * against `facility.boundary_code` directly.
   *
   * A country-level grant yields no filter at all: there is no country filter to send, and
   * sending only the finer levels would narrow a user who is meant to see everything.
   */
  facilitySearchFilters: (boundaries) => {
    // Callers fall back to { country: ["-"] } when the session has no jurisdiction. The "-" is a
    // placeholder that matches nothing, not a boundary - so it must not be mistaken for a real
    // country grant, which means the opposite.
    const real = (codes) => (codes || []).filter((code) => code && code !== "-");

    const fieldByLevel = {
      state: "state",
      district: "district",
      block: "block",
      facility: "boundaryCodes",
    };

    const filters = {};
    if (boundaries) {
      if (real(boundaries.country).length) {
        // Covers everything, and there is no country filter to send.
        return filters;
      }
      Object.keys(fieldByLevel).forEach((level) => {
        const codes = real(boundaries[level]);
        if (codes.length) {
          filters[fieldByLevel[level]] = codes;
        }
      });
    }

    // No jurisdiction at all. Match nothing rather than everything: this is the state a session
    // is in before login data arrives, and the caller would otherwise be shown the whole tenant.
    return Object.keys(filters).length ? filters : { boundaryCodes: ["-"] };
  },

  aggregateBoundaryTypes: (boundaries) => {
    const boundaryTypes = [];
    if (boundaries) {
      Object.keys(boundaries).forEach((key) => {
        boundaryTypes.push(key);
      })
    }

    return boundaryTypes;
  }

}