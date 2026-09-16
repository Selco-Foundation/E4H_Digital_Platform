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
