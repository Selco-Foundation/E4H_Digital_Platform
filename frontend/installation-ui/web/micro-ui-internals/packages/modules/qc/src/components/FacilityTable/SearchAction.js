import React, { useEffect, useRef, useState } from "react";
import { TextInput, LinkLabel, TickMark, DownloadIcon, SearchIcon, Toast } from "@egovernments/digit-ui-react-components";
import { DoneAll } from "@egovernments/digit-ui-svg-components";
import useInstallationReport from "../../hooks/useInstallationReport";
import CommonUtils from "../../utilities/CommonUtils";
import { ActivityService } from "../../services/Activity";

const saveDownload = (blob, filename) => {
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  setTimeout(() => URL.revokeObjectURL(url), 60000);
};

const SearchActionCentre = ({ t, fieldPlanId, projectQueryFilter, mainCheckBox, selectedFacilities, onSearch, revalidateData, setUpdatingWorkflow }) => {

  const [textToSearch, setTextToSearch] = useState(projectQueryFilter.facilitySearch.name || "");
  const [toast, setToast] = useState(null);

  const [downloading, setDownloading] = useState(false);
  const downloadInProgress = useRef(false);
  const { fetchZip } = useInstallationReport(fieldPlanId);

  const handleDownload = async () => {
    if (!fieldPlanId || downloadInProgress.current) return;
    downloadInProgress.current = true;
    setDownloading(true);
    setUpdatingWorkflow(true);
    try {
      const blob = await fetchZip();
      if (!blob) {
        setToast({ key: "warning", label: t("QC_NO_APPROVED_INSTALLATION_REPORTS", "No approved installation reports are available for this plan.") });
        return;
      }
      saveDownload(blob, `Installation_Reports_${fieldPlanId}.zip`);
    } catch (error) {
      console.error("Error downloading installation reports", error);
      const message = CommonUtils.getApiErrorMessage(error);
      setToast({ key: "error", label: message && message !== "QC_INSTALLATION_REPORT_DOWNLOAD_FAILED"
        ? message : t("QC_INSTALLATION_REPORT_DOWNLOAD_FAILED", "Unable to download installation reports. Please try again.") });
    } finally {
      downloadInProgress.current = false;
      setDownloading(false);
      setUpdatingWorkflow(false);
    }
  };

  const handleSearch = (name) => {
    const facilitySearchQuery = {};
    if (name) {
      facilitySearchQuery.name = name;
    }

    onSearch({
      facilitySearch: {
        name,
      },
      facilitySearchQuery
    })
  }

  useEffect(() => {
    if (!toast) return;
    const timeout = setTimeout(() => setToast(null), 2500);
    return () => clearTimeout(timeout);
  }, [toast]);

  const handleClear = () => {
    setTextToSearch("");
    handleSearch("");
  }

  const ClickableSearchIcon = () => (
    <button
      style={{
        backgroundColor: "white",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <SearchIcon />
    </button>
  )

  const handleBulkApprove = async () => {
    setUpdatingWorkflow(true);

    try {
      const response = await ActivityService.bulkApproveActivityFacilities(projectQueryFilter, mainCheckBox, selectedFacilities);

      if (response) {
        revalidateData();
      }

      switch (response?.status) {
        case 200:
          setUpdatingWorkflow(false);
          setToast({
            key: "success",
            label: t("QC_BULK_APPROVE_SUCCESS"),
          });
          break;
        case 207:
          setUpdatingWorkflow(false);
          setToast({
            key: "warning",
            label: t("QC_BULK_APPROVE_PARTIAL_SUCCESS"),
            failedCount: response?.data?.failedProjectIDs?.length,
          });
          break;
        default:
          setUpdatingWorkflow(false);
          setToast({
            key: "error",
            label: t("QC_BULK_APPROVE_FAILED"),
          })
          break;
      }

    } catch (err) {
      console.error("Error bulk approving", err);
    } finally {
      setUpdatingWorkflow(false);
    }
  }

  return (
    <React.Fragment>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: "10px",
          width: "90%",
          marginLeft: "auto",
          marginRight: "auto",
          height: "20px",
          minWidth: "fit-content",
        }}
      >
        {toast && (
          <Toast
            error={toast.key === "error"}
            warning={toast.key === "warning"}
            label={`${toast.label} ${toast.failedCount ? `(${toast.failedCount} ${t("QC_BULK_APPROVE_FAILED_COUNT")})` : ""}`}
            onClose={() => setToast(null)}
            style={{ maxWidth: "670px" }}
            isDleteBtn={true}
          />
        )}
        {mainCheckBox || selectedFacilities?.length > 0 ? (
          <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
            <DoneAll />
            <div style={{ fontSize: "16px", fontWeight: "bold", color: "#004d66" }}>
              {mainCheckBox ? t("CORE_COMMON_ALL") : selectedFacilities.length} {t("QC_HEALTH_FACILITIES_SELECTED")}
            </div>
          </div>
        ) : (
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleSearch(textToSearch);
            }}
          >
            <div
              style={{
                display: "flex",
                alignItems: "center",
                gap: "10px",
              }}
            >
              <TextInput
                name="serviceRequestId"
                value={textToSearch}
                placeholder={t("QC_SEARCH_HEALTH_FACILITIES")}
                onChange={(e) => {
                  setTextToSearch(e.target.value);
                }}
                className={"search-action"}
                signature={true}
                signatureImg={<ClickableSearchIcon />}
                style={{
                  marginTop: "auto",
                  marginBottom: "auto",
                  border: "none",
                  width: "300px",
                  height: "38px"
                }}
              />
              <LinkLabel
                style={{
                  fontSize: "18px",
                  marginTop: "auto",
                  marginBottom: "auto",
                  minWidth: "fit-content",
                }}
                onClick={handleClear}
              >
                {t("CORE_COMMON_CLEAR")}
              </LinkLabel>
            </div>
          </form>
        )}
        <div
          style={{
            display: "flex",
            gap: "10px",
            alignItems: "center",
          }}
        >
          {(mainCheckBox || selectedFacilities.length > 0) && (
            <button
              style={{
                border: "1px solid #d35400",
                backgroundColor: "#d35400",
                padding: "8px 20px",
                cursor: "pointer",
                color: "white",
                fontWeight: "bold",
                fontSize: "16px",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                gap: "10px",
                height: "40px"
              }}
              onClick={handleBulkApprove}
            >
              <span>{t("CORE_COMMON_APPROVE")}</span>
              <div style={{ transform: "scale(1.4)" }}>
                <TickMark />
              </div>
            </button>
          )}
          <button
            type="button"
            onClick={handleDownload}
            disabled={downloading || !fieldPlanId}
            aria-busy={downloading}
            style={{
              opacity: downloading || !fieldPlanId ? 0.5 : 1,
              backgroundColor: "white",
              border: "1px solid #d35400",
              color: "#d35400",
              padding: "8px 20px",
              cursor: "pointer",
              fontWeight: "bold",
              fontSize: "16px",
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              gap: "5px",
              height: "40px"
            }}
          >
            <span>{t("QC_DOWNLOAD_REPORTS")}</span>
            <div style={{ height: "14px", marginBottom: "auto", transform: "scale(0.7)" }}>
              <DownloadIcon fill={"#d35400"} />
            </div>
          </button>
        </div>
      </div>
    </React.Fragment>
  );
};

export default SearchActionCentre;
