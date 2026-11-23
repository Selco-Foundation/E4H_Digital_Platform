const getCode = (value) => {
  if (!value) return "";
  return typeof value === "string" ? value : value.code || "";
};

export const getFacilityGeography = (facility = {}) => {
  const additionalDetails = facility?.additionalDetails || {};
  const boundary = additionalDetails?.boundary || {};

  return {
    state: getCode(boundary?.state),
    district: getCode(boundary?.district),
    block: getCode(boundary?.block),
  };
};

// Boundaries already picked keep their place and new picks go on the end, so the list records the
// order they were chosen in. The assessment include sheet lists the latest pick's facilities first.
export const inSelectionOrder = (previous = [], next = []) => {
  const nextCodes = new Set(next.map((boundary) => boundary?.code));
  const kept = previous.filter((boundary) => nextCodes.has(boundary?.code));
  const keptCodes = new Set(kept.map((boundary) => boundary?.code));
  return [...kept, ...next.filter((boundary) => !keptCodes.has(boundary?.code))];
};

// The boundaries for these codes in the codes' own order, not the master list's.
export const boundariesInCodeOrder = (boundaries = [], codes = []) =>
  (codes || []).map((code) => boundaries.find((boundary) => boundary.code === code)).filter(Boolean);
