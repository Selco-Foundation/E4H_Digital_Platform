import React, { useCallback, useEffect, useMemo, useState } from "react";
import useBoundary from "../../hooks/useBoundary";
import useMDMS from "../../hooks/useMDMS";
import useProject from "../../hooks/useProject";
import { useTranslation } from "react-i18next";
import { Button, FormComposerV2, Loader, PopUp, Toast } from "@egovernments/digit-ui-react-components";
import { Stepper } from "@egovernments/digit-ui-components";
import { useDispatch } from "react-redux";
import { populateResponsePage, populateWorkingFieldPlan, populateWorkingProject } from "../../redux/actions";
import { useHistory } from "react-router-dom";
import useFieldPlan from "../../hooks/useFieldPlan";
import { FieldPlanService } from "../../services/FieldPlan";
import { PMService } from "../../services/PMService";
import { ActivityService } from "../../services/Activity";
import useOrganization from "../../hooks/useOrganization";
import useOrganizationUser from "../../hooks/useOrganizationUser";
import useActivityAssignment from "../../hooks/useActivityAssignment";
import CommonUtils from "../../utilities/CommonUtils";
import UnsavedDataAlert from "../../components/UnsavedDataAlert";

const CreateAMC = () => {

  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getStateId();
  const [currentKey, setCurrentKey] = useState(1);
  const [persistedFormData, setPersistedFormData] = useState({});
  const [defaultFormData, setDefaultFormData] = useState({});
  const [createdProject, setCreatedProject] = useState(null);
  const [createdFieldPlan, setCreatedFieldPlan] = useState(null);
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [toast, setToast] = useState(null);
  const [blockUI, setBlockUI] = useState(null);
  const [file, setFile] = useState(null);
  const [invalidDataError, setInvalidDataError] = useState(null);
  const [getFormData, setGetFormData] = useState(null);
  const [backAlert, setBackAlert] = useState(null);
  const [boundaryData, setBoundaryData] = useState(null);
  const history = useHistory();
  const url = window.location.href;
  const projectId = url.split("project/")[1].split("/")[0];
  const dispatch = useDispatch();

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const { data: fetchedBoundaryData } = useBoundary("State");

  const { data: activityData } = useMDMS(tenantId, "common-masters", ["Activities"], {
    select: (data) => {
      return data?.["common-masters"]?.["Activities"] || [];
    },
    enabled: true,
  });

  const { isLoading: projectDataLoading, data: projectData } = useProject({
    id: [projectId],
  });

  const { data: organizationData } = useOrganization();

  useEffect(() => {
    const project = projectData?.projects?.[0];
    if (project) {
      dispatch(populateWorkingProject(project));
      setCreatedProject(project);
    }
  }, [projectData]);

  useEffect(()=>{
    if (toast) {
      setTimeout(()=>{
        setToast(null);
      },2500)
    }
  },[toast])

  useEffect(() => {
    if (fetchedBoundaryData && createdProject) {
      const selectedStateCode = createdProject.additionalDetails.geographyDetails.state.code;
      const selectedDistrictCodes = createdProject.additionalDetails.geographyDetails.districts.map((district) => district.code);
      const selectedBlockCodes = createdProject.additionalDetails.geographyDetails.blocks.map((block) => block.code);

      const states = fetchedBoundaryData.states.filter((state) => state.code === selectedStateCode);
      const districts = fetchedBoundaryData.districts.filter((district) => selectedDistrictCodes.includes(district.code));
      const blocks = fetchedBoundaryData.blocks.filter((block) => selectedBlockCodes.includes(block.code));

      setBoundaryData({
        states,
        districts,
        blocks,
      });
    }
  }, [fetchedBoundaryData, createdProject]);

  const getDefaultActivityAssignments = useCallback(() => {
    return activityData
      ?.filter((activity) => activity.code.toUpperCase() === "AMC")
        .map((activity) => ({
          activity: activity,
          users: [
            {
              poNumber: { value: "", error: "", },
              organization: { value: null, error: "", },
              role: { value: null, error: "", },
              email: { value: null, error: "", },
              isEmailSent: false,
            }
          ],
        }))
  }, [activityData])

  useEffect(() => {
    if (createdProject) {
      const formData = {
        geographyDetails: {
          state: createdProject.additionalDetails.geographyDetails.state,
        },
        activityDetails: {
          activityUserAssignment: getDefaultActivityAssignments(),
        }
      }

      setPersistedFormData(formData);
    }
  }, [createdProject, getDefaultActivityAssignments]);

  const handleFacilityDataDownload = useCallback(async () => {

    setBlockUI(true);
    try {
      await PMService.downloadAMCFacilityDataTemplate(createdProject.id, persistedFormData, t);

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
  }, [createdProject, persistedFormData, t])

  const handleFacilityDataUpload = useCallback(async (chosenFile) => {

    setBlockUI(true);
    let uploadedFile;
    try {
      const response = await PMService.uploadAMCFacilityDataTemplate(chosenFile, createdProject.id, persistedFormData);

      if (response.errorCode === "INVALID_TEMPLATE") {
        setToast({
          key: "error",
          label: t("PM_TOAST_FACILITY_DATA_UPLOAD_TEMPLATE_ERROR")
        })
        setInvalidDataError(null);

      } else if (response.errorCode === "INVALID_DATA") {
        setInvalidDataError({
          label: `${response.errorCount} ${t("PM_HEALTH_FACILITIES_VALIDATION_FAILED")}`
        })
        uploadedFile = {
          name: response.file.name || chosenFile.name,
          data: response.file.data,
          errorCodes: ["INVALID_DATA"]
        }

      } else {
        setToast({
          key: "success",
          label: t("PM_TOAST_FACILITY_DATA_UPLOAD_SUCCESS"),
        })
        setInvalidDataError(null);
        uploadedFile = {
          name: response.file.name || chosenFile.name,
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
  }, [createdProject, persistedFormData, t]);

  const validateActivityData = (activityData) => {
    let faultyData = false;
    let emptyData = true;

    const validatedData = activityData.map((dataEntry) => ({
      ...dataEntry,
      users: dataEntry.users.map((userEntry) => {
        const newUserEntry = {}

        if (Object.keys(userEntry).every((key) => (["id", "isEmailSent", "deleteAssignment", "savedAssignment"].includes(key) || !userEntry[key].value))) {
          Object.keys(userEntry).forEach((key) => {
            if (["id", "isEmailSent", "deleteAssignment", "savedAssignment"].includes(key)) {
              newUserEntry[key] =  userEntry[key];
            } else {
              newUserEntry[key] =  {
                ...userEntry[key],
                error: ""
              };
            }
          })
          return newUserEntry;
        }

        emptyData = false;
        Object.keys(userEntry).forEach((key) => {
          if (["id", "isEmailSent", "deleteAssignment", "savedAssignment"].includes(key)) {
            newUserEntry[key] =  userEntry[key];
          } else if (!userEntry[key].value) {
            faultyData = true;
            newUserEntry[key] = {
              ...userEntry[key],
              error: t("CORE_COMMON_REQUIRED")
            };
          } else {
            newUserEntry[key] =  {
              ...userEntry[key],
              error: ""
            };
          }
        })

        return newUserEntry;
      })
    }))

    return {
      faultyData,
      emptyData,
      validatedData,
    }
  }

  const config = useMemo(
    () => [
      {
        key: "1",
        body: [
          {
            inline: true,
            label: "PM_CREATE_FIELD_PLAN_LABEL_STATE",
            isMandatory: true,
            key: "state",
            type: "component",
            component: "PMStateSelector",
            customProps: {
              name: "state",
              disable: true,
              t,
              boundaryData,
            },
            disable: true,
            route: "state",
            nextRoute: "districts",
            populators: {
              name: "state",
              error: "Required",
            },
          },
          {
            inline: true,
            label: "PM_CREATE_FIELD_PLAN_LABEL_DISTRICTS",
            isMandatory: true,
            key: "districts",
            type: "component",
            component: "PMDistrictSelector",
            customProps: {
              name: "districts",
              stateIdentifier: "state",
              selectedOptions: [],
              t,
              boundaryData,
            },
            disable: false,
            route: "districts",
            nextRoute: "blocks",
            populators: {
              name: "districts",
              error: "Required",
            },
          },
          {
            inline: true,
            label: "PM_CREATE_FIELD_PLAN_LABEL_BLOCKS",
            isMandatory: true,
            key: "blocks",
            type: "component",
            component: "PMBlockSelector",
            customProps: {
              name: "blocks",
              districtsIdentifier: "districts",
              selectedOptions: [],
              t,
              boundaryData,
            },
            disable: false,
            route: "blocks",
            nextRoute: "",
            populators: {
              name: "blocks",
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
            key: "activityUserAssignment",
            withoutLabelFieldPair: true,
            withoutLabel: true,
            type: "component",
            component: "PMAMCUserManager",
            disable: false,
            route: "activity-details",
            customProps: {
              name: "activityUserAssignment",
              t,
              activityData,
              organizationData,
            },
            nextRoute: "",
            populators: {
              name: "activityUserAssignment",
              error: "Required",
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
              heading: "PM_CREATE_AMC_HEAD_DOWNLOAD_FACILITY_TEMPLATE",
              description: "PM_CREATE_AMC_HEAD_DOWNLOAD_FACILITY_TEMPLATE_DESC",
              handleDownload: handleFacilityDataDownload,
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
        key: "3",
        body: [
          {
            isMandatory: false,
            key: "uploadFacilityData",
            type: "component",
            component: "PMUploadFacilityData",
            withoutLabelFieldPair: true,
            withoutLabel: true,
            route: "upload-facility-data",
            customProps: {
              name: "uploadFacilityData",
              allowedFileTypes: [".xlsx"],
              handleFileUpload: handleFacilityDataUpload,
              invalidDataError: invalidDataError,
              errorViewLabel: "CORE_COMMON_VIEW_ERRORS",
              heading: "PM_CREATE_AMC_HEAD_UPLOAD_FACILITY_DATA",
              description: "PM_CREATE_AMC_HEAD_UPLOAD_FACILITY_DATA_DESC",
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
              error: "Required",
            },
          },
        ],
      },
    ],
    [t, activityData, boundaryData, createdProject, organizationData, handleFacilityDataDownload, handleFacilityDataUpload, file, invalidDataError]
  );

  const filterConfig = (config, currentKey) => {
    return config.filter((step) => parseInt(step.key) === currentKey);
  };

  const [filteredConfig, setFilteredConfig] = useState(filterConfig(config, currentKey));

  useEffect(() => {
    setFilteredConfig(filterConfig(config, currentKey));
  }, [config, currentKey]);

  useEffect(() => {
    switch (currentKey) {
      case 1:
        setDefaultFormData(persistedFormData.geographyDetails);
        break;
      case 2:
        setDefaultFormData(persistedFormData.activityDetails);
        break;
      case 3:
        setDefaultFormData({});
    }
  }, [persistedFormData, currentKey]);

  const validateRolesPresence = (activityFormData) => {
    let allRolesPresent = true;

    activityFormData.forEach((dataEntry) => {
      const completeRoleCodes = activityData
        ?.filter((activity) => activity?.code === dataEntry.activity?.code)?.[0]?.roles
        ?.map((role) => role?.code);

      const selectedRoleCodes = dataEntry.users
        .filter((user) => !user?.deleteAssignment)
        .map((user) => user?.role?.value?.code);

      completeRoleCodes?.forEach((code) => {
        if (!selectedRoleCodes.includes(code)) {
          allRolesPresent = false;
        }
      })
    })

    return allRolesPresent;
  }

  const saveActivityDetails = (activityData) => {

    const { faultyData, validatedData } = validateActivityData(activityData);

    if (faultyData) {
      setPersistedFormData((prevState) => ({
        ...prevState,
        activityDetails: {
          activityUserAssignment: validatedData,
        },
      }));

    } else if (!validateRolesPresence(activityData)) {
      setToast({
        key: "error",
        label: t("PM_TOAST_ACTIVITY_DETAILS_ROLES_PRESENCE_ERROR"),
      })

    } else {
      setPersistedFormData((prevState) => ({
        ...prevState,
        activityDetails: {
          activityUserAssignment: activityData,
        },
      }));
      setCurrentKey((prev) => prev + 1);
    }
  }

  const handleFormSubmit = async (data) => {
    switch (currentKey) {
      case 1:
        setPersistedFormData((prev) => ({ ...prev, geographyDetails: data }));
        setCurrentKey((prev) => prev + 1);
        break;
      case 2:
        saveActivityDetails(data.activityUserAssignment);
        break;
      case 3:
        if (!file) {
          setToast({
            label: t("PM_TOAST_FACILITY_UPLOAD_MANDATORY"),
            key: "error"
          })
        } else {
          dispatch(
            populateResponsePage({
              response: {},
              message: t("PM_COMMON_AMC_CREATED"),
              secondaryRedirectionLabel: t("PM_LABEL_GO_TO_PROJECT"),
              onSecondaryRedirection: () => history.push(`/${window?.contextPath}/employee/pm/project/${createdProject.id}/field-plans`),
            })
          );
          history.push(`/${window?.contextPath}/employee/pm/response`);
        }
    }
  };

  const setFormAccessors = ({ setValue, getValues }) => {
    setGetFormData(() => getValues);
  };

  const getNextActionLabel = () => {
    if (currentKey === 1 || currentKey === 2) {
      return t("CORE_COMMON_NEXT");
    } else {
      return t("CORE_COMMON_SUBMIT");
    }
  };

  const getHeading = () => {
    switch (currentKey) {
      case 1:
        return t("PM_CREATE_AMC_HEAD_GEOGRAPHY_DETAILS");
      case 2:
        return t("PM_CREATE_AMC_HEAD_USER_MANAGEMENT");
    }
  };

  const getDescription = () => {
    switch (currentKey) {
      case 1:
        return t("PM_CREATE_FIELD_PLAN_HEAD_FIELD_PLAN_DETAILS_DESC");
    }
  };

  const onStepClick = (key) => {
    if (key + 1 >= currentKey) return;
    switch (currentKey) {
      case 2:
        const currentActivityAssignments = getFormData("activityUserAssignment");
        setPersistedFormData((prevState) => ({
          ...prevState,
          activityDetails: {
            activityUserAssignment: currentActivityAssignments,
          },
        }));
        setCurrentKey(key + 1);
        break;
      case 3:
        setCurrentKey(key + 1);
    }
  };

  const handleBackNavigation = () => {
    switch (currentKey) {
      case 1:
        setBackAlert({
          continueAction: () => {
            window.history.back();
          }
        });
        break;
      case 2:
        const currentActivityAssignments = getFormData("activityUserAssignment");
        setPersistedFormData((prevState) => ({
          ...prevState,
          activityDetails: {
            activityUserAssignment: currentActivityAssignments,
          },
        }));
        setCurrentKey((prev) => prev - 1);
        break;
      case 3:
        setCurrentKey((prev) => prev - 1);
    }
  };

  const getDefaultValues = () => {
    switch (currentKey) {
      case 1:
        return persistedFormData.geographyDetails;
      case 2:
        return persistedFormData.activityDetails;
    }
  }

  if (projectDataLoading) {
    return <Loader />;
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
        <div style={{fontSize: "40px", fontWeight: "700", fontFamily: "Roboto Condensed", marginBottom: "20px", color: "#0B0C0C"}}>
          {createdProject?.name}
        </div>
      )}
      <Stepper
        customSteps={[
          "PM_CREATE_AMC_HEAD_GEOGRAPHY_DETAILS",
          "PM_CREATE_AMC_HEAD_USER_MANAGEMENT",
          "PM_CREATE_AMC_HEAD_FACILITY_DATA",
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
      {backAlert && <UnsavedDataAlert t={t} alert={backAlert} setAlert={setBackAlert} />}
    </div>
  )
}

export default CreateAMC;