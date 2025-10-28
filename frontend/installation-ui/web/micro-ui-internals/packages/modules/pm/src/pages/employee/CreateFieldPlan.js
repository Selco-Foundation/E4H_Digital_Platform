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

const CreateFieldPlan = () => {

  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getStateId();
  const [currentKey, setCurrentKey] = useState(1);
  const [persistedFormData, setPersistedFormData] = useState({});
  const [defaultFormData, setDefaultFormData] = useState({});
  const [createdProject, setCreatedProject] = useState(null);
  const [createdFieldPlan, setCreatedFieldPlan] = useState(null);
  const { key, fieldPlanId } = Digit.Hooks.useQueryParams();
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
  const [organizationIds, setOrganizationIds] = useState([""]);

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

  const {
    isLoading: fieldPlanDataLoading,
    data: fieldPlanData,
    revalidate: invalidateFieldPlanData
  } = useFieldPlan({
    tenantId,
    ids: [fieldPlanId],
  });

  const { data: organizationData } = useOrganization();

  const { data: organizationUserData } = useOrganizationUser({
    organizationIds,
  });

  const {
    isLoading: activityAssignmentDataLoading,
    data: activityAssignmentData,
    revalidate: invalidateActivityAssignmentData
  } = useActivityAssignment({
    fieldPlanIds: [fieldPlanId],
  })

  useEffect(() => {
    if (createdFieldPlan?.id && key) {
      setCurrentKey(parseInt(key));
    }
  }, [createdFieldPlan?.id]);

  useEffect(() => {
    const project = projectData?.projects?.[0];
    if (project) {
      dispatch(populateWorkingProject(project));
      setCreatedProject(project);
    }
  }, [projectData]);

  useEffect(() => {
    const fieldPlan = fieldPlanData?.fieldPlans?.[0];
    if (fieldPlan) {
      dispatch(populateWorkingFieldPlan(fieldPlan));
      setCreatedFieldPlan(fieldPlan);
    }
  }, [fieldPlanData]);

  useEffect(() => {
    if (organizationData) {
      setOrganizationIds(organizationData.organizations.map((organization) => organization.id));
    }
  }, [organizationData]);

  useEffect(() => {
    if (createdFieldPlan?.id) {
      history.replace({
        pathname: location.pathname,
        search: `fieldPlanId=${createdFieldPlan.id}&key=${currentKey}`,
      });
    }

  }, [createdFieldPlan?.id, currentKey])

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

  const formatDateForForm = (timestamp) => {
    const date = new Date(timestamp);
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const year = date.getFullYear();
    return `${year}-${month}-${day}`;
  };

  const getDefaultActivityAssignments = useCallback(() => {
    return createdFieldPlan?.activities
      ? createdFieldPlan.activities
        .map((activity) => ({
          activity: activity,
          users: [
            ...(
              activityAssignmentData?.activityAssignments?.filter((assignment) => assignment.activityCode === activity.code)?.length
                ? activityAssignmentData.activityAssignments
                  .filter((assignment) => assignment.activityCode === activity.code)
                  .sort((a, b) => a.auditDetails.createdTime - b.auditDetails.createdTime)
                  .map((assignment) => ({
                    id: assignment.id,
                    savedAssignment: assignment,
                    startDate: { value: formatDate(assignment.startDate), error: "", },
                    endDate: { value: formatDate(assignment.endDate), error: "", },
                    poNumber: { value: assignment.pocNumber, error: "", },
                    organization: {
                      value: organizationData?.organizations?.filter((organization) => (
                        organization?.id === organizationUserData?.organizationUsers?.filter((user) => user.uuid === assignment.assignedTo)?.[0]?.organizationId
                      ))?.[0],
                      error: "",
                    },
                    role: { value: assignment.role, error: "", },
                    email: {
                      value: organizationUserData?.organizationUsers?.filter((user) => user.uuid === assignment.assignedTo)?.[0],
                      error: "",
                    },
                    isEmailSent: assignment.isEmailSent,
                  }))
                : [
                  {
                    startDate: { value: "", error: "", },
                    endDate: { value: "", error: "", },
                    poNumber: { value: "", error: "", },
                    organization: { value: null, error: "", },
                    role: { value: null, error: "", },
                    email: { value: null, error: "", },
                    isEmailSent: false,
                  }
                ]
            )
          ],
        }))
      : null
  }, [createdFieldPlan, activityAssignmentData, organizationData, organizationUserData])

  useEffect(() => {
    if (createdFieldPlan?.id && boundaryData && activityData) {
      const savedActivityCodes = createdFieldPlan.activities.map((activity) => activity.code);

      const formData = {
        fieldPlanDetails: {
          state: boundaryData.states
            .filter((state) => state.code === createdFieldPlan.geographyDetails.state)
            .map((state) => ({
              code: state?.code,
              name: `STATE_${state?.code.toUpperCase()}`,
            }))
            ?.[0],

          districts: boundaryData.districts.filter((district) => createdFieldPlan.geographyDetails.districts.includes(district.code)),
          blocks: boundaryData.blocks.filter((block) => createdFieldPlan.geographyDetails.blocks.includes(block.code)),
          fieldPlanDuration: {
            startDate: formatDateForForm(createdFieldPlan.startDate),
            endDate: formatDateForForm(createdFieldPlan.endDate),
          },
          healthFacilitiesCount: createdFieldPlan.healthFacilityNumber,
          activities: activityData.filter((activity) => savedActivityCodes.includes(activity.code)),
        },
        activityDetails: {
          activityUserAssignment: getDefaultActivityAssignments(),
        }
      }

      setPersistedFormData(formData);
    } else if (createdProject) {
      const formData = {
        fieldPlanDetails: {
          state: createdProject.additionalDetails.geographyDetails.state,
        }
      }

      setPersistedFormData(formData);
    }
  }, [createdProject, createdFieldPlan, boundaryData, activityData, getDefaultActivityAssignments]);

  const handleFacilityDataDownload = async () => {

    setBlockUI(true);
    try {
      const geographyDetails = {
        state: boundaryData.states
          .filter((state) => state.code === createdFieldPlan.geographyDetails.state)
          .map((state) => ({
            code: state?.code,
          }))
          ?.[0],

        districts: boundaryData.districts.filter((district) => createdFieldPlan.geographyDetails.districts.includes(district.code)),
        blocks: boundaryData.blocks.filter((block) => createdFieldPlan.geographyDetails.blocks.includes(block.code)),
      }
      await PMService.downloadFieldPlanFacilityDataTemplate(createdProject.id, createdFieldPlan.id, geographyDetails, t);

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

  const handleFacilityDataUpload = async (chosenFile) => {

    setBlockUI(true);
    let uploadedFile;
    try {
      const response = await PMService.uploadFieldPlanFacilityDataTemplate(chosenFile, createdFieldPlan.id);

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
  };

  const formatDate = (timestamp) => {
    const date = new Date(timestamp);
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const year = date.getFullYear();
    return `${year}-${month}-${day}`;
  };

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

  const assignActivityUsers = async (activityData) => {

    const activityUserAssignmentsForCreate = [];
    const activityUserAssignmentsForUpdate = [];
    const activityUserAssignmentsForDelete = [];

    activityData.forEach((dataEntry) => {
      dataEntry.users.forEach((userEntry) => {

        if (Object.keys(userEntry).every((key) => (["id", "isEmailSent", "deleteAssignment", "savedAssignment"].includes(key) || !userEntry[key].value))) {
          return;
        }

        const activityUserAssignment = {
          tenantId: Digit.ULBService.getCurrentTenantId(),
          assignedTo: userEntry.email.value.uuid,
          assignedBy: Digit.UserService.getUser()?.info?.uuid,
          fieldPlanId: createdFieldPlan?.id,
          role: userEntry.role.value,
          activityId: dataEntry.activity.code,
          pocNumber: userEntry.poNumber.value,
          startDate: (new Date(userEntry.startDate.value)).getTime(),
          endDate: (new Date(userEntry.endDate.value)).getTime(),
        };

        if (userEntry.deleteAssignment) {
          activityUserAssignmentsForDelete.push({
            ...userEntry.savedAssignment,
            ...activityUserAssignment,
            activityId: userEntry.savedAssignment?.activityId,
            activityCode: dataEntry.activity.code,
          });

        } else if (userEntry.id) {
          activityUserAssignmentsForUpdate.push({
            ...userEntry.savedAssignment,
            ...activityUserAssignment,
            activityId: userEntry.savedAssignment?.activityId,
            activityCode: dataEntry.activity.code,
          });

        } else {
          activityUserAssignmentsForCreate.push(activityUserAssignment);
        }
      })
    });

    if (activityUserAssignmentsForCreate.length) {
      await ActivityService.createActivityAssignment(activityUserAssignmentsForCreate);
    }
    if (activityUserAssignmentsForUpdate.length) {
      await ActivityService.updateActivityAssignment(activityUserAssignmentsForUpdate);
    }
    if (activityUserAssignmentsForDelete.length) {
      await ActivityService.deleteActivityAssignment(activityUserAssignmentsForDelete);
    }

    if (activityUserAssignmentsForCreate.length || activityUserAssignmentsForUpdate.length || activityUserAssignmentsForDelete.length) {
      await invalidateActivityAssignmentData();
    }
  }

  const handleActivityDataSave = async (activityData) => {

    const { faultyData, emptyData, validatedData } = validateActivityData(activityData);

    if (faultyData) {
      setPersistedFormData((prevState) => ({
        ...prevState,
        activityDetails: {
          activityUserAssignment: validatedData,
        },
      }));

    } else if (emptyData) {
      setToast({
        key: "error",
        label: t("PM_TOAST_ACTIVITY_DETAILS_EMPTY_SAVE_WARNING"),
      })
      setPersistedFormData((prevState) => ({
        ...prevState,
        activityDetails: {
          activityUserAssignment: validatedData,
        },
      }));

    } else {
      try {
        setBlockUI(true);
        await assignActivityUsers(activityData);
        setToast({
          key: "success",
          label: t("PM_TOAST_ACTIVITY_DETAILS_SAVE_SUCCESS"),
        })

      } catch (error) {
        console.error("Error assigning users for field plan activities", error);
        setToast({
          key: "error",
          label: t("PM_TOAST_ACTIVITY_DETAILS_SAVE_ERROR"),
        })

      } finally {
        setBlockUI(false);
      }
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
              selectedOptions: (createdFieldPlan?.id && createdFieldPlan?.status !== "DRAFT") ? boundaryData?.districts?.filter((district) => createdFieldPlan.geographyDetails.districts.includes(district?.code)) : [],
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
              selectedOptions: (createdFieldPlan?.id && createdFieldPlan?.status !== "DRAFT") ? boundaryData?.blocks?.filter((block) => createdFieldPlan.geographyDetails.blocks.includes(block?.code)) : [],
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
          {
            inline: true,
            label: "PM_CREATE_FIELD_PLAN_LABEL_FIELD_PLAN_DATES",
            isMandatory: true,
            key: "fieldPlanDuration",
            type: "component",
            component: "PMDateRange",
            disable: false,
            customProps: {
              name: "fieldPlanDuration",
              minimumStartDate: createdProject?.startDate ? formatDate(createdProject.startDate) : "",
              maximumStartDate: createdProject?.endDate ? formatDate(createdProject.endDate) : "",
              minimumEndDate: createdProject?.startDate ? formatDate(createdProject.startDate) : "",
              maximumEndDate: createdProject?.endDate ? formatDate(createdProject.endDate) : "",
            },
            route: "field-plan-duration",
            nextRoute: "number-of-health-facilities",
            populators: {
              name: "fieldPlanDuration",
              error: "Required",
            },
          },
          {
            inline: true,
            label: "PM_CREATE_FIELD_PLAN_LABEL_NO_OF_HEALTH_FACILITIES",
            isMandatory: true,
            key: "healthFacilitiesCount",
            type: "number",
            disable: false,
            route: "number-of-health-facilities",
            nextRoute: "activities",
            populators: {
              name: "healthFacilitiesCount",
              error: "Required",
              validation: {
                max: 1000000,
              },
            },
          },
          {
            inline: true,
            label: "PM_CREATE_FIELD_PLAN_LABEL_ACTIVITIES",
            isMandatory: true,
            key: "activities",
            type: "component",
            component: "PMActivitySelector",
            disable: false,
            customProps: {
              name: "activities",
              selectedOptions: (createdFieldPlan?.id && createdFieldPlan?.status !== "DRAFT") ? activityData?.filter((activity) => createdFieldPlan.activities.map((activity) => activity.code).includes(activity?.code)) : [],
              description: "PM_CREATE_FIELD_PLAN_LABEL_ACTIVITIES_DESC",
              t,
              activityData,
            },
            route: "activities",
            nextRoute: "",
            populators: {
              name: "activities",
              error: "Required",
            },
          },
        ],
      },
      {
        key: "2",
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
              heading: "PM_CREATE_FIELD_PLAN_HEAD_DOWNLOAD_FACILITY_TEMPLATE",
              description: "PM_CREATE_FIELD_PLAN_HEAD_DOWNLOAD_FACILITY_TEMPLATE_DESC",
              handleDownload: handleFacilityDataDownload,
              t,
            },
            route: "project-duration-2",
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
              heading: "PM_CREATE_FIELD_PLAN_HEAD_UPLOAD_FACILITY_DATA",
              description: "PM_CREATE_FIELD_PLAN_HEAD_UPLOAD_FACILITY_DATA_DESC",
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
      {
        key: "3",
        body: [
          {
            isMandatory: false,
            key: "activityUserAssignment",
            withoutLabelFieldPair: true,
            withoutLabel: true,
            type: "component",
            component: "PMActivityDetails",
            disable: false,
            route: "activity-details",
            customProps: {
              name: "activityUserAssignment",
              fieldPlanStartDate: createdFieldPlan?.startDate ? formatDate(createdFieldPlan.startDate) : "",
              fieldPlanEndDate: createdFieldPlan?.endDate ? formatDate(createdFieldPlan.endDate) : "",
              onActivityDataSave: handleActivityDataSave,
              t,
              activityData,
              organizationData,
            },
            nextRoute: "",
            populators: {
              name: "activityDetails",
              error: "Required",
            },
          },
        ],
      },
    ],
    [t, activityData, boundaryData, createdProject, createdFieldPlan, organizationData, file, invalidDataError]
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
        setDefaultFormData(persistedFormData.fieldPlanDetails);
        break;
      case 2:
        setDefaultFormData(persistedFormData.facilityData);
        break;
      case 3:
        setDefaultFormData(persistedFormData.activityDetails);
    }
  }, [persistedFormData, currentKey]);

  const formatDataForUpdate = (data) => {
    return {
      ...createdFieldPlan,
      healthFacilityNumber: parseInt(data.healthFacilitiesCount, 10),
      startDate: (new Date(data.fieldPlanDuration.startDate)).getTime(),
      endDate: (new Date(data.fieldPlanDuration.endDate)).getTime(),
      geographyDetails: {
        state: data.state.code,
        districts: data.districts.map((district) => district.code),
        blocks: data.blocks.map((block) => block.code),
      },
      activities: data.activities.map((activity) => ({
        code: activity.code,
        name: activity.name,
      })),
    }
  }

  const formatDataForCreate = (data) => {
    return {
      tenantId,
      healthFacilityNumber: parseInt(data.healthFacilitiesCount, 10),
      projectId: createdProject?.id,
      startDate: (new Date(data.fieldPlanDuration.startDate)).getTime(),
      endDate: (new Date(data.fieldPlanDuration.endDate)).getTime(),
      geographyDetails: {
        state: data.state.code,
        districts: data.districts.map((district) => district.code),
        blocks: data.blocks.map((block) => block.code),
      },
      activities: data.activities.map((activity) => ({
        code: activity.code,
        name: activity.name,
      })),
    }
  }

  const upsertFieldPlan = async (fieldPlanFormData) => {

    setBlockUI(true);
    let fieldPlanUpsertData;
    if (createdFieldPlan?.id) {
      fieldPlanUpsertData = {
        FieldPlans: [formatDataForUpdate(fieldPlanFormData)],
        isCascadingProjectDateUpdate: true,
        apiOperation: "UPDATE"
      };
    } else {
      fieldPlanUpsertData = {
        FieldPlans: [formatDataForCreate(fieldPlanFormData)],
        apiOperation: "CREATE"
      };
    }

    try {
      const fieldPlanResponse = await FieldPlanService.upsertFieldPlan(fieldPlanUpsertData);
      const upsertedFieldPlanResponse = fieldPlanResponse.FieldPlans?.[0];
      await invalidateFieldPlanData();
      history.replace({
        pathname: location.pathname,
        search: `fieldPlanId=${upsertedFieldPlanResponse.id}&key=${currentKey + 1}`,
      });
      setCurrentKey(prev => prev + 1);
      setToast({
        key: "success",
        label: createdFieldPlan?.id ? t("PM_TOAST_DRAFT_FIELD_PLAN_UPDATION_SUCCESS") : t("PM_TOAST_DRAFT_FIELD_PLAN_CREATION_SUCCESS"),
      })

    } catch (e) {
      console.error(`Error ${ createdFieldPlan?.id ? `updating` : `creating` } field plan`, e);
      setToast({
        key: "error",
        label: createdFieldPlan?.id ? t("PM_TOAST_DRAFT_FIELD_PLAN_UPDATION_ERROR") : t("PM_TOAST_DRAFT_FIELD_PLAN_CREATION_ERROR"),
      })

    } finally {
      setBlockUI(false);
    }

  };

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

  const saveActivityDetailsAndUpdateFieldPlan = async (activityData) => {

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
      setBlockUI(true);
      const schedulingFieldPlan = createdFieldPlan?.status === "DRAFT";

      try {
        await assignActivityUsers(activityData);

        if (schedulingFieldPlan) {
          const fieldPlanUpdateData = {
            FieldPlans: [{
              ...createdFieldPlan,
              status: "SCHEDULED"
            }],
            isCascadingProjectDateUpdate: true,
            apiOperation: "UPDATE"
          };
          await FieldPlanService.upsertFieldPlan(fieldPlanUpdateData);
          await invalidateFieldPlanData();
        }

        dispatch(
          populateResponsePage({
            response: {},
            message: schedulingFieldPlan ? t("PM_COMMON_FIELD_PLAN_CREATED") : t("PM_COMMON_FIELD_PLAN_UPDATED"),
            createdId: createdFieldPlan?.name,
            info: t("PM_COMMON_FIELD_PLAN_NAME"),
            secondaryRedirectionLabel: t("PM_LABEL_GO_TO_PROJECT"),
            onSecondaryRedirection: () => history.push(`/${window?.contextPath}/employee/pm/project/${createdProject.id}/field-plans`),
          })
        );
        history.push(`/${window?.contextPath}/employee/pm/response`);

      } catch (error) {
        console.error("Error submitting field plan creation form", error);
        setToast({
          key: "error",
          label: schedulingFieldPlan ? t("PM_TOAST_FIELD_PLAN_SUBMIT_CREATE_ERROR") : t("PM_TOAST_FIELD_PLAN_SUBMIT_UPDATE_ERROR"),
        })

      } finally {
        setBlockUI(false);
      }
    }
  }

  const handleFormSubmit = async (data) => {
    switch (currentKey) {
      case 1:
        const savedFieldPlanDetails = persistedFormData.fieldPlanDetails;
        if (CommonUtils.isNotEqual(savedFieldPlanDetails, data)) {
          setPersistedFormData((prev) => ({ ...prev, fieldPlanDetails: data }));
          await upsertFieldPlan(data);
        } else {
          setCurrentKey((prev) => prev + 1);
        }
        break;
      case 2:
        setPersistedFormData((prev) => ({ ...prev, facilityData: data }));
        setCurrentKey((prev) => prev + 1);
        break;
      case 3:
        await saveActivityDetailsAndUpdateFieldPlan(data.activityUserAssignment);
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
        return t("PM_CREATE_FIELD_PLAN_HEAD_FIELD_PLAN_DETAILS");
      case 3:
        return t("PM_CREATE_FIELD_PLAN_HEAD_ACTIVITY_DETAILS");
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
        setCurrentKey(key + 1);
        break;
      case 3:
        const savedActivityAssignments = getDefaultActivityAssignments();
        const currentActivityAssignments = getFormData("activityUserAssignment");
        if (CommonUtils.isNotEqual(savedActivityAssignments, currentActivityAssignments)) {
          setBackAlert({
            continueAction: () => {
              setPersistedFormData((prevState) => ({
                ...prevState,
                activityDetails: {
                  activityUserAssignment: savedActivityAssignments,
                },
              }));
              setCurrentKey(key + 1);
            }
          });
        } else {
          setCurrentKey(key + 1);
        }
    }
  };

  const handleBackNavigation = () => {
    switch (currentKey) {
      case 1:
        const savedFieldPlanDetails = {
          districts: persistedFormData?.fieldPlanDetails?.districts,
          blocks: persistedFormData?.fieldPlanDetails?.blocks,
          fieldPlanDuration: persistedFormData?.fieldPlanDetails?.fieldPlanDuration,
          healthFacilitiesCount: persistedFormData?.fieldPlanDetails?.healthFacilitiesCount,
          activities: persistedFormData?.fieldPlanDetails?.activities,
        };
        const currentFieldPlanDetails = {
          districts: getFormData("districts"),
          blocks: getFormData("blocks"),
          fieldPlanDuration: getFormData("fieldPlanDuration"),
          healthFacilitiesCount: getFormData("healthFacilitiesCount"),
          activities: getFormData("activities"),
        };
        if (CommonUtils.isNotEqual(savedFieldPlanDetails, currentFieldPlanDetails)) {
          setBackAlert({
            continueAction: () => {
              window.history.back();
            }
          });
        } else {
          window.history.back();
        }
        break;
      case 2:
        setCurrentKey((prev) => prev - 1);
        break;
      case 3:
        const savedActivityAssignments = getDefaultActivityAssignments();
        const currentActivityAssignments = getFormData("activityUserAssignment");
        if (CommonUtils.isNotEqual(savedActivityAssignments, currentActivityAssignments)) {
          setBackAlert({
            continueAction: () => {
              setPersistedFormData((prevState) => ({
                ...prevState,
                activityDetails: {
                  activityUserAssignment: savedActivityAssignments,
                },
              }));
              setCurrentKey((prev) => prev - 1);
            }
          });
        } else {
          setCurrentKey((prev) => prev - 1);
        }
    }
  };

  const getDefaultValues = () => {
    switch (currentKey) {
      case 1:
        return persistedFormData.fieldPlanDetails;
      case 3:
        return persistedFormData.activityDetails;
    }
  }

  if (projectDataLoading || fieldPlanDataLoading || activityAssignmentDataLoading) {
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
      <div style={{fontSize: "32px", fontWeight: "700", fontFamily: "Roboto Condensed", marginBottom: "20px", color: "#0B0C0C"}}>
        {createdFieldPlan?.name || t("PM_COMMON_NEW_FIELD_PLAN")}
      </div>
      <Stepper
        customSteps={[
          "PM_CREATE_FIELD_PLAN_HEAD_FIELD_PLAN_DETAILS",
          "PM_CREATE_FIELD_PLAN_HEAD_FACILITY_DATA",
          "PM_CREATE_FIELD_PLAN_HEAD_ACTIVITY_DETAILS",
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
        // submitIcon={<CustomArrowRight />}
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

export default CreateFieldPlan;