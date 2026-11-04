
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