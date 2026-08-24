import React, { useCallback, useEffect, useMemo, useState } from "react";
import useBoundary from "../../hooks/useBoundary";
import useMDMS from "../../hooks/useMDMS";
import useProject from "../../hooks/useProject";
import { useTranslation } from "react-i18next";
import { FormComposerV2, Loader, Toast } from "@egovernments/digit-ui-react-components";
import { Stepper } from "@egovernments/digit-ui-components";
import { useDispatch } from "react-redux";
import { populateResponsePage, populateWorkingAssessmentPlan, populateWorkingProject } from "../../redux/actions";
import { useHistory } from "react-router-dom";
import useAssessmentPlan from "../../hooks/useAssessmentPlan";
import { AssessmentPlanService } from "../../services/AssessmentPlan";
import { PMService } from "../../services/PMService";
import useOrganization from "../../hooks/useOrganization";
import useOrganizationUser from "../../hooks/useOrganizationUser";
import CommonUtils from "../../utilities/CommonUtils";
import UnsavedDataAlert from "../../components/UnsavedDataAlert";

const CreateAssessment = () => {

  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getStateId();
  const [currentKey, setCurrentKey] = useState(1);
  const [persistedFormData, setPersistedFormData] = useState({});
  const [defaultFormData, setDefaultFormData] = useState({});
  const [createdProject, setCreatedProject] = useState(null);
  const [createdAssessmentPlan, setCreatedAssessmentPlan] = useState(null);
  const { key, assessmentId } = Digit.Hooks.useQueryParams();
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [toast, setToast] = useState(null);
  const [blockUI, setBlockUI] = useState(null);
  const [file, setFile] = useState(null);
  const [invalidDataError, setInvalidDataError] = useState(null);
  const [getFormData, setGetFormData] = useState(null);
  const [backAlert, setBackAlert] = useState(null);
  const [boundaryData, setBoundaryData] = useState(null);
  const [hasSavedFacilityUpload, setHasSavedFacilityUpload] = useState(false);
  const [facilityUploadStatusLoading, setFacilityUploadStatusLoading] = useState(false);
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
    isLoading: assessmentPlanDataLoading,
    data: assessmentPlanData,
    revalidate: invalidateAssessmentPlanData
  } = useAssessmentPlan({
    id: [assessmentId],
  });

  const { data: organizationData } = useOrganization();

  const { data: organizationUserData } = useOrganizationUser({
    organizationIds,
  });

  useEffect(() => {
    if (createdAssessmentPlan?.id && key) {
      setCurrentKey(parseInt(key));
    }
  }, [createdAssessmentPlan?.id]);

  useEffect(() => {
    const project = projectData?.projects?.[0];
    if (project) {
      dispatch(populateWorkingProject(project));
      setCreatedProject(project);
    }
  }, [projectData]);

  useEffect(() => {
    const assessmentPlan = assessmentPlanData?.assessmentPlans?.[0];
    if (assessmentPlan) {
      dispatch(populateWorkingAssessmentPlan(assessmentPlan));
      setCreatedAssessmentPlan(assessmentPlan);
    }
  }, [assessmentPlanData]);

  useEffect(() => {
    if (organizationData) {
      setOrganizationIds(organizationData.organizations.map((organization) => organization.id));
    }
  }, [organizationData]);

  useEffect(() => {
    if (createdAssessmentPlan?.id) {
      history.replace({
        pathname: location.pathname,
        search: `assessmentId=${createdAssessmentPlan.id}&key=${currentKey}`,
      });
    }

  }, [createdAssessmentPlan?.id, currentKey])

  useEffect(() => {
    const selectedAssessmentPlanId = createdAssessmentPlan?.id || assessmentId;

    if (!selectedAssessmentPlanId) {
      setHasSavedFacilityUpload(false);
      return;
    }

    let isCurrentRequest = true;
    const fetchFacilityUploadStatus = async () => {
      setFacilityUploadStatusLoading(true);

      try {
        const hasUpload = await AssessmentPlanService.hasUploadedAssessmentFacilityData(selectedAssessmentPlanId);

        if (isCurrentRequest) {
          setHasSavedFacilityUpload(Boolean(hasUpload));
        }
      } catch (error) {
        console.error("Error fetching assessment plan facility upload status", error);

        if (isCurrentRequest) {
          setHasSavedFacilityUpload(false);
        }
      } finally {
        if (isCurrentRequest) {
          setFacilityUploadStatusLoading(false);
        }
      }
    };

    fetchFacilityUploadStatus();

    return () => {
      isCurrentRequest = false;
    };
  }, [createdAssessmentPlan?.id, assessmentId]);

  useEffect(() => {
    if (currentKey === 2 && !file && persistedFormData?.facilityData?.uploadFacilityData) {
      setFile(persistedFormData.facilityData.uploadFacilityData);
    }
  }, [currentKey, file, persistedFormData?.facilityData?.uploadFacilityData]);

  const closeToast = useCallback(() => {
    setToast(null);
  }, []);

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

  const formatDate = (timestamp) => {
    const date = new Date(timestamp);
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const year = date.getFullYear();
    return `${year}-${month}-${day}`;
  };

  const getDefaultActivityAssignments = useCallback(() => {
    const assignableActivities = activityData?.filter((activity) => activity?.code === "ASSESSMENT");

    return assignableActivities
      ? assignableActivities
        .map((activity) => ({
          activity: activity,
          users: [
            ...(
              createdAssessmentPlan?.assessors?.length
                ? createdAssessmentPlan.assessors.map((assessor) => {
                  const assignedUser = organizationUserData?.organizationUsers?.filter((user) => user.userId === assessor.userId)?.[0];

                  return {
                    organization: {
                      value: organizationData?.organizations?.filter((organization) => organization?.id === assignedUser?.organizationId)?.[0],
                      error: "",
                    },
                    role: {
                      value: activity.roles?.filter((role) => role?.code === assessor.role)?.[0],
                      error: "",
                    },
                    email: {
                      value: assignedUser,
                      error: "",
                    },
                    isEmailSent: false,
                  };
                })
                : [
                  {
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
  }, [activityData, createdAssessmentPlan, organizationData, organizationUserData])

  useEffect(() => {
    if (createdAssessmentPlan?.id && boundaryData) {
      const formData = {
        assessmentDetails: {
          assessmentName: createdAssessmentPlan.name,
          state: boundaryData.states
            .filter((state) => state.code === createdAssessmentPlan.geographyDetails?.state)
            .map((state) => ({
              code: state?.code,
              name: `Boundary_${state?.code}`,
            }))
            ?.[0],

          districts: boundaryData.districts.filter((district) => createdAssessmentPlan.geographyDetails?.districts?.includes(district.code)),
          blocks: boundaryData.blocks.filter((block) => createdAssessmentPlan.geographyDetails?.blocks?.includes(block.code)),
          assessmentDuration: {
            startDate: formatDate(createdAssessmentPlan.startDate),
            endDate: formatDate(createdAssessmentPlan.endDate),
          },
        },
        activityDetails: {
          activityUserAssignment: getDefaultActivityAssignments(),
        }
      }

      setPersistedFormData(formData);
    } else if (createdProject) {
      const formData = {
        assessmentDetails: {
          state: createdProject.additionalDetails.geographyDetails.state,
        }
      }

      setPersistedFormData(formData);
    }
  }, [createdProject, createdAssessmentPlan, boundaryData, getDefaultActivityAssignments]);

  const handleFacilityDataDownload = async () => {

    setBlockUI(true);
    try {
      const geographyDetails = {
        state: boundaryData.states
          .filter((state) => state.code === createdAssessmentPlan.geographyDetails.state)
          .map((state) => ({
            code: state?.code,
          }))
          ?.[0],

        districts: boundaryData.districts.filter((district) => createdAssessmentPlan.geographyDetails.districts.includes(district.code)),
        blocks: boundaryData.blocks.filter((block) => createdAssessmentPlan.geographyDetails.blocks.includes(block.code)),
      }
      await PMService.downloadAssessmentPlanFacilityDataTemplate(createdProject.id, createdAssessmentPlan.id, geographyDetails, t);

      setBlockUI(false);
      setToast({
        label: t("PM_TOAST_FACILITY_TEMPLATE_DOWNLOAD_SUCCESS"),
        key: "success",
      })

    } catch (error) {
      console.error("Error downloading assessment plan facility data template", error);
      setBlockUI(false);
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
      const response = await PMService.uploadAssessmentPlanFacilityDataTemplate(chosenFile, createdProject.id, createdAssessmentPlan.id);
      setBlockUI(false);

      if (response.errorCode === "INVALID_TEMPLATE") {
        setToast({
          key: "error",
          label: response.apiErrorMessage || t("PM_TOAST_FACILITY_DATA_UPLOAD_TEMPLATE_ERROR"),
          translate: false,
        })
        setInvalidDataError(null);

      } else if (response.errorCode === "INVALID_DATA") {
        setInvalidDataError({
          label: `${response.errorCount} ${t("PM_HEALTH_FACILITIES_VALIDATION_FAILED")}`
        })
        uploadedFile = {
          name: response.file.name || chosenFile.name,
          data: response.file.data,
          originalData: chosenFile,
          errorCodes: ["INVALID_DATA"]
        }

      } else {
        setToast({
          key: "success",
          label: t("PM_TOAST_FACILITY_DATA_UPLOAD_SUCCESS"),
        })
        setInvalidDataError(null);
        setHasSavedFacilityUpload(true);
        AssessmentPlanService.markAssessmentFacilityDataUploaded(createdAssessmentPlan.id);
        uploadedFile = {
          name: response.file.name || chosenFile.name,
          data: response.file.data,
          originalData: chosenFile,
        }
      }

    } catch (e) {
      console.error("Error uploading template", e);
      setBlockUI(false);
      setToast({
        key: "error",
        label: t("PM_TOAST_FACILITY_DATA_UPLOAD_ERROR"),
      })

    } finally {
      setBlockUI(false);
    }

    setFile(uploadedFile);
  };

  const validateActivityData = (activityData) => {
    let faultyData = false;
    let emptyData = true;

    const validatedData = activityData.map((dataEntry) => ({
      ...dataEntry,
      users: dataEntry.users.map((userEntry) => {
        const newUserEntry = {}

        if (userEntry.deleteAssignment) {
          return userEntry;
        }

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

  const formatAssessorsForUpdate = (activityData) => {
    const assessors = [];

    activityData.forEach((dataEntry) => {
      dataEntry.users.forEach((userEntry) => {

        if (userEntry.deleteAssignment) {
          return;
        }

        if (Object.keys(userEntry).every((key) => (["id", "isEmailSent", "deleteAssignment", "savedAssignment"].includes(key) || !userEntry[key].value))) {
          return;
        }

        assessors.push({
          role: userEntry.role.value?.code,
          userId: userEntry.email.value?.userId,
        });
      })
    });

    return assessors;
  }

  const assignAssessmentActivityUsers = async (activityData, planOverrides = {}) => {
    const assessors = formatAssessorsForUpdate(activityData);

    const assessmentPlanUpdateData = {
      AssessmentPlans: [{ ...createdAssessmentPlan, ...planOverrides }],
      assessors,
      apiOperation: "UPDATE",
    };

    const [updatedAssessmentPlan] = await AssessmentPlanService.upsertAssessmentPlan(assessmentPlanUpdateData);
    return updatedAssessmentPlan;
  }

  const config = useMemo(
    () => [
      {
        key: "1",
        body: [
          {
            inline: true,
            label: "PM_CREATE_ASSESSMENT_LABEL_ASSESSMENT_NAME",
            isMandatory: true,
            key: "assessmentName",
            type: "text",
            disable: false,
            route: "assessment-name",
            nextRoute: "state",
            populators: {
              name: "assessmentName",
              error: t("CORE_COMMON_REQUIRED"),
            },
          },
          {
            inline: true,
            label: "PM_CREATE_ASSESSMENT_LABEL_STATE",
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
            label: "PM_CREATE_ASSESSMENT_LABEL_DISTRICTS",
            isMandatory: true,
            key: "districts",
            type: "component",
            component: "PMDistrictSelector",
            customProps: {
              name: "districts",
              stateIdentifier: "state",
              selectedOptions: (createdAssessmentPlan?.id && createdAssessmentPlan?.status !== "DRAFT") ? boundaryData?.districts?.filter((district) => createdAssessmentPlan.geographyDetails?.districts?.includes(district?.code)) : [],
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
            label: "PM_CREATE_ASSESSMENT_LABEL_BLOCKS",
            isMandatory: true,
            key: "blocks",
            type: "component",
            component: "PMBlockSelector",
            customProps: {
              name: "blocks",
              districtsIdentifier: "districts",
              selectedOptions: (createdAssessmentPlan?.id && createdAssessmentPlan?.status !== "DRAFT") ? boundaryData?.blocks?.filter((block) => createdAssessmentPlan.geographyDetails?.blocks?.includes(block?.code)) : [],
              t,
              boundaryData,
            },
            disable: false,
            route: "blocks",
            nextRoute: "assessment-duration",
            populators: {
              name: "blocks",
              error: "Required",
            },
          },
          {
            inline: true,
            label: "PM_CREATE_ASSESSMENT_LABEL_ASSESSMENT_DATES",
            isMandatory: true,
            key: "assessmentDuration",
            type: "component",
            component: "PMDateRange",
            disable: false,
            customProps: {
              name: "assessmentDuration",
              minimumStartDate: createdProject?.startDate ? formatDate(createdProject.startDate) : "",
              maximumStartDate: createdProject?.endDate ? formatDate(createdProject.endDate) : "",
              minimumEndDate: createdProject?.startDate ? formatDate(createdProject.startDate) : "",
              maximumEndDate: createdProject?.endDate ? formatDate(createdProject.endDate) : "",
            },
            route: "assessment-duration",
            nextRoute: "",
            populators: {
              name: "assessmentDuration",
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
              heading: "PM_CREATE_ASSESSMENT_HEAD_DOWNLOAD_FACILITY_TEMPLATE",
              description: "PM_CREATE_ASSESSMENT_HEAD_DOWNLOAD_FACILITY_TEMPLATE_DESC",
              handleDownload: handleFacilityDataDownload,
              t,
            },
            route: "facility-data-download",
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
              heading: "PM_CREATE_ASSESSMENT_HEAD_UPLOAD_FACILITY_DATA",
              description: "PM_CREATE_ASSESSMENT_HEAD_UPLOAD_FACILITY_DATA_DESC",
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
            component: "PMAssessmentActivityDetails",
            disable: false,
            route: "activity-details",
            customProps: {
              name: "activityUserAssignment",
              t,
              activityData: activityData?.filter((activity) => activity?.code === "ASSESSMENT"),
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
    [t, activityData, boundaryData, createdProject, createdAssessmentPlan, organizationData, file, invalidDataError]
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
        setDefaultFormData(persistedFormData.assessmentDetails);
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
      ...createdAssessmentPlan,
      name: data.assessmentName,
      startDate: (new Date(data.assessmentDuration.startDate)).getTime(),
      endDate: (new Date(data.assessmentDuration.endDate)).getTime(),
      geographyDetails: {
        state: data.state.code,
        districts: data.districts.map((district) => district.code),
        blocks: data.blocks.map((block) => block.code),
      },
    }
  }

  const formatDataForCreate = (data) => {
    return {
      tenantId,
      name: data.assessmentName,
      projectId: createdProject?.id,
      startDate: (new Date(data.assessmentDuration.startDate)).getTime(),
      endDate: (new Date(data.assessmentDuration.endDate)).getTime(),
      geographyDetails: {
        state: data.state.code,
        districts: data.districts.map((district) => district.code),
        blocks: data.blocks.map((block) => block.code),
      },
    }
  }

  const upsertAssessmentPlan = async (assessmentFormData) => {

    setBlockUI(true);
    let assessmentPlanUpsertData;
    if (createdAssessmentPlan?.id) {
      assessmentPlanUpsertData = {
        AssessmentPlans: [formatDataForUpdate(assessmentFormData)],
        apiOperation: "UPDATE"
      };
    } else {
      assessmentPlanUpsertData = {
        AssessmentPlans: [formatDataForCreate(assessmentFormData)],
        apiOperation: "CREATE"
      };
    }

    try {
      const upsertedAssessmentPlanResponse = await AssessmentPlanService.upsertAssessmentPlan(assessmentPlanUpsertData);
      const upsertedAssessmentPlan = upsertedAssessmentPlanResponse?.[0];
      await invalidateAssessmentPlanData();
      history.replace({
        pathname: location.pathname,
        search: `assessmentId=${upsertedAssessmentPlan.id}&key=${currentKey + 1}`,
      });
      setCurrentKey(prev => prev + 1);
      setBlockUI(false);
      setToast({
        key: "success",
        label: createdAssessmentPlan?.id ? t("PM_TOAST_DRAFT_ASSESSMENT_PLAN_UPDATION_SUCCESS") : t("PM_TOAST_DRAFT_ASSESSMENT_PLAN_CREATION_SUCCESS"),
      })

    } catch (e) {
      console.error(`Error ${ createdAssessmentPlan?.id ? `updating` : `creating` } assessment plan`, e);
      setBlockUI(false);
      setToast({
        key: "error",
        label: CommonUtils.getApiErrorMessage(e) || (createdAssessmentPlan?.id ? t("PM_TOAST_DRAFT_ASSESSMENT_PLAN_UPDATION_ERROR") : t("PM_TOAST_DRAFT_ASSESSMENT_PLAN_CREATION_ERROR")),
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

  const saveActivityDetailsAndUpdateAssessmentPlan = async (activityData) => {

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
      const creatingAssessmentPlan = createdAssessmentPlan?.status === "DRAFT";

      try {
        await assignAssessmentActivityUsers(activityData, creatingAssessmentPlan ? { status: "ACTIVE" } : {});

        const upsertedAssessmentPlanData = await invalidateAssessmentPlanData();
        const upsertedAssessmentPlan = upsertedAssessmentPlanData?.assessmentPlans?.[0];
        dispatch(
          populateResponsePage({
            response: {},
            message: creatingAssessmentPlan ? t("PM_COMMON_ASSESSMENT_PLAN_CREATED") : t("PM_COMMON_ASSESSMENT_PLAN_UPDATED"),
            createdId: upsertedAssessmentPlan?.name,
            info: t("PM_COMMON_ASSESSMENT_PLAN_NAME"),
            secondaryRedirectionLabel: t("PM_LABEL_GO_TO_PROJECT"),
            onSecondaryRedirection: () => history.push(`/${window?.contextPath}/employee/pm/project/${createdProject.id}/field-plans`),
          })
        );
        history.push(`/${window?.contextPath}/employee/pm/response`);

      } catch (error) {
        console.error("Error submitting assessment plan creation form", error);
        setToast({
          key: "error",
          label: CommonUtils.getApiErrorMessage(error) || (creatingAssessmentPlan ? t("PM_TOAST_ASSESSMENT_PLAN_SUBMIT_CREATE_ERROR") : t("PM_TOAST_ASSESSMENT_PLAN_SUBMIT_UPDATE_ERROR")),
        })

      } finally {
        setBlockUI(false);
      }
    }
  }

  const handleFormSubmit = async (data) => {
    switch (currentKey) {
      case 1:
        const savedAssessmentDetails = persistedFormData.assessmentDetails;
        if (CommonUtils.isNotEqual(savedAssessmentDetails, data)) {
          setPersistedFormData((prev) => ({ ...prev, assessmentDetails: data }));
          await upsertAssessmentPlan(data);
        } else {
          setCurrentKey((prev) => prev + 1);
        }
        break;
      case 2:
        setPersistedFormData((prev) => ({ ...prev, facilityData: data }));
        setCurrentKey((prev) => prev + 1);
        break;
      case 3:
        await saveActivityDetailsAndUpdateAssessmentPlan(data.activityUserAssignment);
        break;
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
        return t("PM_CREATE_ASSESSMENT_HEAD_ASSESSMENT_DETAILS");
      case 3:
        return t("PM_CREATE_ASSESSMENT_HEAD_ACTIVITY_DETAILS");
    }
  };

  const getDescription = () => {
    switch (currentKey) {
      case 1:
        return t("PM_CREATE_ASSESSMENT_HEAD_ASSESSMENT_DETAILS_DESC");
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
        const savedAssessmentDetails = {
          districts: persistedFormData?.assessmentDetails?.districts,
          blocks: persistedFormData?.assessmentDetails?.blocks,
          assessmentDuration: persistedFormData?.assessmentDetails?.assessmentDuration,
          assessmentName: persistedFormData?.assessmentDetails?.assessmentName,
        };
        const currentAssessmentDetails = {
          districts: getFormData("districts"),
          blocks: getFormData("blocks"),
          assessmentDuration: getFormData("assessmentDuration"),
          assessmentName: getFormData("assessmentName"),
        };
        if (CommonUtils.isNotEqual(savedAssessmentDetails, currentAssessmentDetails)) {
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
        return persistedFormData.assessmentDetails;
      case 2:
        return persistedFormData.facilityData;
      case 3:
        return persistedFormData.activityDetails;
    }
  }

  if (projectDataLoading || assessmentPlanDataLoading || facilityUploadStatusLoading) {
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
        {createdAssessmentPlan?.name || t("PM_COMMON_NEW_ASSESSMENT_PLAN")}
      </div>
      <Stepper
        customSteps={[
          "PM_CREATE_ASSESSMENT_HEAD_ASSESSMENT_DETAILS",
          "PM_CREATE_ASSESSMENT_HEAD_FACILITY_DATA",
          "PM_CREATE_ASSESSMENT_HEAD_ACTIVITY_DETAILS",
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
          label={toast.translate === false ? toast.label : t(toast.label)}
          isDleteBtn={true}
          onClose={closeToast}
        />
      )}
      {backAlert && <UnsavedDataAlert t={t} alert={backAlert} setAlert={setBackAlert} />}
    </div>
  )
}

export default CreateAssessment;
