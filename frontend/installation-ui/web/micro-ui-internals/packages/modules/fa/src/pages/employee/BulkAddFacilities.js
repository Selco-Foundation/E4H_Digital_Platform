import React, { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { FormComposerV2, Loader, Toast } from "@egovernments/digit-ui-react-components";
import { IngestionService } from "../../services/Ingestion";
import { FAService } from "../../services/FA";

const BulkAddFacilities = () => {

  const { t } = useTranslation();
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [toast, setToast] = useState(null);
  const [blockUI, setBlockUI] = useState(null);
  const [file, setFile] = useState(null);
  const [invalidDataError, setInvalidDataError] = useState(null);
  const [isOnmReady, setIsOnmReady] = useState(false);

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  useEffect(() => {
    if (toast) {
      setTimeout(() => {
        setToast(null);
      }, 2500);
    }
  }, [toast]);

  const handleFacilityTemplateDownload = async () => {
    try {
      setBlockUI(true);
      await IngestionService.downloadFacilityDataTemplate();
      setBlockUI(false);
      setToast({ key: "success", label: "FACILITY_TEMPLATE_DOWNLOAD_SUCCESS" });

    } catch (e) {
      console.log("Failed to download facility template", e);
      setBlockUI(false);
      setToast({ key: "error", label: "FACILITY_TEMPLATE_DOWNLOAD_ERROR" });
    }
  };

  const handleFacilityDataUpload = async (chosenFile, onmReadyStatus) => {
    let uploadedFile;
    try {
      setBlockUI(true);
      const response = await FAService.uploadFacilityDataTemplate(chosenFile, onmReadyStatus);
      setBlockUI(false);

      if (response.errorCode === "INVALID_TEMPLATE") {
        setToast({
          key: "error",
          label: t("FACILITY_DATA_UPLOAD_TEMPLATE_ERROR"),
        });
        setInvalidDataError(null);

      } else if (response.errorCode === "INVALID_DATA") {
        setInvalidDataError({
          label: `${response.errorCount} ${t("HEALTH_FACILITIES_VALIDATION_FAILED")}`,
        });
        uploadedFile = {
          name: response.file.name || file.name,
          data: response.file.data,
          errorCodes: ["INVALID_DATA"],
        };

      } else {
        setToast({
          key: "success",
          label: t("FACILITY_DATA_UPLOAD_SUCCESS"),
        });
        setInvalidDataError(null);
        uploadedFile = {
          name: response.file.name || file.name,
          data: response.file.data,
        };
      }

    } catch (e) {
      console.error("Error uploading template", e);
      setBlockUI(false);
      setToast({
        key: "error",
        label: t("FACILITY_DATA_UPLOAD_ERROR"),
      });
    }

    setFile(uploadedFile);
  }

  const config = useMemo(
    () => [
      {
        key: "1",
        body: [
          {
            key: "downloadTemplate",
            type: "component",
            component: "FADownloadTemplate",
            withoutLabelFieldPair: true,
            withoutLabel: true,
            disable: false,
            customProps: {
              name: "downloadTemplate",
              heading: "PM_CREATE_PROJECT_HEAD_DOWNLOAD_FACILITY_TEMPLATE",
              description: "PM_CREATE_PROJECT_HEAD_DOWNLOAD_FACILITY_TEMPLATE_DESC",
              handleDownload: handleFacilityTemplateDownload,
              t,
            },
            route: "project-duration-2",
            nextRoute: "",
            populators: {
              name: "downloadTemplate",
              error: t("CORE_COMMON_REQUIRED"),
            },
          },
        ],
      },
      {
        key: "2",
        body: [
          {
            key: "onmReadyToggler",
            type: "component",
            component: "FAOnmReadyToggler",
            withoutLabelFieldPair: true,
            withoutLabel: true,
            disable: false,
            customProps: {
              name: "onmReadyToggler",
              isOnmReady,
              setIsOnmReady,
              t,
            },
            route: "facilities-is-onm-ready",
            nextRoute: "",
            populators: {
              name: "onmReadyToggler",
            },
          },
        ],
      },
      {
        key: "3",
        body: [
          {
            isMandatory: false,
            key: "uploadFacilityData",
            type: "component",
            component: "FAUploadFacilityData",
            withoutLabelFieldPair: true,
            withoutLabel: true,
            route: "upload-facility-data",
            customProps: {
              name: "uploadFacilityData",
              allowedFileTypes: [".xls", ".xlsx"],
              handleFileUpload: (file) => handleFacilityDataUpload(file, isOnmReady),
              invalidDataError: invalidDataError,
              errorViewLabel: "CORE_COMMON_VIEW_ERRORS",
              heading: "PM_CREATE_PROJECT_HEAD_UPLOAD_FACILITY_DATA",
              description: "PM_CREATE_PROJECT_HEAD_UPLOAD_FACILITY_DATA_DESC",
              t,
              setToast,
              setBlockUI,
              setInvalidDataError,
              file,
              setFile,
            },
            nextRoute: "",
            populators: {
              name: "uploadFacilityData",
              error: t("PM_PROJECT_ERROR_FACILITY_DATA_REQUIRED"),
            },
          },
        ],
      },
    ],
    [t, isOnmReady, file, invalidDataError]
  );

  return (
    <div style={{ padding: mobileView ? "15px" : "0px" }}>
      {blockUI && (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            height: "100%",
            width: "100%",
            zIndex: 10000005,
            backgroundColor: "gray",
            opacity: 0.5,
            position: "fixed",
            top: 0,
            left: 0,
          }}
        >
          <Loader />
        </div>
      )}
      <FormComposerV2
        config={config}
        showMultipleCardsWithoutNavs={true}
        noBreakLine={true}
        submitInForm={false}
        cardStyle={{ padding: "20px" }}
      />
      {toast && (
        <Toast
          error={toast.key === "error"}
          warning={toast.key === "warning"}
          style={{
            ...(toast.key === "error" ? { backgroundColor: "#B91900" } : {}),
            ...(mobileView ? { bottom: "120px" } : {}),
          }}
          label={t(toast.label)}
          isDleteBtn={true}
          onClose={() => setToast(null)}
        />
      )}
    </div>
  );
};

export default BulkAddFacilities;