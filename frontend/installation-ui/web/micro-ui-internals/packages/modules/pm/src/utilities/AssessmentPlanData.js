// Dummy data source for the Assessment Plan feature until the backend entity exists.

const DEFAULT_SUMMARY = {
  totalFacilities: 10,
  remoteAssessmentsCompleted: 7,
  onSiteAssessmentsCompleted: 0,
  eligibleCount: 2,
  notEligibleCount: 2,
};

export const DUMMY_ASSESSMENT_PLANS = [
  { id: "1", name: "GU-INS-2026-1", startDate: "2026-06-18", endDate: "2026-07-01", numberOfFacilities: 57, status: "DRAFT", summary: DEFAULT_SUMMARY },
  { id: "2", name: "GU-INS-2026-2", startDate: "2026-07-05", endDate: "2026-07-20", numberOfFacilities: 45, status: "SCHEDULED", summary: DEFAULT_SUMMARY },
  { id: "3", name: "GU-INS-2026-3", startDate: "2026-07-25", endDate: "2026-08-10", numberOfFacilities: 30, status: "COMPLETED", summary: DEFAULT_SUMMARY },
  { id: "4", name: "GU-INS-2026-4", startDate: "2026-08-15", endDate: "2026-08-30", numberOfFacilities: 60, status: "DRAFT", summary: DEFAULT_SUMMARY },
];

export const DUMMY_ASSESSMENT_FACILITIES = [
  { id: "1", name: "PHC Bundu", facilityType: "Primary Health Center", category: "GOVERNMENT", district: "Ranchi", block: "Bundu", remoteStatus: "QUALIFIED", onSiteStatus: "NOT_INITIATED", result: "PENDING", resultSource: null, notEligibleReason: null, notEligibleRemarks: null },
  { id: "2", name: "SC Tamar", facilityType: "Sub Center", category: "GOVERNMENT", district: "Ranchi", block: "Tamar", remoteStatus: "QUALIFIED", onSiteStatus: "PENDING", result: "PENDING", resultSource: null, notEligibleReason: null, notEligibleRemarks: null },
  { id: "3", name: "PHC Khunti", facilityType: "Block Primary Health Center", category: "GOVERNMENT", district: "Khunti", block: "Khunti", remoteStatus: "QUALIFIED", onSiteStatus: "QUALIFIED", result: "ELIGIBLE", resultSource: "AUTO", notEligibleReason: null, notEligibleRemarks: null },
  { id: "4", name: "Anganwadi-12", facilityType: "Health and Wellness Center", category: "PRIVATE", district: "Ranchi", block: "Bundu", remoteStatus: "PENDING", onSiteStatus: "NOT_INITIATED", result: "PENDING", resultSource: null, notEligibleReason: null, notEligibleRemarks: null },
  { id: "5", name: "SC Murhu", facilityType: "Community Health Center", category: "GOVERNMENT", district: "Khunti", block: "Murhu", remoteStatus: "QUALIFIED", onSiteStatus: "QUALIFIED", result: "NOT_ELIGIBLE", resultSource: "AUTO", notEligibleReason: null, notEligibleRemarks: null },
  { id: "6", name: "SC Silli", facilityType: "Dispensary", category: "PRIVATE", district: "Ranchi", block: "Silli", remoteStatus: "PENDING", onSiteStatus: "NOT_INITIATED", result: "PENDING", resultSource: null, notEligibleReason: null, notEligibleRemarks: null },
  { id: "7", name: "District Hospital Dhanbad", facilityType: "District Hospital", category: "GOVERNMENT", district: "Dhanbad", block: "Dhanbad", remoteStatus: "QUALIFIED", onSiteStatus: "QUALIFIED", result: "ELIGIBLE", resultSource: "AUTO", notEligibleReason: null, notEligibleRemarks: null },
  { id: "8", name: "Anganwadi-Tathibandh", facilityType: "Health and Wellness Center", category: "PRIVATE", district: "Dhanbad", block: "Tathibandh", remoteStatus: "NOT_QUALIFIED", onSiteStatus: "NOT_INITIATED", result: "PENDING", resultSource: null, notEligibleReason: null, notEligibleRemarks: null },
  { id: "9", name: "PHC Ormanjhi", facilityType: "Primary Health Center", category: "GOVERNMENT", district: "Ranchi", block: "Ormanjhi", remoteStatus: "QUALIFIED", onSiteStatus: "PENDING", result: "PENDING", resultSource: null, notEligibleReason: null, notEligibleRemarks: null },
  { id: "10", name: "SC Jharia", facilityType: "Sub Center", category: "GOVERNMENT", district: "Dhanbad", block: "Jharia", remoteStatus: "QUALIFIED", onSiteStatus: "QUALIFIED", result: "NOT_ELIGIBLE", resultSource: "AUTO", notEligibleReason: null, notEligibleRemarks: null },
];

