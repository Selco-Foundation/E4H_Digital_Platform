import React, { useCallback, useEffect, useMemo, useState } from "react";
import useProject from "../../hooks/useProject";
import useFieldPlan from "../../hooks/useFieldPlan";
import { useTranslation } from "react-i18next";
import { FormComposerV2, Loader, Toast } from "@egovernments/digit-ui-react-components";
import { useDispatch } from "react-redux";
import { populateResponsePage, populateWorkingFieldPlan, populateWorkingProject } from "../../redux/actions";
import { useHistory } from "react-router-dom";
import { PMService } from "../../services/PMService";
import UnsavedDataAlert from "../../components/UnsavedDataAlert";

const PopulateBOM = () => {

  const { t } = useTranslation();
  const [createdProject, setCreatedProject] = useState(null);
  const [createdFieldPlan, setCreatedFieldPlan] = useState(null);
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [toast, setToast] = useState(null);
  const [blockUI, setBlockUI] = useState(null);
  const [file, setFile] = useState(null);
  const [uploadedValidFile, setUploadedValidFile] = useState(false);
  const [invalidDataError, setInvalidDataError] = useState(null);
  const [backAlert, setBackAlert] = useState(null);
  const history = useHistory();
  const url = window.location.href;
  const projectId = url.split("project/")[1].split("/")[0];
  const fieldPlanId = url.split("field-plan/")[1].split("/")[0];
  const dispatch = useDispatch();

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const { isLoading: projectDataLoading, data: projectData } = useProject({
    id: [projectId],
  });

  const { isLoading: fieldPlanDataLoading, data: fieldPlanData } = useFieldPlan({
    ids: [fieldPlanId],
  });

  useEffect(() => {
    const project = projectData?.projects?.[0];
    if (project) {
      dispatch(populateWorkingProject(project));
      setCreatedProject(project);
    }
  }, [projectData, dispatch]);

  useEffect(() => {
    const fieldPlan = fieldPlanData?.fieldPlans?.[0];
    if (fieldPlan) {
      dispatch(populateWorkingFieldPlan(fieldPlan));
      setCreatedFieldPlan(fieldPlan);
    }
  }, [fieldPlanData, dispatch]);

  useEffect(() => {
    if (toast) {
      const timeout = setTimeout(() => setToast(null), 2500);
      return () => clearTimeout(timeout);
    }
  }, [toast]);

  const handleBOMDataDownload = useCallback(async () => {
    setBlockUI(true);
    try {
      await PMService.downloadFieldPlanBOMDataTemplate(createdProject.id, createdFieldPlan.id);
      setToast({
        label: t("PM_TOAST_BOM_TEMPLATE_DOWNLOAD_SUCCESS"),
        key: "success",
      });
    } catch (error) {
      console.error("Error downloading BOM template", error);
      setToast({
        label: t("PM_TOAST_BOM_TEMPLATE_DOWNLOAD_ERROR"),
        key: "error",
      });
    } finally {
      setBlockUI(false);
    }
  }, [createdProject, createdFieldPlan, t]);

  const handleBOMDataUpload = useCallback(async (chosenFile) => {
    setBlockUI(true);
    let uploadedFile;
    try {
      const response = await PMService.uploadFieldPlanBOMDataTemplate(chosenFile, createdFieldPlan.id);

      if (response.errorCode === "INVALID_TEMPLATE") {
        setToast({
          key: "error",
          label: t("PM_TOAST_BOM_DATA_UPLOAD_TEMPLATE_ERROR"),
        });
        setUploadedValidFile(false);
        setInvalidDataError(null);
      } else if (response.errorCode === "INVALID_DATA") {
        setInvalidDataError({
          label: `${response.errorCount} ${t("PM_BOM_VALIDATION_FAILED")}`,
        });
        setUploadedValidFile(false);
        uploadedFile = {
          name: response.file.name || chosenFile.name,
          data: response.file.data,
          errorCodes: ["INVALID_DATA"],
        };
      } else {
        setToast({
          key: "success",
          label: t("PM_TOAST_BOM_DATA_UPLOAD_SUCCESS"),
        });
        setInvalidDataError(null);
        setUploadedValidFile(true);
        uploadedFile = {
          name: response.file.name || chosenFile.name,
          data: response.file.data,
        };
      }
    } catch (e) {
      console.error("Error uploading BOM template", e);
      setUploadedValidFile(false);
      setToast({
        key: "error",
        label: t("PM_TOAST_BOM_DATA_UPLOAD_ERROR"),
      });
    } finally {
      setBlockUI(false);
    }

    setFile(uploadedFile);
  }, [createdFieldPlan, t]);

  const config = useMemo(
    () => [
      {
        key: "1",
        body: [
          {
            key: "downloadTemplate",
            type: "component",
            component: "PMDownloadTemplate",
            withoutLabelFieldPair: true,
            withoutLabel: true,
            disable: false,
            customProps: {
              name: "downloadTemplate",
              heading: "PM_POPULATE_BOM_HEAD_DOWNLOAD_TEMPLATE",
              description: "PM_POPULATE_BOM_HEAD_DOWNLOAD_TEMPLATE_DESC",
              handleDownload: handleBOMDataDownload,
              t,
            },
            route: "",
            nextRoute: "",
            populators: {
              name: "downloadTemplate",
              error: "Required",
            },
          },
        ],
      },
      {
        key: "2",
        body: [
          {
            isMandatory: false,
            key: "uploadBOMData",
            type: "component",
            component: "PMUploadFacilityData",
            withoutLabelFieldPair: true,
            withoutLabel: true,
            route: "upload-bom-data",
            customProps: {
              name: "uploadBOMData",
              allowedFileTypes: [".xlsx"],
              handleFileUpload: handleBOMDataUpload,
              invalidDataError: invalidDataError,
              errorViewLabel: "CORE_COMMON_VIEW_ERRORS",
              heading: "PM_POPULATE_BOM_HEAD_UPLOAD_DATA",
              description: "PM_POPULATE_BOM_HEAD_UPLOAD_DATA_DESC",
              t,
              setToast,
              setBlockUI,
              setInvalidDataError,
              file,
              setFile,
            },
            nextRoute: "",
            populators: {
              name: "uploadBOMData",
              error: "Required",
            },
          },
        ],
      },
    ],
    [t, handleBOMDataDownload, handleBOMDataUpload, file, invalidDataError]
  );

  if (projectDataLoading || fieldPlanDataLoading) {
    return <Loader />;
  }

  if (!fieldPlanId || !createdFieldPlan) {
    return (
      <div style={{ padding: mobileView ? "15px" : "0px" }}>
        <span>{t("PM_POPULATE_BOM_FIELD_PLAN_NOT_FOUND")}</span>
      </div>
    );
  }

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
      {createdProject?.name && (
        <div style={{ fontSize: "40px", fontWeight: "700", fontFamily: "Roboto Condensed", marginBottom: "10px", color: "#0B0C0C" }}>
          {createdProject?.name}
        </div>
      )}
      {createdFieldPlan?.name && (
        <div style={{ fontSize: "24px", fontWeight: "500", fontFamily: "Roboto", marginBottom: "20px", color: "#0B0C0C" }}>
          {createdFieldPlan?.name}
        </div>
      )}
      <FormComposerV2
        config={config}
        onSubmit={null}
        label={""}
        heading={""}
        headingStyle={{
          fontSize: "32px",
          marginBottom: "20px",
        }}
        showMultipleCardsWithoutNavs={true}
        noBreakLine={true}
        cardStyle={{ padding: "20px" }}
        actionClassName={"reverse-actionbar"}
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
      {backAlert && <UnsavedDataAlert t={t} alert={backAlert} setAlert={setBackAlert} />}
    </div>
  );
};

export default PopulateBOM;
