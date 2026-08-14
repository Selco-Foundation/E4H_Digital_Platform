const getCode = (value) => {
  if (!value) return "";
  return typeof value === "string" ? value : value.code || "";
};

const getFirstCode = (values) => {
  for (const value of values) {
    const code = getCode(value);
    if (code) return code;
  }
  return "";
};

export const getFacilityGeography = (facility = {}) => {
  const additionalDetails = facility?.additionalDetails || {};
  const facilityDetails = facility?.facilityDetails || facility?.facility_details || {};
  const boundary = additionalDetails?.boundary || facilityDetails?.boundary || {};

  // Facility geography can come in different frontend response shapes.
  return {
    state: getFirstCode([
      boundary?.state,
      additionalDetails?.state,
      additionalDetails?.stateCode,
      facilityDetails?.state,
      facilityDetails?.stateCode,
    ]),
    district: getFirstCode([
      boundary?.district,
      additionalDetails?.district,
      additionalDetails?.districtCode,
      facilityDetails?.district,
      facilityDetails?.districtCode,
    ]),
    block: getFirstCode([
      boundary?.block,
      additionalDetails?.block,
      additionalDetails?.blockCode,
      facilityDetails?.block,
      facilityDetails?.blockCode,
      facility?.boundaryCode,
    ]),
  };
};
