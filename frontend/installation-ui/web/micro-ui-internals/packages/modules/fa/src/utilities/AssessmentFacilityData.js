const getLatestSubmission = (submissions, phase) => (
  (submissions || [])
    .filter((submission) => submission?.assessmentPhase === phase)
    .reduce((latest, submission) => (
      !latest || (submission?.serverReceivedTime || 0) > (latest?.serverReceivedTime || 0) ? submission : latest
    ), null)
);

const sortByOrder = (items) => [...(items || [])].sort((a, b) => (a?.order || 0) - (b?.order || 0));

const formatDateValue = (rawValue) => {
  const date = new Date(rawValue);
  if (Number.isNaN(date.getTime())) return rawValue;

  const month = date.toLocaleString("en-US", { month: "long" });
  const day = String(date.getDate()).padStart(2, "0");
  const year = date.getFullYear();
  return `${day} ${month} ${year}`;
};

// Enum-backed fields (dropdown/select/radio) store the option's code in submissionData; this
// resolves it to the option's display name so callers only ever deal with display text, which
// they should still route through t() in case it becomes a real translation key later. Plain
// text/numeric/date/time values are never translation keys and must never be passed through
// t() — i18next's default nsSeparator treats ":" as a namespace separator, so a raw value like
// "19:02" or an ISO timestamp gets silently mangled (e.g. t("19:02") returns just "02").
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

  if (property?.enums?.length) {
    return resolveEnumName(rawValue);
  }

  if (property?.format === "date") {
    return formatDateValue(rawValue);
  }

  return String(rawValue);
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
          translateValue: !!property?.enums?.length,
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
