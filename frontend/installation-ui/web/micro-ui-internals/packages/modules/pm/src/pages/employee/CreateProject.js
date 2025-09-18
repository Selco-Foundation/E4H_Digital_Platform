import React, {useEffect, useMemo, useState} from "react";
import { FormComposerV2, Loader, Toast, Button, PopUp } from "@egovernments/digit-ui-react-components";
import {Stepper} from "@egovernments/digit-ui-components";
import useMDMS from "../../hooks/useMDMS";
import {useTranslation} from "react-i18next";
import useBoundary from "../../hooks/useBoundary";
import {ProjectService} from "../../services/Project";
import useProject from "../../hooks/useProject";
import {useHistory, useLocation} from "react-router-dom";
import CustomArrowRight from "../../components/Custom/CustomArrowRight";
import CustomCloseSvg from "../../components/Custom/CustomCloseSvg";
import { PMService } from "../../services/PMService";
import { useDispatch } from "react-redux";
import { populateResponsePage, populateWorkingProject } from "../../redux/actions";

const CreateProject = () => {

  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getStateId();
  const [currentKey, setCurrentKey] = useState(1);
  const [persistedFormData, setPersistedFormData] = useState({});
  const [defaultFormData, setDefaultFormData] = useState({});
  const [createdProject, setCreatedProject] = useState({});
  const history = useHistory();
  const location = useLocation();
  const { key, projectId } = Digit.Hooks.useQueryParams();
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [toast, setToast] = useState(null);
  const [blockUI, setBlockUI] = useState(null);
  const [file, setFile] = useState(null);
  const [invalidDataError, setInvalidDataError] = useState(null);
  const [getFormData, setGetFormData] = useState(null);
  const [showBackAlert, setShowBackAlert] = useState(false);
  const dispatch = useDispatch();

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const { data: boundaryData } = useBoundary("State");

  const { data : projectTypeData } = useMDMS(
    tenantId, "common-masters", ["ProjectType"],
    {
      select: (data) => {
        return data?.["common-masters"]?.["ProjectType"] || [];
      },
      enabled: true,
    }
  );

  const { data: projectData, revalidate: invalidateProjectData } = useProject({
    id: [projectId],
  });

  useEffect(() => {
    if (createdProject?.id && key) {
      setCurrentKey(parseInt(key));
    }
  }, [createdProject?.id]);

  useEffect(() => {
    const project = projectData?.projects?.[0];
    if (project) {
      dispatch(populateWorkingProject(project));
      setCreatedProject(project);
    }
  }, [projectData])

  useEffect(() => {
    if (createdProject?.id) {
      history.replace({
        pathname: location.pathname,
        search: `projectId=${createdProject.id}&key=${currentKey}`,
      });
    }

  }, [createdProject?.id, currentKey])

  useEffect(()=>{
    if(toast){
      setTimeout(()=>{
        setToast(null);
      },2500)
    }
  },[toast])

  const formatDate = (timestamp) => {
    const date = new Date(timestamp);
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const year = date.getFullYear();
    return `${year}-${month}-${day}`;
  };

  useEffect(() => {
    if (createdProject?.id && projectTypeData) {
      const formData = {
        projectDetails: {
          projectType: projectTypeData.filter((projectType) => projectType.name === createdProject.projectType)?.[0],
          justificationCode: createdProject.additionalDetails.justificationCode,
          projectDuration: {
            startDate: formatDate(createdProject.startDate),
            endDate: formatDate(createdProject.endDate),
          }
        },
        geographyDetails: createdProject.additionalDetails.geographyDetails,
      }

      setPersistedFormData(formData);
    }
  }, [createdProject, projectTypeData]);

  const handleFacilityDataDownload = async () => {
    setBlockUI(true);
    try {
      await PMService.projectFacilityDataTemplateDownload(createdProject.additionalDetails.geographyDetails, t);
      setToast({
        label: t("PM_TOAST_FACILITY_TEMPLATE_DOWNLOAD_SUCCESS"),
        key: "success",
      })

    } catch (error) {
      console.error("Error downloading project facility data template", error);
      setToast({
        label: t("PM_TOAST_FACILITY_TEMPLATE_DOWNLOAD_ERROR"),
        key: "error"
      })

    } finally {
      setBlockUI(false);
    }
  }

  const handleFacilityDataUpload = async (file) => {

    setBlockUI(true);
    let uploadedFile;
    try {
      const response = await PMService.projectFacilityDataTemplateUpload(file, createdProject?.id);

      if (response.errorCode === "INVALID_TEMPLATE") {
        setToast({
          key: "error",
          label: t("PM_TOAST_FACILITY_DATA_UPLOAD_TEMPLATE_ERROR")
        })

      } else if (response.errorCode === "INVALID_DATA") {
        setInvalidDataError({
          label: `${response.errorCount} ${t("PM_HEALTH_FACILITIES_VALIDATION_FAILED")}`
        })
        uploadedFile = {
          name: response.file.name || file.name,
          data: response.file.data,
          errorCodes: ["INVALID_DATA"]
        }

      } else {
        setToast({
          key: "success",
          label: t("PM_TOAST_FACILITY_DATA_UPLOAD_SUCCESS"),
        })
        uploadedFile = {
          name: response.file.name || file.name,
          data: response.file.data,
        }
      }

    } catch (e) {
      console.error("Error uploading template", e);
      setToast({
        key: "error",
        label: t("PM_TOAST_FACILITY_DATA_UPLOAD_ERROR"),
      })
    } finally {
      setBlockUI(false);
    }

    setFile(uploadedFile);
  }

  const config = useMemo(
    () => [
      {
        key: "1",
        body: [
          {
            inline: true,
            label: "PM_CREATE_PROJECT_LABEL_PROJECT_TYPE",
            isMandatory: true,
            key: "projectType",
            type: "dropdown",
            disable: !!createdProject?.projectType,
            route: "name",
            nextRoute: "justification-code",
            populators: {
              name: "projectType",
              error: t("CORE_COMMON_REQUIRED"),
              optionsKey: "name",
              required: true,
              options: projectTypeData || [],
            },
          },
          {
            inline: true,
            label: "PM_CREATE_PROJECT_LABEL_JUSTIFICATION_CODE",
            isMandatory: true,
            key: "justificationCode",
            type: "text",
            disable: !!createdProject?.additionalDetails?.justificationCode,
            route: "justification-code",
            nextRoute: "project-duration",
            populators: {
              name: "justificationCode",
              error: t("CORE_COMMON_REQUIRED"),
            },
          },
          {
            inline: true,
            label: "PM_CREATE_PROJECT_LABEL_PROJECT_DATES",
            isMandatory: true,
            key: "projectDuration",
            type: "component",
            component: "PMDateRange",
            disable: false,
            customProps: {
              name: "projectDuration",
              disableStartDate: !!createdProject?.id,
              disableEndDate: false,
            },
            route: "project-duration",
            nextRoute: "",
            populators: {
              name: "projectDuration",
              error: t("CORE_COMMON_REQUIRED"),
            },
          },
        ],
      },
      {
        key: "2",
        body: [
          {
            inline: true,
            label: "PM_CREATE_PROJECT_LABEL_STATE",
            isMandatory: true,
            key: "state",
            type: "component",
            component: "PMStateSelector",
            customProps: {
              name: "state",
              disable: !!createdProject?.additionalDetails?.geographyDetails?.state,
              t,
              boundaryData,
            },
            disable: false,
            route: "state",
            nextRoute: "districts",
            populators: {
              name: "state",
              error: t("CORE_COMMON_REQUIRED"),
            },
          },
          {
            inline: true,
            label: "PM_CREATE_PROJECT_LABEL_DISTRICTS",
            isMandatory: true,
            key: "districts",
            type: "component",
            component: "PMDistrictSelector",
            customProps: {
              name: "districts",
              stateIdentifier: "state",
              selectedOptions: createdProject?.additionalDetails?.geographyDetails?.districts || [],
              t,
              boundaryData,
            },
            disable: false,
            route: "districts",
            nextRoute: "blocks",
            populators: {
              name: "districts",
              error: t("CORE_COMMON_REQUIRED"),
            },
          },
          {
            inline: true,
            label: "PM_CREATE_PROJECT_LABEL_BLOCKS",
            isMandatory: true,
            key: "blocks",
            type: "component",
            component: "PMBlockSelector",
            customProps: {
              name: "blocks",
              districtsIdentifier: "districts",
              selectedOptions: createdProject?.additionalDetails?.geographyDetails?.blocks || [],
              t,
              boundaryData,
            },
            disable: false,
            route: "blocks",
            nextRoute: "",
            populators: {
              name: "blocks",
              error: t("CORE_COMMON_REQUIRED"),
            },
          },
        ],
      },
      {
        key: "3",
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
              heading: "PM_CREATE_PROJECT_HEAD_DOWNLOAD_FACILITY_TEMPLATE",
              description: "PM_CREATE_PROJECT_HEAD_DOWNLOAD_FACILITY_TEMPLATE_DESC",
              handleDownload: handleFacilityDataDownload,
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
            isMandatory: !createdProject?.status,
            key: "uploadFacilityData",
            type: "component",
            component: "PMUploadFacilityData",
            withoutLabelFieldPair: true,
            withoutLabel: true,
            route: "upload-facility-data",
            customProps: {
              name: "uploadFacilityData",
              allowedFileTypes: [".csv", ".xls", ".xlsx"],
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
    [t, projectTypeData, boundaryData, createdProject, file, invalidDataError]
  );

  const filterConfig = (config, currentKey) => {
    return config.filter((step) => parseInt(step.key) === currentKey);
  };

  const [filteredConfig, setFilteredConfig] = useState(filterConfig(config, currentKey));

  useEffect(() => {
    setFilteredConfig(filterConfig(config, currentKey));
  }, [config, currentKey])

  useEffect(() => {
    switch (currentKey) {
      case 1:
        setDefaultFormData(persistedFormData.projectDetails);
        break;
      case 2:
        setDefaultFormData(persistedFormData.geographyDetails);
        break;
    }
  }, [persistedFormData, currentKey]);

  const formatDataForCreate = (data) => {
    return {
      projectType: data.projectDetails.projectType.name,
      projectSubType: "PROJECT",
      department: "",
      description: "",
      referenceID: "1",
      parent: "",
      startDate: (new Date(data.projectDetails.projectDuration.startDate)).getTime(),
      endDate: (new Date(data.projectDetails.projectDuration.endDate)).getTime(),
      additionalDetails: {
        geographyDetails: data.geographyDetails,
        justificationCode: data.projectDetails.justificationCode,
      },
      address: {
        boundaryType: "State",
        boundary: data.geographyDetails.state.code,
        tenantId
      },
      tenantId
    }
  }

  const formatDataForUpdate = (data) => {
    const project = {
      ...createdProject,
      projectType: data.projectDetails.projectType.name,
      startDate: (new Date(data.projectDetails.projectDuration.startDate)).getTime(),
      endDate: (new Date(data.projectDetails.projectDuration.endDate)).getTime(),
      additionalDetails: {
        ...createdProject.additionalDetails,
        geographyDetails: data.geographyDetails,
        justificationCode: data.projectDetails.justificationCode,
      },
      tenantId
    }

    if (project.address.boundary !== data.geographyDetails.state.code) {
      project.address = {
        boundaryType: "State",
        boundary: data.geographyDetails.state.code,
        tenantId
      }
    }

    return project;
  }

  const upsertProject = async (projectData) => {

    setBlockUI(true);
    let projectUpsertData;
    if (createdProject?.id) {
      projectUpsertData = {
        Projects: [formatDataForUpdate(projectData)],
        isCascadingProjectDateUpdate: true,
        apiOperation: "UPDATE"
      };
    } else {
      projectUpsertData = {
        Projects: [formatDataForCreate(projectData)],
        apiOperation: "CREATE"
      };
    }

    try {
      const projectResponse = await ProjectService.upsertProject(projectUpsertData);
      const createdProjectResponse = projectResponse.Project?.[0];
      await invalidateProjectData();
      history.replace({
        pathname: location.pathname,
        search: `projectId=${createdProjectResponse.id}&key=${currentKey + 1}`,
      });
      setCurrentKey(prev => prev + 1);
      setToast({
        key: "success",
        label: createdProject?.id ? t("PM_TOAST_DRAFT_PROJECT_UPDATION_SUCCESS") : t("PM_TOAST_DRAFT_PROJECT_CREATION_SUCCESS"),
      })

    } catch (e) {
      console.error(`Error ${ createdProject?.id ? `updating` : `creating` } project`, e);
      setToast({
        key: "error",
        label: createdProject?.id ? t("PM_TOAST_DRAFT_PROJECT_UPDATION_ERROR") : t("PM_TOAST_DRAFT_PROJECT_CREATION_ERROR"),
      })

    } finally {
      setBlockUI(false);
    }
  }

  const setFormAccessors = ({setValue, getValues}) => {
    setGetFormData(() => getValues)
  }

  const getNextActionLabel = () => {
    if (currentKey === 1 || currentKey === 2) {
      return t("CORE_COMMON_NEXT");
    } else {
      return t("CORE_COMMON_SAVE");
    }
  };

  const getHeading = () => {
    switch (currentKey) {
      case 1:
        return t("PM_CREATE_PROJECT_HEAD_PROJECT_DETAILS");
      case 2:
        return t("PM_CREATE_PROJECT_HEAD_GEOGRAPHY_DETAILS");
    }
  }

  const getDescription = () => {
    switch (currentKey) {
      case 1:
        return t("PM_CREATE_PROJECT_HEAD_PROJECT_DETAILS_DESC");
      case 2:
        return t("PM_CREATE_PROJECT_HEAD_GEOGRAPHY_DETAILS_DESC");
    }
  }

  const onStepClick = (key) => {
    if (key >= currentKey) return;
    setCurrentKey(key + 1);
  }

  const handleCompleteProjectCreation = async () => {

    if (!createdProject?.id) {
      return;
    }

    try {
      let response = {};
      if (!createdProject?.status) {
        response = await ProjectService.updateProjectWorkflow(createdProject.id, "SCHEDULED", "Schedule Project");
      }

      await invalidateProjectData();
      dispatch(populateResponsePage({
        response: response,
        message: !!createdProject?.status ? t("PM_COMMON_PROJECT_UPDATED") : t("PM_COMMON_PROJECT_CREATED"),
        createdId: createdProject.name,
        info: t("PM_COMMON_PROJECT_NAME"),
        secondaryRedirectionLabel: t("PM_LABEL_CREATE_FIELD_PLAN"),
        onSecondaryRedirection: () => history.push(`/${window?.contextPath}/employee/pm/project/${createdProject.id}/details`),
      }))
      history.push(`/${window?.contextPath}/employee/pm/response`);

    } catch (error) {
      console.error("Error updating project workflow", error);
      setToast({
        key: "error",
        label: t("PM_TOAST_DRAFT_PROJECT_WORKFLOW_UPDATE_ERROR"),
      })
    }
  }

  const handleFormSubmit = async (data) => {
    switch (currentKey) {
      case 1:
        setPersistedFormData(prev => ({...prev, projectDetails: data}));
        setCurrentKey(prev => prev + 1);
        break;
      case 2:
        const newProjectFormData = {...persistedFormData, geographyDetails: data};
        setPersistedFormData(newProjectFormData);
        await upsertProject(newProjectFormData);
        break;
      case 3:
        await handleCompleteProjectCreation();
    }
  }

  const handleBackNavigation = () => {
    switch (currentKey) {
      case 1:
        setShowBackAlert(true);
        break;
      case 2:
        const geographyDetails = {
          state: getFormData("state"),
          districts: getFormData("districts"),
          blocks: getFormData("blocks"),
        }
        setPersistedFormData(prev => ({...prev, geographyDetails : geographyDetails}))
        setCurrentKey(prev => prev - 1);
        break;
      case 3:
        if (file) {
          setShowBackAlert(true);
        } else {
          setCurrentKey(prev => prev - 1);
        }
    }
  }

  const handleConfirmBackNavigation = () => {
    switch (currentKey) {
      case 1:
        setShowBackAlert(false);
        window.history.back();
        break;
      case 3:
         setShowBackAlert(false);
         setCurrentKey(prev => prev - 1);
    }
  }

  const getDefaultValues = () => {
    switch (currentKey) {
      case 1:
        return persistedFormData.projectDetails;
      case 2:
        return persistedFormData.geographyDetails;
    }
  }

  return (
    <div style={{padding: mobileView ? "15px" : "0px"}}>
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
        <div style={{fontSize: "40px", fontWeight: "bold", fontFamily: "Roboto Condensed", marginBottom: "20px", color: "#0B0C0C"}}>
          {createdProject?.name}
        </div>
      )}
      <Stepper
        customSteps={[
          "PM_CREATE_PROJECT_HEAD_PROJECT_DETAILS",
          "PM_CREATE_PROJECT_HEAD_GEOGRAPHY_DETAILS",
          "PM_CREATE_PROJECT_HEAD_FACILITY_DATA",
        ]}
        onStepClick={onStepClick}
        currentStep={currentKey}
        style={{
          marginBottom: "20px"
        }}
      />
      <FormComposerV2
        key={JSON.stringify(defaultFormData)}
        config={filteredConfig}
        onSubmit={handleFormSubmit}
        label={getNextActionLabel()}
        showSecondaryLabel={true}
        secondaryLabel={t("CORE_COMMON_BACK")}
        onSecondayActionClick={handleBackNavigation}
        heading={getHeading()}
        headingStyle={{
          fontSize: "32px",
          marginBottom: "20px",
        }}
        description={getDescription()}
        descriptionStyle={{
          fontSize: "14px",
          fontFamily: "Roboto",
          fontWeight: "400",
          color: "#0B0C0C"
        }}
        isDescriptionBold={true}
        getFormAccessors={setFormAccessors}
        defaultValues={getDefaultValues()}
        showMultipleCardsWithoutNavs={true}
        noBreakLine={true}
        cardStyle={{padding: "20px"}}
        actionClassName={"reverse-actionbar"}
        submitIcon={<CustomArrowRight />}
      />
      {toast && (
        <Toast
          error={toast.key === "error"}
          warning={toast.key === "warning"}
          style={{
            ...(toast.key === "error" ? {backgroundColor: "#B91900"} : {}),
            ...(mobileView ? {bottom: "120px"} : {})
          }}
          label={t(toast.label)}
          isDleteBtn={true}
          onClose={() => setToast(null)}
        />
      )}
      {showBackAlert && (
        <PopUp>
          <div
            style={{
              backgroundColor: "white",
              position: "fixed",
              top: "50%",
              left: "50%",
              transform: "translate(-50%, -50%)",
              width: "400px",
              maxWidth: "95%",
              padding: "24px",
              borderRadius: "5px",
            }}
          >
            <div
              style={{
                width: "100%",
                position: "relative",
              }}
            >
              <button
                type={"button"}
                style={{
                  cursor: "pointer",
                  position: "absolute",
                  top: "-15px",
                  right: "-15px",
                  backgroundColor: "#D6D5D4",
                  display: "flex",
                  alignItems: "center",
                  padding: "0",
                  borderRadius: "3px",
                }}
                onClick={() => setShowBackAlert(false)}
              >
                <CustomCloseSvg fill={"transparent"} />
              </button>
            </div>
            <h2
              style={{
                margin: "0 0 16px 0",
                fontSize: "20px",
                fontWeight: "600",
                color: "#333",
                textAlign: "center",
              }}
            >
              {t("CORE_COMMON_ALERT")}
            </h2>

            <p
              style={{
                fontSize: "16px",
                color: "#555",
                marginBottom: "24px",
                textAlign: "center",
              }}
            >
              {t("PM_ALERT_LOSE_UNSAVED_DATA")}
            </p>
            <div style={{display: "flex", justifyContent: "space-around"}}>
              <Button variation={"secondary"} label={t("CORE_COMMON_CANCEL")} onButtonClick={() => setShowBackAlert(false)} />
              <Button variation={"primary"} label={t("CORE_COMMON_CONTINUE")} onButtonClick={handleConfirmBackNavigation} />
            </div>
          </div>
        </PopUp>
      )}
    </div>
  )

}

export default CreateProject;