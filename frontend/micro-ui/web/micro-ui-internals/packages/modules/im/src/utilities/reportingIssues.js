export const getAvailableReportingIssues = (issues = [], facilityStatus) =>
  facilityStatus === "UNINSTALLED" ? issues.filter(({ key }) => key === "ADDITIONAL_SUPPORT") : issues;

export const getReportingObservations = (issue) => issue?.subtypes || [];

export const isReportingObservationOptional = (issue) => Boolean(issue && issue.key !== "THEFT");

export const isReportingDescriptionValid = (description = "") => description.trim().length > 0 && description.length <= 500;

export const isReportingSelectionValid = ({ issue, observation, complaintType, subType, hasError, description }) => Boolean(
  !hasError && issue && complaintType.key === issue.key &&
  (observation ? subType.key === observation.key : isReportingObservationOptional(issue) && !subType.key &&
    (issue.key !== "ADDITIONAL_SUPPORT" || isReportingDescriptionValid(description)))
);

export const getReportingInstallationAction = (observation) => {
  if (observation?.key === "ADDITIONAL_SUPPORT_UNINSTALLATION_REQUIRED") return "UNINSTALL";
  if (observation?.key === "ADDITIONAL_SUPPORT_REINSTALLATION_REQUIRED") return "REINSTALL";
  return null;
};

export const isReportingSystemStatusValid = (status) => ["FUNCTIONAL", "NON_FUNCTIONAL"].includes(status);
