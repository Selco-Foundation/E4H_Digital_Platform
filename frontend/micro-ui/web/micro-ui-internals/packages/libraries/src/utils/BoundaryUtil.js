
export const BoundaryUtil = {

  aggregateBoundaryCodes: (boundaries) => {
    const boundaryCodes = [];
    if (boundaries) {
      Object.values(boundaries).forEach((value) => {
        boundaryCodes.push(...(value || []));
      })
    }

    return [...new Set(boundaryCodes)];
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
   *
   * Nothing to narrow by means **no filter**, not a filter matching nothing. This used to return
   * `{ boundaryCodes: ["-"] }` so that a session without jurisdiction data showed an empty list
   * rather than the whole tenant - the client was the only thing standing between the user and
   * every facility in the country. It is not any more: facility search derives the ceiling from
   * HRMS and refuses a caller it cannot resolve. Keeping the placeholder now only hides facilities
   * the server would have allowed.
   */
  facilitySearchFilters: (boundaries) => {
    // "-" is a placeholder that matches nothing, not a boundary, and some callers still default to
    // { country: ["-"] }. It must not be read as a real country grant, which means the opposite.
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

    return filters;
  },

  /**
   * Whether two comma-joined boundary-code strings name the same set of boundaries.
   *
   * The two sides come from different places and are ordered differently: one is the user's
   * jurisdiction as HRMS returns it, the other the same boundaries as they appear in the
   * boundary tree. Comparing the joined strings makes "Karnataka,Meghalaya" look different from
   * "Meghalaya,Karnataka", so a user holding more than one state reads as having narrowed their
   * selection when they have not - which silently disables the location pickers that depend on
   * this test. Order is not information here; the set is.
   */
  sameBoundarySelection: (a, b) => {
    const normalize = (value) =>
      (value || "")
        .split(",")
        .map((code) => code.trim())
        .filter(Boolean)
        .sort()
        .join(",");
    return normalize(a) === normalize(b);
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