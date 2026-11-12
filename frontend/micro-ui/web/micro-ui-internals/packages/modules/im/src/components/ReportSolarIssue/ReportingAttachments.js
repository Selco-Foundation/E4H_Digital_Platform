import React from "react";
import { useTranslation } from "react-i18next";
import { CardText, MultiUploadWrapper } from "@selco/digit-ui-react-components";

const ReportingAttachments = ({ images, videos, onImagesChange, onVideosChange, onUploading, tenantId, queueKey }) => {
  const { t } = useTranslation();
  const [uploadState, setUploadState] = React.useState({ entries: [], controls: {} });
  const { entries: fileStatuses, controls } = uploadState;
  const onFileStatusChange = React.useCallback((entries, controls) => {
    setUploadState({ entries, controls });
  }, []);
  const statusLabels = {
    queued: t("IM_REPORT_UPLOAD_QUEUED", { defaultValue: "Queued" }),
    uploading: t("IM_REPORT_UPLOAD_UPLOADING", { defaultValue: "Uploading" }),
    processing: t("IM_REPORT_UPLOAD_PROCESSING", { defaultValue: "Processing" }),
    failed: t("IM_REPORT_UPLOAD_FAILED", { defaultValue: "Upload failed" }),
  };
  const onChange = (files) => {
    onImagesChange(files.filter(([, entry]) => !entry.file.type.startsWith("video/")));
    onVideosChange(files.filter(([, entry]) => entry.file.type.startsWith("video/")));
  };
  return <div className="im-report-upload">
    <h2 className="im-report-question">{t("IM_REPORT_ATTACHMENTS_QUESTION", { defaultValue: "Please attach a photo or video of the issue, if available." })}</h2>
    <CardText>{t("IM_REPORT_OPTIONAL", { defaultValue: "Optional" })}</CardText>
    <CardText>{t("IM_REPORT_ATTACHMENTS_HELP", { defaultValue: "This will help our CRM support team and technicians understand the problem better and troubleshoot it faster." })}</CardText>
    {controls.restoring && <CardText>{t("IM_REPORT_RESTORING_UPLOADS", { defaultValue: "Restoring your selected files…" })}</CardText>}
    {controls.recoveryUnavailable && <div role="status">{t("IM_REPORT_UPLOAD_RECOVERY_UNAVAILABLE", { defaultValue: "Your browser could not save pending files for recovery. Keep this page open until uploads finish; after refreshing, you may need to select those files again." })}</div>}
    <MultiUploadWrapper t={t} module="Incident" tenantId={tenantId}
      setuploadedstate={[...images, ...videos]} getFormState={onChange} onUploadStatusChange={onUploading}
      allowedFileTypesRegex={/(jpg|jpeg|png|image|mp4|mov|avi|wmv|video)$/i}
      allowedMaxSizeInMB={10} maxFilesAllowed={5}
      specificFileConstraint={{ type: "video", maxSize: 50, maxFiles: 2 }}
      ulb={Digit.SessionStorage.get("Employee.tenantId")}
      acceptFiles=".png, .jpg, .jpeg, .mp4, .avi, .mov, .wmv, image/*, video/*"
      individualUploadProgress={true} onFileStatusChange={onFileStatusChange} uploadQueueKey={queueKey}
      multiple={true} analyticsPage="new_ticket_page" mediaIntent="attachment" />
    <ul style={{ listStyle: "none", padding: 0 }}>
      {fileStatuses.filter((entry) => entry.status !== "uploaded").map((entry) => (
        <li key={entry.id} style={{ margin: "12px 0", padding: "12px", border: "1px solid #d6d6d6", borderRadius: "4px", overflowWrap: "anywhere" }}>
          <div>{entry.name}</div>
          <div role="status">{statusLabels[entry.status]}{entry.progress !== null && entry.status === "uploading" ? ` (${entry.progress}%)` : ""}</div>
          {entry.status === "failed" && <div style={{ display: "flex", gap: "16px", margin: "8px 0" }}>
            <button type="button" onClick={() => controls.retry(entry.id)}>{t("IM_REPORT_RETRY", { defaultValue: "Try again" })}</button>
            <button type="button" onClick={() => controls.remove(entry.id)}>{t("IM_REPORT_REMOVE_FILE", { defaultValue: "Remove" })}</button>
          </div>}
          {entry.status !== "failed" && <progress aria-label={entry.name} max={100}
            value={entry.progress === null ? undefined : entry.progress} style={{ width: "100%" }} />}
        </li>
      ))}
    </ul>
    <CardText>{t("IM_REPORT_ATTACHMENTS_LIMITS", { defaultValue: "Up to 5 photos (10 MB each) and 2 videos (50 MB each)." })}</CardText>
  </div>;
};

export default ReportingAttachments;
