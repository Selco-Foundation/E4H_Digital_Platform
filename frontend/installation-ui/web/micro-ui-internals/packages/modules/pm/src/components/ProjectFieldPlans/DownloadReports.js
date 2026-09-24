import React, { useEffect, useRef, useState } from "react";
import { DownloadIcon, Loader, Toast } from "@egovernments/digit-ui-react-components";
import useInstallationReport from "../../hooks/useInstallationReport";
import CommonUtils from "../../utilities/CommonUtils";

const DownloadReports = ({ t, projectId }) => {
  const { fetchZip } = useInstallationReport(projectId);
  const [actionLoading, setActionLoading] = useState(false);
  const [toast, setToast] = useState(null);
  const downloadInProgress = useRef(false);

  useEffect(() => {
    if (!toast) return;
    const timeout = setTimeout(() => setToast(null), 2500);
    return () => clearTimeout(timeout);
  }, [toast]);

  const handleDownload = async () => {
    if (!projectId || downloadInProgress.current) return;
    downloadInProgress.current = true;
    setActionLoading(true);
    try {
      const blob = await fetchZip();
      if (!blob) {
        setToast({ key: "warning", label: t("PM_NO_APPROVED_INSTALLATION_REPORTS", "No approved installation reports are available for this project.") });
        return;
      }
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `Installation_Reports_${projectId}.zip`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      setTimeout(() => URL.revokeObjectURL(url), 60000);
    } catch (error) {
      console.error("Error downloading project installation reports", error);
      const message = CommonUtils.getApiErrorMessage(error);
      setToast({
        key: "error",
        label: message && message !== "COMMON_INSTALLATION_REPORT_DOWNLOAD_FAILED"
          ? message : t("COMMON_INSTALLATION_REPORT_DOWNLOAD_FAILED", "Unable to download installation reports. Please try again."),
      });
    } finally {
      downloadInProgress.current = false;
      setActionLoading(false);
    }
  };

  return (
    <React.Fragment>
      <button
        type="button"
        onClick={handleDownload}
        disabled={actionLoading || !projectId}
        aria-busy={actionLoading}
        style={{
          backgroundColor: "white", border: "1px solid #d35400", color: "#d35400",
          padding: "8px 20px", cursor: actionLoading ? "wait" : "pointer",
          fontWeight: "bold", fontSize: "16px", display: "flex", alignItems: "center",
          justifyContent: "center", gap: "5px", height: "40px", whiteSpace: "nowrap",
          opacity: actionLoading || !projectId ? 0.5 : 1,
        }}
      >
        <span>{t("COMMON_DOWNLOAD_REPORTS", "Download Reports")}</span>
        <div style={{ height: "14px", marginBottom: "auto", transform: "scale(0.7)" }}>
          <DownloadIcon fill="#d35400" />
        </div>
      </button>
      {actionLoading && (
        <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100%", width: "100%", zIndex: 10000000, backgroundColor: "gray", opacity: 0.5, position: "fixed", top: 0, left: 0 }}>
          <Loader />
        </div>
      )}
      {toast && <Toast error={toast.key === "error"} warning={toast.key === "warning"} label={toast.label} onClose={() => setToast(null)} />}
    </React.Fragment>
  );
};

export default DownloadReports;