export const REMOTE_ASSESSMENT_STATUS_CODES = ["QUALIFIED", "NOT_QUALIFIED", "PENDING", "PENDING_WRONG_NUMBER", "PENDING_NO_ANSWER"];

export const ONSITE_ASSESSMENT_STATUS_CODES = ["NOT_INITIATED", "PENDING", "QUALIFIED", "NOT_QUALIFIED"];

export const ASSESSMENT_RESULT_CODES = ["ELIGIBLE", "NOT_ELIGIBLE", "PENDING"];

export const ASSESSMENT_FACILITY_CATEGORY_CODES = ["GOVERNMENT", "PRIVATE"];

export const ASSESSMENT_NOT_ELIGIBLE_REASON_CODES = ["FACILITY_NOT_OPERATIONAL", "DOES_NOT_MEET_CRITERIA", "INSUFFICIENT_INFRASTRUCTURE", "DUPLICATE_ENTRY", "OTHER"];

const isFinalRemoteStatus = (status) => status === "QUALIFIED" || status === "NOT_QUALIFIED";
const isFinalOnSiteStatus = (status) => status === "QUALIFIED" || status === "NOT_QUALIFIED";

// The "Assign for On-site Assessment" action is only valid once the remote assessment has
// reached a final state, on-site assessment hasn't been started yet, and no manual/auto
// assessment result has already been recorded for the facility.
export const canAssignForOnSiteAssessment = (facility) => (
  isFinalRemoteStatus(facility?.remoteStatus) &&
  facility?.onSiteStatus === "NOT_INITIATED" &&
  facility?.result === "PENDING"
);

// Marking a facility Not Eligible always requires a reason. Marking a facility Eligible only
// requires a reason when it overrides a unanimous "not qualified" agreement between the two
// assessments; likewise marking Not Eligible while both assessments unanimously agree
// "qualified" is also an override, even though that case already always shows the reason modal.
export const isUnanimousOverride = (facility, targetResult) => {
  if (targetResult === "NOT_ELIGIBLE") {
    return facility?.remoteStatus === "QUALIFIED" && facility?.onSiteStatus === "QUALIFIED";
  }
  if (targetResult === "ELIGIBLE") {
    return facility?.remoteStatus === "NOT_QUALIFIED" && facility?.onSiteStatus === "NOT_QUALIFIED";
  }
  return false;
};

export const computeAssessmentSummary = (facilities = []) => ({
  totalFacilities: facilities.length,
  remoteAssessmentsCompleted: facilities.filter((facility) => isFinalRemoteStatus(facility?.remoteStatus)).length,
  onSiteAssessmentsCompleted: facilities.filter((facility) => isFinalOnSiteStatus(facility?.onSiteStatus)).length,
  eligibleCount: facilities.filter((facility) => facility?.result === "ELIGIBLE").length,
  notEligibleCount: facilities.filter((facility) => facility?.result === "NOT_ELIGIBLE").length,
});

const REMOTE_ASSESSMENT_QUESTIONS = [
  "Is the facility functional?",
  "Does it have adequate supply?",
  "Are staff present?",
];

const SITE_ASSESSMENT_QUESTIONS = [
  "Is the facility physically accessible?",
  "Is the required equipment available on-site?",
  "Does the facility meet infrastructure standards?",
];

// Dummy Remote Assessor / Field POC responses, derived from the facility's current status
// until the backend stores actual submitted responses.
export const getAssessmentResponses = (facility) => {
  const remoteAnswer = facility?.remoteStatus === "NOT_QUALIFIED" ? "No" : facility?.remoteStatus === "QUALIFIED" ? "Yes" : null;
  const remoteResponses = remoteAnswer
    ? REMOTE_ASSESSMENT_QUESTIONS.map((question) => ({ question, answer: remoteAnswer }))
    : [];

  const siteAnswer = facility?.onSiteStatus === "NOT_QUALIFIED" ? "No" : facility?.onSiteStatus === "QUALIFIED" ? "Yes" : null;
  const siteResponses = siteAnswer
    ? SITE_ASSESSMENT_QUESTIONS.map((question) => ({ question, answer: siteAnswer }))
    : [];

  return { remoteResponses, siteResponses };
};

const outcomeFromStatus = (status) => {
  if (status === "QUALIFIED") return "ELIGIBLE";
  if (status === "NOT_QUALIFIED") return "NOT_ELIGIBLE";
  return "AWAITING_RESULT";
};

export const getPhoneOutcome = (facility) => outcomeFromStatus(facility?.remoteStatus);

export const getFieldOutcome = (facility) => outcomeFromStatus(facility?.onSiteStatus);
