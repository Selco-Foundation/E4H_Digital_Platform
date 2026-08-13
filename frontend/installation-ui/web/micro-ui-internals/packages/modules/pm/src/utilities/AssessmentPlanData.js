export const REMOTE_ASSESSMENT_STATUS_CODES = ["NOT_INITIATED", "QUALIFIED", "NOT_QUALIFIED", "PENDING", "PENDING_WRONG_NUMBER", "PENDING_NO_ANSWER"];

export const ONSITE_ASSESSMENT_STATUS_CODES = ["NOT_INITIATED", "PENDING", "QUALIFIED", "NOT_QUALIFIED"];

export const ASSESSMENT_RESULT_CODES = ["ELIGIBLE", "NOT_ELIGIBLE", "PENDING"];

export const REMOTE_PENDING_STATUS_CODES = ["PENDING", "PENDING_WRONG_NUMBER", "PENDING_NO_ANSWER", "NOT_INITIATED"];

const isFinalRemoteStatus = (status) => status === "QUALIFIED" || status === "NOT_QUALIFIED";
const isFinalResult = (result) => result === "ELIGIBLE" || result === "NOT_ELIGIBLE";

// The "Assign for On-site Assessment" action is only valid once the remote assessment has
// reached a final state (Qualified or Not Qualified), the on-site assessment hasn't been
// started yet, and the facility hasn't already been marked Eligible/Not Eligible.
export const canAssignForOnSiteAssessment = (facility) => (
  isFinalRemoteStatus(facility?.remoteStatus) &&
  facility?.onSiteStatus === "NOT_INITIATED" &&
  !isFinalResult(facility?.result)
);

// A reason is only required when the selected result overrides a unanimous opposite agreement
// between the two assessments: marking Not Eligible while both assessments qualified the
// facility, or marking Eligible while both assessments found it not qualified.
export const isUnanimousOverride = (facility, targetResult) => {
  if (targetResult === "NOT_ELIGIBLE") {
    return facility?.remoteStatus === "QUALIFIED" && facility?.onSiteStatus === "QUALIFIED";
  }
  if (targetResult === "ELIGIBLE") {
    return facility?.remoteStatus === "NOT_QUALIFIED" && facility?.onSiteStatus === "NOT_QUALIFIED";
  }
  return false;
};

// Evaluates the Mark Eligible/Mark Not Eligible validation scenarios, in strict priority order,
// for the given selection of facilities and the result the user is trying to apply.
// Returns one of: "BLOCK_REMOTE_PENDING", "WARN_ONSITE_PENDING", "WARN_ONSITE_NOT_INITIATED",
// "REASON_REQUIRED", "BULK_NOT_SUPPORTED", or "PROCEED".
export const evaluateMarkResultScenario = (facilities, targetResult) => {
  if (facilities.some((facility) => REMOTE_PENDING_STATUS_CODES.includes(facility?.remoteStatus))) {
    return "BLOCK_REMOTE_PENDING";
  }

  if (facilities.some((facility) => isFinalRemoteStatus(facility?.remoteStatus) && facility?.onSiteStatus === "PENDING")) {
    return "WARN_ONSITE_PENDING";
  }

  if (facilities.some((facility) => isFinalRemoteStatus(facility?.remoteStatus) && facility?.onSiteStatus === "NOT_INITIATED")) {
    return "WARN_ONSITE_NOT_INITIATED";
  }

  // Every facility now has a finalised remote and on-site outcome.
  const overrideFacilities = facilities.filter((facility) => isUnanimousOverride(facility, targetResult));

  if (overrideFacilities.length === 0) {
    return "PROCEED";
  }

  if (overrideFacilities.length === facilities.length) {
    return "REASON_REQUIRED";
  }

  return "BULK_NOT_SUPPORTED";
};

const RESPONSE_QUESTIONS = [
  { key: "renovationPlanned", questionKey: "PM_ASSESSMENT_RESPONSE_QUESTION_RENOVATION_PLANNED" },
  { key: "existingSolar", questionKey: "PM_ASSESSMENT_RESPONSE_QUESTION_EXISTING_SOLAR" },
  { key: "govtOwned", questionKey: "PM_ASSESSMENT_RESPONSE_QUESTION_GOVT_OWNED" },
];

const getLatestSubmission = (submissions, phase) => (
  (submissions || [])
    .filter((submission) => submission?.assessmentPhase === phase)
    .reduce((latest, submission) => (
      !latest || (submission?.serverReceivedTime || 0) > (latest?.serverReceivedTime || 0) ? submission : latest
    ), null)
);

// answerCode is "YES"/"NO" (translate via TL_COMMON_YES/TL_COMMON_NO) or null when the
// submission didn't capture that field.
const getResponsesForSubmission = (submission) => (
  submission
    ? RESPONSE_QUESTIONS.map(({ key, questionKey }) => {
      const value = submission?.submissionData?.[key];
      return { questionKey, answerCode: value === "YES" || value === "NO" ? value : null };
    })
    : []
);

// Remote (PHONE) and on-site (FIELD) responses, read from the facility's actual submissions
// via the plan/facility/_detail API.
export const getAssessmentResponses = (facilityDetail) => ({
  remoteResponses: getResponsesForSubmission(getLatestSubmission(facilityDetail?.submissions, "PHONE")),
  siteResponses: getResponsesForSubmission(getLatestSubmission(facilityDetail?.submissions, "FIELD")),
});

const outcomeFromStatus = (status) => {
  if (status === "QUALIFIED") return "ELIGIBLE";
  if (status === "NOT_QUALIFIED") return "NOT_ELIGIBLE";
  return "AWAITING_RESULT";
};

export const getPhoneOutcome = (facility) => outcomeFromStatus(facility?.remoteStatus);

export const getFieldOutcome = (facility) => outcomeFromStatus(facility?.onSiteStatus);
