import ReportingActions from "../../../components/ReportSolarIssue/ReportingActions";
import React from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useLocation } from "react-router-dom";
import { Card, CardText, SubmitBar, Loader } from "@selco/digit-ui-react-components";
import { Employee } from "../../../constants/Routes";
import useReportingFacilities from "../../../hooks/useReportingFacilities";
import FacilitySelection from "../../../components/ReportSolarIssue/FacilitySelection";
import ReportingSelection from "../../../components/ReportSolarIssue/ReportingSelection";
import ReportingDuplicateGuard from "../../../components/ReportSolarIssue/ReportingDuplicateGuard";
import ReportingLayout from "../../../components/ReportSolarIssue/ReportingLayout";
import ReportingStepper from "../../../components/ReportSolarIssue/ReportingStepper";
import { ReportingFirDraft } from "../../../services/ReportingFirDraft";
import { ReportingSupportDraft } from "../../../services/ReportingSupportDraft";
import { ReportingDetailsDraft } from "../../../services/ReportingDetailsDraft";
import SupportDescription from "../../../components/ReportSolarIssue/SupportDescription";
import useReportingIssues from "../../../hooks/useReportingIssues";
import { getAvailableReportingIssues, getReportingObservations, isReportingObservationOptional, isReportingSystemStatusValid, isReportingDescriptionValid } from "../../../utilities/reportingIssues";

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
  const [description, setDescription] = React.useState(() => ReportingSupportDraft.read(selectedCode));
  const changeDescription = (value) => {
    setDescription(value);
    ReportingSupportDraft.save(selectedCode, value);
  };
  const selectedFacility = facilities.find(({ code }) => code === selectedCode);
  const choosingFacility = codes.length > 1 && (!["issue", "observation", "description", "status"].includes(params.get("step")) || !selectedFacility);
  const issueQuery = useReportingIssues(Boolean(selectedFacility));
  const issues = getAvailableReportingIssues(issueQuery.data, selectedFacility?.status)
    .map((issue) => ({ ...issue, name: t(issue.labelKey, { defaultValue: issue.name }) }));
  const selectedIssue = issues.find(({ key }) => key === params.get("issue"));
  const observations = getReportingObservations(selectedIssue, selectedFacility?.status)
    .map((observation) => ({ ...observation, name: t(observation.labelKey, { defaultValue: observation.name }) }));
  const isSupport = selectedIssue?.key === "ADDITIONAL_SUPPORT";
  const observationOptional = isReportingObservationOptional(selectedIssue);
  const selectedObservation = observations.find(({ key }) => key === params.get("observation"));
  const selectedStatus = isReportingSystemStatusValid(params.get("status")) ? params.get("status") : null;
  const choosingStatus = params.get("step") === "status" && Boolean(selectedIssue)
    && (Boolean(selectedObservation) || (observationOptional && !params.get("observation") && (!isSupport || isReportingDescriptionValid(description))));
  const choosingDescription = isSupport && (params.get("step") === "description" || (params.get("step") === "status" && !selectedObservation && !params.get("observation") && !choosingStatus));
  const choosingObservation = Boolean(selectedIssue) && (params.get("step") === "observation" || (params.get("step") === "status" && !choosingStatus && !choosingDescription));
  const updateSelection = (code, step = "facility", push = false) => {
    if (code !== selectedCode) { ReportingFirDraft.clear(); ReportingSupportDraft.clear(); ReportingDetailsDraft.clear(); setDescription(""); }
    const next = new URLSearchParams({ facility: code, step });
    // A different facility starts a fresh issue selection; keep it when navigating back.
    if (code === selectedCode && selectedIssue) {
      next.set("issue", selectedIssue.key);
      if (selectedStatus) next.set("status", selectedStatus);
      if (selectedObservation) next.set("observation", selectedObservation.key);
    }
    history[push ? "push" : "replace"]({ pathname: location.pathname, search: next.toString() });
  };
  const selectIssue = (key) => {
    if (key !== selectedIssue?.key) { ReportingFirDraft.clear(); ReportingSupportDraft.clear(); ReportingDetailsDraft.clear(); setDescription(""); }
    const next = new URLSearchParams({ facility: selectedFacility.code, step: "issue", issue: key });
    if (selectedStatus) next.set("status", selectedStatus);
    if (key === selectedIssue?.key && selectedObservation) next.set("observation", selectedObservation.key);
    history.replace({ pathname: location.pathname, search: next.toString() });
  };
  const selectObservation = (key) => {
    if (key !== selectedObservation?.key) ReportingFirDraft.clear();
    const next = new URLSearchParams({ facility: selectedFacility.code, step: "observation", issue: selectedIssue.key, observation: key });
    if (selectedStatus) next.set("status", selectedStatus);
    history.replace({ pathname: location.pathname, search: next.toString() });
  };
  const selectStatus = (status) => {
    const next = new URLSearchParams(params);
    next.set("status", status);
    history.replace({ pathname: location.pathname, search: next.toString() });
  };
  const continueToDetails = (skipObservation = false) => {
    if (!selectedFacility || !selectedIssue || issueQuery.isLoading || issueQuery.isError || (choosingObservation && !issueQuery.observations.isSuccess)) return;
    if (choosingStatus) {
      if (!selectedStatus) return;
      const next = new URLSearchParams({ reportFacility: selectedFacility.code, reportIssue: selectedIssue.key, reportStatus: selectedStatus });
      if (selectedObservation) next.set("reportObservation", selectedObservation.key);
      history.push(`${parentUrl}${Employee.CreateComplaint}?${next.toString()}`);
      return;
    }
    if (choosingDescription) {
      if (isReportingDescriptionValid(description)) updateSelection(selectedFacility.code, "status", true);
      return;
    }
    if (!choosingObservation) {
      updateSelection(selectedFacility.code, "observation", true);
      return;
    }
    if (skipObservation ? !observationOptional : !selectedObservation) return;
    const next = new URLSearchParams({ facility: selectedFacility.code, issue: selectedIssue.key, step: skipObservation && isSupport ? "description" : "status" });
    if (!skipObservation) next.set("observation", selectedObservation.key);
    if (selectedStatus) next.set("status", selectedStatus);
    history.push({ pathname: location.pathname, search: next.toString() });
  };

  const observationBlocked = choosingObservation && !issueQuery.observations.isSuccess;

  const renderIssueSelection = () => {
    if (choosingObservation && issueQuery.observations.isLoading) return <Loader />;
    if (choosingObservation && issueQuery.observations.isError) return <div role="alert">
      <CardText>{t("IM_REPORT_NO_OBSERVATIONS", { defaultValue: "No observations are available for this component/issue. Please go back and choose another option, or contact your administrator." })}</CardText>
      <SubmitBar label={t("IM_REPORT_RETRY", { defaultValue: "Try again" })} onSubmit={() => issueQuery.observations.refetch()} />
    </div>;
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
    if (choosingObservation && !observations.length && !observationOptional) {
      return <CardText>{t("IM_REPORT_NO_OBSERVATIONS", { defaultValue: "No observations are available for this component/issue. Please go back and choose another option, or contact your administrator." })}</CardText>;
    }
    if (choosingDescription) return <SupportDescription value={description} onChange={changeDescription} />;
    if (choosingStatus) return <ReportingSelection
      name="reportStatus"
      question={t("IM_REPORT_SYSTEM_STATUS_QUESTION", { defaultValue: "Is your solar system working right now?" })}
      options={[
        { key: "FUNCTIONAL", name: t("Yes") },
        { key: "NON_FUNCTIONAL", name: t("No") },
      ]}
      selectedKey={selectedStatus}
      onSelect={selectStatus}
    />;
    return <React.Fragment>
    <ReportingSelection
      name={choosingObservation ? "reportObservation" : "reportIssue"}
      required={!choosingObservation || !observationOptional}
      question={choosingObservation
        ? selectedIssue.key === "THEFT"
          ? t("IM_REPORT_STOLEN_COMPONENT_QUESTION", { defaultValue: "Please select the component stolen" })
          : isSupport ? `${t("IM_REPORT_SUPPORT_QUESTION", { defaultValue: "Please choose the issue you need support with" })} (${t("IM_REPORT_OPTIONAL", { defaultValue: "Optional" })})`
          : `${t("IM_REPORT_OBSERVATION_QUESTION", { defaultValue: "What issue are you observing?" })}${observationOptional ? ` (${t("IM_REPORT_OPTIONAL", { defaultValue: "Optional" })})` : ""}`
        : t("IM_REPORT_ISSUE_QUESTION", { defaultValue: "Which component/issue do you need support with?" })}
      options={choosingObservation ? observations : issues}
      selectedKey={choosingObservation ? selectedObservation?.key : selectedIssue?.key}
      onSelect={choosingObservation ? selectObservation : selectIssue}
    />
    {choosingObservation && isSupport && <CardText>{t("IM_REPORT_SUPPORT_HELP", { defaultValue: "Select a support request, or skip to describe the issue in your own words." })}</CardText>}
    {choosingObservation && selectedIssue.key === "THEFT" && <CardText>
      {t("IM_REPORT_THEFT_HELP", { defaultValue: "Please raise another ticket to report any additional component theft." })}
    </CardText>}
    </React.Fragment>;
  };

  return (
    <ReportingLayout facilityName={selectedFacility?.name}>
    {facilities.length > 0 && !isLoading && !isError && <ReportingStepper
      currentStep={choosingFacility ? "facility" : choosingDescription ? "description" : choosingStatus ? "status" : choosingObservation ? "observation" : "issue"}
      showFacility={codes.length > 1}
      showTheft={selectedIssue?.key === "THEFT"}
      showDescription={isSupport && !selectedObservation && (choosingDescription || choosingStatus)}
    />}
    <Card style={{ width: "100%", boxSizing: "border-box", margin: "0 auto" }}>
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
          <ReportingActions next={<SubmitBar label={t("CS_COMMON_NEXT", { defaultValue: "Next" })} disabled={!selectedFacility}
            onSubmit={() => selectedFacility && updateSelection(selectedFacility.code, "issue", true)} />} />
        </React.Fragment>
      ) : <React.Fragment>
        {choosingObservation && <CardText>{selectedIssue.name}</CardText>}
        <ReportingDuplicateGuard facility={selectedFacility?.code} component={selectedIssue?.key}
          enabled={Boolean(selectedIssue && (choosingObservation || choosingDescription || choosingStatus))}
          parentUrl={parentUrl} onBack={() => updateSelection(selectedFacility.code, "issue")}>
        {renderIssueSelection()}
      <ReportingActions
        back={(choosingDescription || choosingStatus || choosingObservation || codes.length > 1) && <SubmitBar className="im-report-back" label={t("CORE_COMMON_BACK", { defaultValue: "Back" })}
          onSubmit={() => updateSelection(selectedFacility.code, choosingDescription ? "observation" : choosingStatus ? (isSupport && !selectedObservation ? "description" : "observation") : choosingObservation ? "issue" : "facility")} />}
        skip={choosingObservation && observationOptional && <SubmitBar className="im-report-skip"
          label={t("CS_COMMON_SKIP", { defaultValue: "Skip" })}
          disabled={issueQuery.isLoading || issueQuery.isError || observationBlocked}
          onSubmit={() => continueToDetails(true)}
        />}
        next={<SubmitBar
          label={t("CS_COMMON_NEXT", { defaultValue: "Next" })}
          disabled={!selectedIssue || (choosingObservation && !selectedObservation) || (choosingStatus && !selectedStatus) || (choosingDescription && !isReportingDescriptionValid(description)) || issueQuery.isLoading || issueQuery.isError || observationBlocked}
          onSubmit={() => continueToDetails()}
        />}
      />
      </ReportingDuplicateGuard>
      </React.Fragment>}
    </Card>
    </ReportingLayout>
  );
};

export default ReportSolarIssue;
