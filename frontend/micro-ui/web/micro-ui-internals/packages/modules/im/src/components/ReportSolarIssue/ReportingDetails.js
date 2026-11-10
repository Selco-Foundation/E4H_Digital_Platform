import React from "react";
import { useHistory, useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Card, CardHeader, CardText, SubmitBar } from "@selco/digit-ui-react-components";
import ReportingStepper from "./ReportingStepper";
import SupportDescription from "./SupportDescription";
import ReportingAttachments from "./ReportingAttachments";
import FirUpload from "./FirUpload";
import { isReportingDescriptionValid } from "../../utilities/reportingIssues";

const ReportingDetails = ({ draftId, facility, issue, observation, status, description, onDescriptionChange,
  images, videos, fir, onImagesChange, onVideosChange, onFirChange, onImageUploading, onVideoUploading, onFirUploading,
  uploading, submitting, canSubmit, onSubmit, backUrl, tenantId, showFacility }) => {
  const { t } = useTranslation();
  const history = useHistory();
  const location = useLocation();
  const [uploadReset, setUploadReset] = React.useState(0);
  const theft = issue?.key === "THEFT";
  const requiredDescription = issue?.key === "ADDITIONAL_SUPPORT" && !observation;
  // A description-only support request has already answered its mandatory description before status.
  const steps = [...(theft ? ["fir"] : []), "attachments", ...(!requiredDescription ? ["description"] : []), "review"];
  const params = new URLSearchParams(location.search);
  const storedStep = params.get("reportStep");
  const requestedStep = ["images", "videos"].includes(storedStep) ? "attachments" : storedStep;
  const hasFir = fir.some(([, entry]) => entry?.fileStoreId?.fileStoreId);
  const step = theft && !hasFir ? "fir" : requiredDescription && !isReportingDescriptionValid(description)
    ? "description" : (steps.includes(requestedStep) || (requiredDescription && requestedStep === "description")) ? requestedStep : steps[0];
  React.useEffect(() => {
    // Keep a missing prerequisite on its own question until the user explicitly continues.
    if ((theft && !hasFir) || (requiredDescription && !isReportingDescriptionValid(description))) {
      if (requestedStep !== step) {
        const params = new URLSearchParams(location.search);
        params.set("reportStep", step);
        history.replace({ pathname: location.pathname, search: params.toString() });
      }
    }
  }, [theft, hasFir, requiredDescription, description, requestedStep, step, location.pathname, location.search, history]);
  const index = steps.indexOf(step);
  const busy = uploading || submitting;
  const valid = step === "fir" ? hasFir : step === "description"
    ? description.length <= 500 && (!requiredDescription || isReportingDescriptionValid(description)) : true;
  const navigate = (target) => {
    const params = new URLSearchParams(location.search);
    params.set("reportStep", target);
    history.push({ pathname: location.pathname, search: params.toString() });
  };
  const next = () => { if (!busy && valid) navigate(steps[index + 1] || "attachments"); };
  const skip = () => {
    if (busy) return;
    if (step === "description") onDescriptionChange("");
    if (step === "attachments") {
      onImagesChange([]);
      onVideosChange([]);
      setUploadReset((value) => value + 1);
    }
    navigate(steps[index + 1]);
  };
  const media = step === "attachments";
  const optional = (step === "description" && !requiredDescription) || media;
  return <React.Fragment>
    <ReportingStepper currentStep={step === "fir" ? "fir" : "details"}
      showTheft={theft} showDescription={requiredDescription} showFacility={showFacility} />
    <Card style={{ maxWidth: "720px", width: "100%", boxSizing: "border-box", margin: "0 auto" }}>
      <CardHeader>{t("IM_REPORT_SOLAR_ISSUE_TITLE", { defaultValue: "Report an issue with your solar" })}</CardHeader>
      <CardText style={{ fontSize: "20px" }}>{t("IM_REPORT_STEP_FACILITY", { defaultValue: "Facility" })}: {facility?.name}</CardText>
      {step === "description" && <SupportDescription value={description} onChange={onDescriptionChange} required={requiredDescription} />}
      {step === "fir" && <React.Fragment>
        <h2>{t("IM_REPORT_FIR_QUESTION", { defaultValue: "Please upload the FIR/Complaint letter" })} *</h2>
        <CardText>{t("IM_REPORT_FIR_LIMITS", { defaultValue: "PDF, JPG, JPEG or PNG. Maximum 5 MB per file, up to 5 files." })}</CardText>
        <FirUpload files={fir} onChange={onFirChange} onUploading={onFirUploading} tenantId={tenantId} />
      </React.Fragment>}
      <div hidden={!media}>
      <ReportingAttachments key={uploadReset} queueKey={JSON.stringify([Digit.UserService.getUser().info.uuid, tenantId, draftId, params.get("reportFacility"), params.get("reportIssue")])} images={images} videos={videos}
        onImagesChange={onImagesChange} onVideosChange={onVideosChange}
        onUploading={(value) => { onImageUploading(value); onVideoUploading(value); }} tenantId={tenantId} />
      </div>
      {step === "review" && <React.Fragment>
        <h2>{t("IM_REPORT_REVIEW", { defaultValue: "Review your ticket" })}</h2>
        <style>{`
          .im-report-review {
            display: grid;
            grid-template-columns: minmax(0, 40%) minmax(0, 1fr);
            gap: 12px 16px;
            margin: 16px 0;
          }
          .im-report-review dt { font-weight: 600; overflow-wrap: anywhere; }
          .im-report-review dd { margin: 0; white-space: pre-wrap; overflow-wrap: anywhere; }
        `}</style>
        <dl className="im-report-review">
          <dt>{t("IM_REPORT_STEP_COMPONENT", { defaultValue: "Component/Issue" })}:</dt><dd>{issue?.name}</dd>
          <dt>{t("IM_REPORT_STEP_OBSERVATION", { defaultValue: "Observation" })}:</dt><dd>{observation?.name || t("IM_REPORT_SKIPPED", { defaultValue: "Skipped" })}</dd>
          <dt>{t("IM_REPORT_STEP_STATUS", { defaultValue: "System status" })}:</dt><dd>{status?.name}</dd>
          <dt>{t("IM_REPORT_STEP_DESCRIPTION", { defaultValue: "Description" })}:</dt><dd>{description || t("IM_REPORT_SKIPPED", { defaultValue: "Skipped" })}</dd>
          <dt>{t("INCIDENT_UPLOAD_IMAGE")}:</dt><dd>{images.map(([name]) => name).join(", ") || t("IM_REPORT_SKIPPED", { defaultValue: "Skipped" })}</dd>
          <dt>{t("INCIDENT_UPLOAD_VIDEO")}:</dt><dd>{videos.map(([name]) => name).join(", ") || t("IM_REPORT_SKIPPED", { defaultValue: "Skipped" })}</dd>
          {theft && <React.Fragment><dt>{t("IM_REPORT_STEP_FIR", { defaultValue: "FIR/Complaint letter" })}:</dt><dd>{fir.map(([name]) => name).join(", ")}</dd></React.Fragment>}
        </dl>
      </React.Fragment>}
      <div style={{ display: "flex", flexWrap: "wrap", gap: "24px", alignItems: "center", marginTop: "24px" }}>
        <SubmitBar label={step === "review" ? t("FILE_INCIDENT") : t("CS_COMMON_NEXT", { defaultValue: "Next" })}
          disabled={busy || !valid || (step === "review" && !canSubmit)}
          onSubmit={step === "review" ? () => { if (!busy && canSubmit) onSubmit({}); } : next} />
        {optional && <SubmitBar label={t("CS_COMMON_SKIP", { defaultValue: "Skip" })} disabled={busy} onSubmit={skip} />}
        <SubmitBar label={t("CORE_COMMON_BACK", { defaultValue: "Back" })} disabled={busy}
          onSubmit={() => index > 0 ? navigate(steps[index - 1]) : history.push(backUrl)} />
      </div>
    </Card>
  </React.Fragment>;
};

export default ReportingDetails;
