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
// Returns one of: "BLOCK_REMOTE_PENDING", "REASON_REQUIRED", "BULK_NOT_SUPPORTED",
// "WARN_ONSITE_PENDING", "WARN_ONSITE_NOT_INITIATED", or "PROCEED".
export const evaluateMarkResultScenario = (facilities, targetResult) => {
  if (facilities.some((facility) => REMOTE_PENDING_STATUS_CODES.includes(facility?.remoteStatus))) {
    return "BLOCK_REMOTE_PENDING";
  }

  // isUnanimousOverride only matches facilities that have already finalised BOTH assessments,
  // so this must be checked before the on-site pending/not-initiated warnings below — otherwise
  // a mixed selection (some facilities still mid-assessment, one already needing a reason) gets
  // misclassified as a plain on-site warning and the reason requirement is silently skipped.
  const overrideFacilities = facilities.filter((facility) => isUnanimousOverride(facility, targetResult));

  if (overrideFacilities.length > 0) {
    return overrideFacilities.length === facilities.length ? "REASON_REQUIRED" : "BULK_NOT_SUPPORTED";
  }

  if (facilities.some((facility) => isFinalRemoteStatus(facility?.remoteStatus) && facility?.onSiteStatus === "PENDING")) {
    return "WARN_ONSITE_PENDING";
  }

  if (facilities.some((facility) => isFinalRemoteStatus(facility?.remoteStatus) && facility?.onSiteStatus === "NOT_INITIATED")) {
    return "WARN_ONSITE_NOT_INITIATED";
  }

  return "PROCEED";
};

const getLatestSubmission = (submissions, phase) => (
  (submissions || [])
    .filter((submission) => submission?.assessmentPhase === phase)
    .reduce((latest, submission) => (
      !latest || (submission?.serverReceivedTime || 0) > (latest?.serverReceivedTime || 0) ? submission : latest
    ), null)
);

const sortByOrder = (items) => [...(items || [])].sort((a, b) => (a?.order || 0) - (b?.order || 0));

// Enum-backed fields (dropdown/select/radio) store the option's code in submissionData; this
// resolves it to the option's display name so callers only ever deal with display text, which
// they should still route through t() in case it becomes a real translation key later.
const resolveFieldValue = (property, rawValue) => {
  const resolveEnumName = (code) => {
    const match = property?.enums?.find((option) => option?.code === code)?.name;
    return match === null || match === undefined ? code : match;
  };

  if (Array.isArray(rawValue)) {
    return rawValue.length ? rawValue.map(resolveEnumName) : null;
  }

  if (rawValue === null || rawValue === undefined || rawValue === "") {
    return null;
  }

  return property?.enums?.length ? resolveEnumName(rawValue) : String(rawValue);
};

// A page only shows up if at least one of its properties was actually answered in the
// submission — this naturally matches the mobile form's own visibilityCondition-driven
// skipping, without needing to evaluate those expressions here.
const buildResponseSections = (submission, formSchemas) => {
  if (!submission) return [];

  const schema = (formSchemas || []).find((formSchema) => formSchema?.formType === submission.formType);
  if (!schema) return [];

  return sortByOrder(schema.pages)
    .map((page) => ({
      key: page.page,
      label: page.label,
      fields: sortByOrder(page.properties)
        .map((property) => ({
          label: property.label,
          value: resolveFieldValue(property, submission?.submissionData?.[property.fieldName]),
        }))
        .filter((field) => field.value !== null),
    }))
    .filter((section) => section.fields.length > 0);
};

// Builds the per-page response sections for the facility details page, driven by the
// assessment.AssessmentMobileFormSchema MDMS master (matched to each submission by formType)
// instead of a fixed set of questions.
export const getAssessmentResponseSections = (facilityDetail, formSchemas) => ({
  remoteSections: buildResponseSections(getLatestSubmission(facilityDetail?.submissions, "PHONE"), formSchemas),
  siteSections: buildResponseSections(getLatestSubmission(facilityDetail?.submissions, "FIELD"), formSchemas),
});

const outcomeFromStatus = (status) => {
  if (status === "QUALIFIED") return "ELIGIBLE";
  if (status === "NOT_QUALIFIED") return "NOT_ELIGIBLE";
  return "AWAITING_RESULT";
};

export const getPhoneOutcome = (facility) => outcomeFromStatus(facility?.remoteStatus);

export const getFieldOutcome = (facility) => outcomeFromStatus(facility?.onSiteStatus);
