import React, { useEffect, useMemo, useRef, useState } from "react";
import { FormComposerV2, Loader, Toast } from "@egovernments/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { IngestionService } from "../../services/Injestion";

const DEFAULT_SHEET_NAME = "BoundaryTable Data";

const UploadBoundaryData = () => {
  const { t } = useTranslation();
  const history = useHistory();

  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [toast, setToast] = useState(null);
  const [blockUI, setBlockUI] = useState(false);

  const [file, setFile] = useState(null);
  const [invalidDataError, setInvalidDataError] = useState(null);

  const uploadSeqRef = useRef(0);

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  useEffect(() => {
    if (toast) {
      const tId = setTimeout(() => setToast(null), 2500);
      return () => clearTimeout(tId);
    }
  }, [toast]);

  const handleDownloadTemplate = async () => {
    setBlockUI(true);
    try {
      await IngestionService.downloadBoundaryIngestionTemplate({});
      setToast({ key: "success", label: "FA_TOAST_BOUNDARY_TEMPLATE_DOWNLOAD_SUCCESS" });
    } catch (e) {
      console.error("Error downloading boundary template", e);
      setToast({ key: "error", label: "FA_TOAST_BOUNDARY_TEMPLATE_DOWNLOAD_ERROR" });
    } finally {
      setBlockUI(false);
    }
  };

  const uploadBoundaryNow = async (pickedFile, pickedFileName) => {
    if (!pickedFile) return;

    const seq = ++uploadSeqRef.current;

    setBlockUI(true);
    try {
      const res = await IngestionService.uploadBoundaryDataAndGetDisplayFile({
        boundaryFile: pickedFile,
        boundarySheetName: DEFAULT_SHEET_NAME,
        fallbackFileName: pickedFileName,
      });

      if (seq !== uploadSeqRef.current) return;

      if (res?.errorCode === "INVALID_TEMPLATE") {
        setToast({ key: "error", label: t("FA_TOAST_BOUNDARY_DATA_UPLOAD_TEMPLATE_ERROR") });
        setInvalidDataError(null);

        setFile({ name: pickedFileName, data: pickedFile });
        return;
      }

      if (res?.errorCode === "INVALID_DATA") {
        setInvalidDataError({
          label: `${res?.errorCount || ""} ${t("FA_BOUNDARY_VALIDATION_FAILED")}`,
        });

        setFile({
          name: res?.file?.name || pickedFileName,
          data: res?.file?.data || pickedFile,
          errorCodes: ["INVALID_DATA"],
        });

        setToast({ key: "error", label: t("FA_TOAST_BOUNDARY_DATA_UPLOAD_DATA_ERROR") });
        return;
      }

      setInvalidDataError(null);
      setFile({
        name: res?.file?.name || pickedFileName,
        data: res?.file?.data || pickedFile,
      });

      setToast({ key: "success", label: t("FA_TOAST_BOUNDARY_DATA_UPLOAD_SUCCESS") });
    } catch (e) {
      if (seq !== uploadSeqRef.current) return;
      console.error("Error uploading boundary data", e);
      setToast({ key: "error", label: t("FA_TOAST_BOUNDARY_DATA_UPLOAD_ERROR") });
      setFile({ name: pickedFileName, data: pickedFile });
    } finally {
      if (seq === uploadSeqRef.current) setBlockUI(false);
    }
  };

  const handleBoundaryFileSelect = async (uploaded) => {
    if (!uploaded) return;

    const pickedFileName = uploaded?.name || "boundary.xlsx";

    setFile({ name: pickedFileName, data: uploaded });
    setInvalidDataError(null);

    await uploadBoundaryNow(uploaded, pickedFileName);
  };
  const handleSubmit = () => history.goBack();

  const config = useMemo(
    () => [
      {
        key: "1",
        body: [
          {
            isMandatory: false,
            key: "downloadBoundaryTemplate",
            type: "component",
            component: "PMDownloadTemplate",
            withoutLabelFieldPair: true,
            withoutLabel: true,
            route: "download-boundary-template",
            customProps: {
              t,
              setToast,
              setBlockUI,

              heading: "FA_DOWNLOAD_BOUNDARY_TEMPLATE_PAGE_TITLE",
              description: "FA_DOWNLOAD_BOUNDARY_TEMPLATE_PAGE_DESC",

              handleDownload: handleDownloadTemplate,
              downloadLabel: "PM_DOWNLOAD_TEMPLATE",
            },
            populators: {
              name: "downloadBoundaryTemplate",
            },
          },
        ],
      },

      {
        key: "2",
        body: [
          {
            isMandatory: false,
            key: "uploadBoundaryData",
            type: "component",
            component: "PMUploadFacilityData",
            withoutLabelFieldPair: true,
            withoutLabel: true,
            route: "upload-boundary-data",
            customProps: {
              name: "uploadBoundaryData",
              allowedFileTypes: [".csv", ".xls", ".xlsx"],

              handleFileUpload: handleBoundaryFileSelect,

              invalidDataError,
              errorViewLabel: "CORE_COMMON_VIEW_ERRORS",

              heading: "FA_UPLOAD_BOUNDARY_DATA_PAGE_TITLE",
              description: "FA_UPLOAD_BOUNDARY_DATA_PAGE_DESC",

              t,
              setToast,
              setBlockUI,
              setInvalidDataError,
              file,
              setFile,
            },
            populators: {
              name: "uploadBoundaryData",
              error: t("CORE_COMMON_REQUIRED"),
            },
          },
        ],
      },
    ],
    [t, file, invalidDataError]
  );

  return (
    <div className={"create-project-wrapper"} style={{ padding: mobileView ? "15px" : "0px" }}>
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

      <style>
        {`
      .fa-boundary-upload-cards form > div:not(:last-child) {
        margin-bottom: 20px !important;
      }
    `}
      </style>

      <div className="fa-boundary-upload-cards">
        <FormComposerV2
          config={config}
          onSubmit={handleSubmit}
          label={t("CORE_COMMON_BACK")}
          showSecondaryLabel={false}
          showMultipleCardsWithoutNavs={true}
          noBreakLine={true}
          cardStyle={{ padding: "20px", marginBottom: "20px" }}
          actionClassName={"reverse-actionbar"}
          isDisabled={!!blockUI}
        />
      </div>

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

export default UploadBoundaryData;