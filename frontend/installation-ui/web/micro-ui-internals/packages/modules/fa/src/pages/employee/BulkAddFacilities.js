import React, { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { FormComposerV2, Loader, Toast } from "@egovernments/digit-ui-react-components";

const BulkAddFacilities = () => {

  const { t } = useTranslation();
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [toast, setToast] = useState(null);
  const [blockUI, setBlockUI] = useState(null);
  const [file, setFile] = useState(null);
  const [invalidDataError, setInvalidDataError] = useState(null);

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const handleFacilityTemplateDownload = async () => {

  };

  const handleFacilityDataUpload = async () => {

  }

  const config = useMemo(
    () => [
      {
        key: "3",
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
              handleFileUpload: handleFacilityDataUpload,
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
    [t]
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