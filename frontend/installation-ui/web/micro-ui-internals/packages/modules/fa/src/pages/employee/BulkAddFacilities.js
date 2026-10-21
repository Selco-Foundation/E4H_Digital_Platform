import React, { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { FormComposerV2, Loader, Toast } from "@egovernments/digit-ui-react-components";
import { IngestionService } from "../../services/Ingestion";
import { FAService } from "../../services/FA";
import useBoundary from "../../hooks/useBoundary";
import { buildBoundaryDataTree, EMPTY_GEOGRAPHY } from "../../utilities/GeographySelection";

const BulkAddFacilities = () => {

  const { t } = useTranslation();
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [toast, setToast] = useState(null);
  const [blockUI, setBlockUI] = useState(null);
  const [file, setFile] = useState(null);
  const [invalidDataError, setInvalidDataError] = useState(null);
  const [isOnmReady, setIsOnmReady] = useState(false);
  // The template is generated for this geography only: its boundary sheet and its
  // State/District/Block dropdowns hold just these blocks, never the whole country.
  const [geography, setGeography] = useState(EMPTY_GEOGRAPHY);
  const { data: boundary, isLoading: isBoundaryLoading } = useBoundary();

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
      // Only the state is required: an empty district or block level means "all of it"
      // (`buildBoundaryDataTree`), so the template covers the whole state.
      if (!geography.states.length) {
        setToast({ key: "error", label: "FA_BULK_ADD_SELECT_GEOGRAPHY" });
        return;
      }
      setBlockUI(true);
      await IngestionService.downloadFacilityDataTemplate(buildBoundaryDataTree(geography, boundary));
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
        key: "0",
        // Laid out like the pm Geography step: heading, then inline State / Districts / Blocks
        // rows whose labels and required marks FormComposer draws.
        body: [
          {
            key: "geographyHeader",
            type: "component",
            component: "FAGeographyHeader",
            withoutLabelFieldPair: true,
            withoutLabel: true,
            customProps: { name: "geographyHeader", t },
            populators: { name: "geographyHeader" },
          },
          // Only the state is mandatory: an empty district or block level stands for all of
          // them (`buildBoundaryDataTree`), so those two narrow the template rather than
          // complete it.
          ...[
            ["states", "PM_CREATE_PROJECT_LABEL_STATE", true],
            ["districts", "PM_CREATE_PROJECT_LABEL_DISTRICTS", false],
            ["blocks", "PM_CREATE_PROJECT_LABEL_BLOCKS", false],
          ].map(([level, label, isMandatory]) => ({
            inline: true,
            label,
            isMandatory,
            key: `geography_${level}`,
            type: "component",
            component: "FAGeographyLevelSelector",
            customProps: {
              name: `geography_${level}`,
              level,
              geography,
              setGeography,
              boundary,
              isLoading: isBoundaryLoading,
              t,
            },
            populators: { name: `geography_${level}` },
          })),
        ],
      },
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
              disabled: geography.states.length === 0,
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
    [t, isOnmReady, file, invalidDataError, geography, boundary, isBoundaryLoading]
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