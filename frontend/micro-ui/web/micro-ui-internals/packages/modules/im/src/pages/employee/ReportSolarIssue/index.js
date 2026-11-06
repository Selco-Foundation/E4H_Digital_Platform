import React from "react";
import { useTranslation } from "react-i18next";
import { Link, useHistory, useLocation } from "react-router-dom";
import { Card, CardHeader, CardText, SubmitBar, Loader } from "@selco/digit-ui-react-components";
import { Employee } from "../../../constants/Routes";
import useReportingFacilities from "../../../hooks/useReportingFacilities";
import FacilitySelection from "../../../components/ReportSolarIssue/FacilitySelection";
import ReportingSelection from "../../../components/ReportSolarIssue/ReportingSelection";
import ReportingStepper from "../../../components/ReportSolarIssue/ReportingStepper";
import useReportingIssues from "../../../hooks/useReportingIssues";
import { getAvailableReportingIssues, getReportingObservations } from "../../../utilities/reportingIssues";

const ReportSolarIssue = ({ parentUrl }) => {
  const { t } = useTranslation();
  const history = useHistory();
  const location = useLocation();
  const { data, codes, isLoading, isError, refetch } = useReportingFacilities();
  const params = new URLSearchParams(location.search);
  const facilities = (data || []).map((facility) => ({
    ...facility,
    name: t(`Boundary_${facility.code}`, { defaultValue: facility.name || facility.code }),
  })).sort((a, b) => a.name.localeCompare(b.name));
  const selectedCode = codes.length === 1 ? codes[0] : params.get("facility");
  const selectedFacility = facilities.find(({ code }) => code === selectedCode);
  const choosingFacility = codes.length > 1 && (!["issue", "observation"].includes(params.get("step")) || !selectedFacility);
  const issueQuery = useReportingIssues(Boolean(selectedFacility));
  const issues = getAvailableReportingIssues(issueQuery.data, selectedFacility?.status)
    .map((issue) => ({ ...issue, name: t(issue.labelKey, { defaultValue: issue.name }) }));
  const selectedIssue = issues.find(({ key }) => key === params.get("issue"));
  const observations = getReportingObservations(selectedIssue, selectedFacility?.status)
    .map((observation) => ({ ...observation, name: t(observation.labelKey, { defaultValue: observation.name }) }));
  const selectedObservation = observations.find(({ key }) => key === params.get("observation"));
  const choosingObservation = params.get("step") === "observation" && Boolean(selectedIssue);
  const updateSelection = (code, step = "facility", push = false) => {
    const next = new URLSearchParams({ facility: code, step });
    // A different facility starts a fresh issue selection; keep it when navigating back.
    if (code === selectedCode && selectedIssue) {
      next.set("issue", selectedIssue.key);
      if (selectedObservation) next.set("observation", selectedObservation.key);
    }
    history[push ? "push" : "replace"]({ pathname: location.pathname, search: next.toString() });
  };
  const selectIssue = (key) => {
    const next = new URLSearchParams({ facility: selectedFacility.code, step: "issue", issue: key });
    if (key === selectedIssue?.key && selectedObservation) next.set("observation", selectedObservation.key);
    history.replace({ pathname: location.pathname, search: next.toString() });
  };
  const selectObservation = (key) => {
    const next = new URLSearchParams({ facility: selectedFacility.code, step: "observation", issue: selectedIssue.key, observation: key });
    history.replace({ pathname: location.pathname, search: next.toString() });
  };
  const continueToDetails = () => {
    if (!selectedFacility || !selectedIssue || issueQuery.isLoading || issueQuery.isError) return;
    if (!choosingObservation) {
      updateSelection(selectedFacility.code, "observation", true);
      return;
    }
    if (!selectedObservation) return;
    const next = new URLSearchParams({ reportFacility: selectedFacility.code, reportIssue: selectedIssue.key, reportObservation: selectedObservation.key });
    history.push(`${parentUrl}${Employee.CreateComplaint}?${next.toString()}`);
  };

  const renderIssueSelection = () => {
    if (issueQuery.isLoading) return <Loader />;
    if (issueQuery.isError) {
      return <div role="alert">
        <CardText>{t("IM_REPORT_ISSUES_LOAD_ERROR", { defaultValue: "We could not load the components/issues. Please try again." })}</CardText>
        <SubmitBar label={t("IM_REPORT_RETRY", { defaultValue: "Try again" })} onSubmit={() => issueQuery.refetch()} />
      </div>;
    }
    if (!issues.length) {
      return <CardText>{t("IM_REPORT_NO_ISSUES", { defaultValue: "No components/issues are available for this facility. Please contact your administrator." })}</CardText>;
    }
    if (choosingObservation && !observations.length) {
      return <CardText>{t("IM_REPORT_NO_OBSERVATIONS", { defaultValue: "No observations are available for this component/issue. Please go back and choose another option, or contact your administrator." })}</CardText>;
    }
    return <ReportingSelection
      name={choosingObservation ? "reportObservation" : "reportIssue"}
      question={choosingObservation
        ? t("IM_REPORT_OBSERVATION_QUESTION", { defaultValue: "What issue have you noticed?" })
        : t("IM_REPORT_ISSUE_QUESTION", { defaultValue: "Which component/issue do you need support with?" })}
      options={choosingObservation ? observations : issues}
      selectedKey={choosingObservation ? selectedObservation?.key : selectedIssue?.key}
      onSelect={choosingObservation ? selectObservation : selectIssue}
    />;
  };

  return (
    <React.Fragment>
    {facilities.length > 0 && !isLoading && !isError && <ReportingStepper
      currentStep={choosingFacility ? "facility" : choosingObservation ? "observation" : "issue"}
      showFacility={codes.length > 1}
    />}
    <Card style={{ maxWidth: "720px", width: "100%", boxSizing: "border-box", margin: "0 auto" }}>
      <CardHeader>
        <h1 style={{ margin: 0, fontSize: "inherit" }}>
          {t("IM_REPORT_SOLAR_ISSUE_TITLE", { defaultValue: "Report an issue with your solar" })}
        </h1>
      </CardHeader>
      {isLoading && codes.length > 0 ? <Loader /> : isError ? (
        <div role="alert">
          <CardText>{t("IM_REPORT_FACILITY_LOAD_ERROR", { defaultValue: "We could not load your facilities. Please try again." })}</CardText>
          <SubmitBar label={t("IM_REPORT_RETRY", { defaultValue: "Try again" })} onSubmit={() => refetch()} />
        </div>
      ) : !facilities.length ? (
        <CardText>{t("IM_REPORT_NO_FACILITIES", { defaultValue: "No mapped facilities are available. Please contact your administrator." })}</CardText>
      ) : choosingFacility ? (
        <React.Fragment>
          <FacilitySelection facilities={facilities} selectedCode={selectedCode}
            onSelect={updateSelection} showSearch={codes.length > 4} />
          <SubmitBar label={t("CS_COMMON_NEXT", { defaultValue: "Next" })} disabled={!selectedFacility}
            onSubmit={() => selectedFacility && updateSelection(selectedFacility.code, "issue", true)} />
        </React.Fragment>
      ) : <React.Fragment>
        <CardText style={{ fontSize: "20px" }}>{t("IM_REPORT_STEP_FACILITY", { defaultValue: "Facility" })}: {selectedFacility?.name}</CardText>
        {choosingObservation && <CardText>{selectedIssue.name}</CardText>}
        {renderIssueSelection()}
      <div style={{ display: "flex", flexWrap: "wrap", alignItems: "center", gap: "24px" }}>
        <SubmitBar
          label={t("CS_COMMON_NEXT", { defaultValue: "Next" })}
          disabled={!selectedIssue || (choosingObservation && !selectedObservation) || issueQuery.isLoading || issueQuery.isError}
          onSubmit={continueToDetails}
        />
        {(choosingObservation || codes.length > 1) && <SubmitBar label={t("CORE_COMMON_BACK", { defaultValue: "Back" })}
          onSubmit={() => updateSelection(selectedFacility.code, choosingObservation ? "issue" : "facility")} />}
      </div>
      </React.Fragment>}
      <div style={{ marginTop: "24px" }}>
        <Link to={Employee.Home}>{t("CORE_COMMON_GO_TO_HOME", { defaultValue: "Go to home" })}</Link>
      </div>
    </Card>
    </React.Fragment>
  );
};

export default ReportSolarIssue;
