// TEMPORARY — mocks `additionalDetails.isRectificationSite` on /activity/v1/activities/_search
// responses until field-planner starts sending it. Delete this file and its call in
// services/Activity.js once the backend is live.
// Identical copies live in pm, qc and fa (utilities/rectificationMock.js) — keep them in sync.

export const MOCK_RECTIFICATION_FLAG = true;

// Stable per activity facility, so the same row is flagged on list and detail screens.
const hashId = (id = "") => id.toString().split("").reduce((hash, char) => (hash * 31 + char.charCodeAt(0)) >>> 0, 7);

const withMockRectificationFlag = (activityFacility) => {
  const additionalDetails = activityFacility?.additionalDetails;

  // Only installation activity facilities carry systemType; leave anything else, and any real value, untouched.
  if (!activityFacility?.id || !additionalDetails?.systemType || "isRectificationSite" in additionalDetails) {
    return activityFacility;
  }

  return {
    ...activityFacility,
    additionalDetails: {
      ...additionalDetails,
      isRectificationSite: hashId(activityFacility.id) % 3 === 0,
    },
  };
};

export const applyRectificationMock = (response) => {
  if (!MOCK_RECTIFICATION_FLAG || !Array.isArray(response?.facility)) {
    return response;
  }

  return {
    ...response,
    facility: response.facility.map((row) => (
      row?.activityFacility ? { ...row, activityFacility: withMockRectificationFlag(row.activityFacility) } : row
    )),
  };
};
