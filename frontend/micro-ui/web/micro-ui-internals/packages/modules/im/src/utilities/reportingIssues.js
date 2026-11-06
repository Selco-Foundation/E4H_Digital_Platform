export const getAvailableReportingIssues = (issues = [], facilityStatus) =>
  facilityStatus === "UNINSTALLED"
    ? issues.filter((issue) => getReportingObservations(issue, facilityStatus).length > 0)
    : issues;

export const getReportingObservations = (issue, facilityStatus) => {
  const observations = issue?.subtypes || [];
  return facilityStatus === "UNINSTALLED"
    ? observations.filter(({ key }) => key === "ADDITIONAL_SUPPORT_REINSTALLATION_REQUIRED")
    : observations;
};

export const isReportingSelectionValid = ({ issue, observation, complaintType, subType, hasError }) => Boolean(
  !hasError && issue && observation && complaintType.key === issue.key && subType.key === observation.key
);

export const getReportingInstallationAction = (observation) => {
  if (observation?.key === "ADDITIONAL_SUPPORT_UNINSTALLATION_REQUIRED") return "UNINSTALL";
  if (observation?.key === "ADDITIONAL_SUPPORT_REINSTALLATION_REQUIRED") return "REINSTALL";
  return null;
};
