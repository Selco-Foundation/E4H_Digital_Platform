import React, { useCallback, useEffect, useMemo, useState } from "react";
import useBoundary from "../../hooks/useBoundary";
import useMDMS from "../../hooks/useMDMS";
import useProject from "../../hooks/useProject";
import { useTranslation } from "react-i18next";
import { FormComposerV2, Loader, Toast } from "@egovernments/digit-ui-react-components";
import { Stepper } from "@egovernments/digit-ui-components";
import { useDispatch } from "react-redux";
import { populateResponsePage, populateWorkingProject } from "../../redux/actions";
import { useHistory } from "react-router-dom";
import { useQueryClient } from "react-query";
import { PMService } from "../../services/PMService";
import useOrganization from "../../hooks/useOrganization";
import useOrganizationUser from "../../hooks/useOrganizationUser";
import UnsavedDataAlert from "../../components/UnsavedDataAlert";
import { AMCService } from "../../services/AMC";

const getCurrentStepFromURL = () => {
  const key = parseInt(new URLSearchParams(window.location.search).get("key"), 10);
  return [1, 2, 3].includes(key) ? key : 1;
};

const getUserIdentifier = (user = {}) => user.uuid || user.userId || user.id;

const getAuditDetails = (savedAuditDetails) => {
  const userUuid = Digit.UserService.getUser()?.info?.uuid;
  const now = Date.now();

  return savedAuditDetails ? {
    ...savedAuditDetails,
    lastModifiedBy: userUuid,
    lastModifiedTime: now,
  } : {
    createdBy: userUuid,
    createdTime: now,
    lastModifiedBy: userUuid,
    lastModifiedTime: now,
  };
};

const formatAMCGeographyDetailsForUpdate = (geographyDetails = {}) => ({
  state: geographyDetails?.state?.code || geographyDetails?.state,
  districts: geographyDetails?.districts?.map((district) => district?.code || district) || [],
  blocks: geographyDetails?.blocks?.map((block) => block?.code || block) || [],
});

const CreateAMC = () => {

  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getStateId();
  const [currentKey, setCurrentKey] = useState(getCurrentStepFromURL);
  const [persistedFormData, setPersistedFormData] = useState({});
  const [defaultFormData, setDefaultFormData] = useState({});
  const [createdProject, setCreatedProject] = useState(null);
  const [createdFieldPlan, setCreatedFieldPlan] = useState(null);
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [toast, setToast] = useState(null);
  const [blockUI, setBlockUI] = useState(null);
  const [file, setFile] = useState(null);
  const [uploadedValidFile, setUploadedValidFile] = useState(false);
  const [invalidDataError, setInvalidDataError] = useState(null);
  const [getFormData, setGetFormData] = useState(null);
  const [backAlert, setBackAlert] = useState(null);
  const [boundaryData, setBoundaryData] = useState(null);
  const [savedAMCConfiguration, setSavedAMCConfiguration] = useState(null);
  const [organizationIds, setOrganizationIds] = useState([""]);
  const history = useHistory();
  const url = window.location.href;
  const projectId = url.split("project/")[1].split("/")[0];
  const amcConfigurationId = new URLSearchParams(window.location.search).get("amcConfigurationId");
  const dispatch = useDispatch();
  const queryClient = useQueryClient();

  useEffect(() => {
    const searchParams = new URLSearchParams(window.location.search);
    if (searchParams.get("key") === currentKey.toString()) {
      return;
    }

    searchParams.set("key", currentKey.toString());
    history.replace({
      pathname: window.location.pathname,
      search: searchParams.toString(),
    });
  }, [currentKey, history]);

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

  const { data: organizationUserData } = useOrganizationUser({
    organizationIds,
  });

  useEffect(() => {
    if (organizationData?.organizations?.length && organizationIds.length === 1 && organizationIds[0] === "") {
      setOrganizationIds(organizationData.organizations.map((organization) => organization.id));
    }
  }, [organizationData, organizationIds]);

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

      setPersistedFormData((prev) => ({
        ...formData,
        geographyDetails: prev?.geographyDetails?.districts?.length ? prev.geographyDetails : formData.geographyDetails,
        activityDetails: prev?.activityDetails?.activityUserAssignment?.length ? prev.activityDetails : formData.activityDetails,
      }));
    }
  }, [createdProject, getDefaultActivityAssignments]);

  useEffect(() => {
    let ignore = false;

    const setSavedAMCGeographyDetails = async () => {
      if (!amcConfigurationId || !fetchedBoundaryData) {
        return;
      }

      try {
        const response = await AMCService.fetchAMCConfigurations({
          searchCriteria: {
            tenantId,
            ids: [amcConfigurationId],
          },
        });

        if (ignore) {
          return;
        }

        const savedConfiguration = response?.AmcConfigurations?.[0];
        setSavedAMCConfiguration(savedConfiguration);

        const assignmentOrganizationIds = savedConfiguration?.assignments
          ?.map((assignment) => assignment?.organization?.id || assignment?.organisation?.id || assignment?.organizationId || assignment?.organisationId)
          .filter(Boolean);
        if (assignmentOrganizationIds?.length) {
          setOrganizationIds([...new Set(assignmentOrganizationIds)]);
        }

        const savedGeographyDetails = savedConfiguration?.geographyDetails;
        if (!savedGeographyDetails) {
          return;
        }
        const parsedGeographyDetails = typeof savedGeographyDetails === "string" ? JSON.parse(savedGeographyDetails) : savedGeographyDetails;
        const stateCode = parsedGeographyDetails?.state?.code || parsedGeographyDetails?.state;
        const districtCodes = parsedGeographyDetails?.districts?.map((district) => district?.code || district) || [];
        const blockCodes = parsedGeographyDetails?.blocks?.map((block) => block?.code || block) || [];

        setPersistedFormData((prev) => ({
          ...prev,
          geographyDetails: {
            state: fetchedBoundaryData.states.find((state) => state.code === stateCode),
            districts: fetchedBoundaryData.districts.filter((district) => districtCodes.includes(district.code)),
            blocks: fetchedBoundaryData.blocks.filter((block) => blockCodes.includes(block.code)),
          },
        }));
      } catch (error) {
        if (!ignore) {
          console.error("Error fetching AMC configuration", error);
          setToast({
            label: t("CORE_COMMON_SOMETHING_WENT_WRONG"),
            key: "error"
          });
        }
      }
    };

    void setSavedAMCGeographyDetails();

    return () => {
      ignore = true;
    };
  }, [amcConfigurationId, fetchedBoundaryData, tenantId, t]);

  useEffect(() => {
    const assignments = savedAMCConfiguration?.assignments;
    if (!assignments?.length || !activityData || !organizationData || !organizationUserData) {
      return;
    }

    const amcActivity = activityData.find((activity) => activity.code?.toUpperCase() === "AMC");
    if (!amcActivity) {
      return;
    }

    const savedUsers = [...assignments]
      .sort((a, b) => (a.auditDetails?.createdTime || 0) - (b.auditDetails?.createdTime || 0))
      .map((assignment) => {
        const assignmentOrganization = assignment.organization || assignment.organisation || {};
        const assignedUserId = assignment.assignedUser || assignment.assignedTo || assignment.userId;
        const assignedUser = organizationUserData.organizationUsers?.find((user) => (
          user.uuid?.toString() === assignedUserId?.toString() ||
          user.userId?.toString() === assignedUserId?.toString() ||
          user.id?.toString() === assignedUserId?.toString()
        ));
        const assignmentOrganizationId = assignmentOrganization.id || assignment.organizationId || assignment.organisationId || assignedUser?.organizationId;
        const organizationFromMaster = organizationData.organizations?.find((org) => org.id === assignmentOrganizationId);
        const vendorFallback = savedAMCConfiguration?.vendor?.id === assignmentOrganizationId ? savedAMCConfiguration.vendor : null;
        const organization = organizationFromMaster || vendorFallback || assignmentOrganization;
        const organizationName = organization?.name || assignmentOrganizationId;

        return {
          id: assignment.id,
          savedAssignment: assignment,
          poNumber: { value: assignment.pocNumber || "", error: "", },
          organization: {
            value: assignmentOrganizationId ? { ...organization, id: organization.id || assignmentOrganizationId, name: organizationName } : null,
            error: "",
          },
          role: { value: assignment.role || null, error: "", },
          email: {
            value: assignedUser ? {
              ...assignedUser,
              emailKey: `${assignedUser.name || ""}${assignedUser.name ? " " : ""}[${assignedUser.emailId || ""}]`,
            } : null,
            error: "",
          },
          isEmailSent: assignment.isEmailSent || false,
        };
      });

    setPersistedFormData((prev) => ({
      ...prev,
      activityDetails: {
        activityUserAssignment: [
          {
            activity: amcActivity,
            users: savedUsers,
          },
        ],
      },
    }));
  }, [activityData, organizationData, organizationUserData, savedAMCConfiguration]);

  const handleFacilityDataDownload = useCallback(async () => {

    setBlockUI(true);
    try {
      await PMService.downloadAMCFacilityDataTemplate(createdProject.id, persistedFormData, t);
      setBlockUI(false);

      setToast({
        label: t("PM_TOAST_FACILITY_TEMPLATE_DOWNLOAD_SUCCESS"),
        key: "success",
      })

    } catch (error) {
      console.error("Error downloading project facility data template", error);
      setBlockUI(false);
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
      setBlockUI(false);

      if (response.errorCode === "INVALID_TEMPLATE") {
        setToast({
          key: "error",
          label: t("PM_TOAST_FACILITY_DATA_UPLOAD_TEMPLATE_ERROR")
        })
        setUploadedValidFile(false);
        setInvalidDataError(null);

      } else if (response.errorCode === "INVALID_DATA") {
        setInvalidDataError({
          label: `${response.errorCount} ${t("PM_HEALTH_FACILITIES_VALIDATION_FAILED")}`
        })
        setUploadedValidFile(false);
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
        setUploadedValidFile(true);
        await queryClient.invalidateQueries(["AMC_CONFIGURATION"]);
        uploadedFile = {
          name: response.file.name || chosenFile.name,
          data: response.file.data,
        }
      }

    } catch (e) {
      console.error("Error uploading template", e);
      setBlockUI(false);
      setUploadedValidFile(false);
      setToast({
        key: "error",
        label: t("PM_TOAST_FACILITY_DATA_UPLOAD_ERROR"),
      })

    } finally {
      setBlockUI(false);
    }

    setFile(uploadedFile);
  }, [createdProject, persistedFormData, queryClient, t]);

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

  const buildUpdateAMCConfigurationRequest = (formData) => {
    const geographyDetails = formatAMCGeographyDetailsForUpdate(formData.geographyDetails);
    const assignmentRows = formData?.activityDetails?.activityUserAssignment?.flatMap((activityAssignment) => (
      activityAssignment.users?.map((userEntry) => ({
        ...userEntry,
        activity: activityAssignment.activity,
      })) || []
    )) || [];
    const assignments = assignmentRows
      .filter((userEntry) => !userEntry.deleteAssignment && userEntry.email?.value)
      .map((userEntry) => {
        const savedAssignment = userEntry.savedAssignment || {};
        const selectedUser = userEntry.email.value;
        const selectedOrganization = userEntry.organization.value;
        const selectedRole = userEntry.role.value;
        const assignmentId = userEntry.id || savedAssignment.id;
        const assignedUser = getUserIdentifier(selectedUser);
        const auditDetails = getAuditDetails(savedAssignment.auditDetails);

        return {
          ...savedAssignment,
          id: assignmentId,
          tenantId: savedAssignment.tenantId || selectedUser.tenantId || savedAMCConfiguration.tenantId || tenantId,
          amcConfigurationId: savedAMCConfiguration.id,
          assignedUser: assignedUser?.toString(),
          assignedTo: assignedUser,
          assignedBy: Digit.UserService.getUser()?.info?.uuid,
          projectId,
          activityId: userEntry.activity?.code,
          activityCode: userEntry.activity?.code,
          pocNumber: userEntry.poNumber?.value || "",
          poNumber: userEntry.poNumber?.value || "",
          organizationId: selectedOrganization?.id,
          organizationName: selectedOrganization?.name,
          organization: selectedOrganization,
          role: selectedRole,
          roles: selectedRole ? [selectedRole] : [],
          user: selectedUser,
          userId: selectedUser.userId || selectedUser.id,
          uuid: selectedUser.uuid,
          userName: selectedUser.userName,
          name: selectedUser.name,
          mobileNumber: selectedUser.mobileNumber,
          emailId: selectedUser.emailId,
          isActive: true,
          auditDetails,
        };
      });

    return {
      AmcConfigurations: [
        {
          ...savedAMCConfiguration,
          tenantId: savedAMCConfiguration.tenantId || tenantId,
          vendorId: savedAMCConfiguration.vendorId || savedAMCConfiguration.vendor?.id,
          facilityId: savedAMCConfiguration.facilityId || savedAMCConfiguration.facility?.id,
          projectId: savedAMCConfiguration.projectId || projectId,
          durationMonths: 1,
          visitFrequencyMonths: 1,
          configurationEndDate: 1,
          assignments,
          geographyDetails,
          additionalDetails: {
            ...(savedAMCConfiguration.additionalDetails || {}),
            geographyDetails,
          },
          auditDetails: getAuditDetails(savedAMCConfiguration.auditDetails),
        },
      ],
    };
  };

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
        if (amcConfigurationId) {
          if (!savedAMCConfiguration?.id) {
            setToast({
              label: t("CORE_COMMON_SOMETHING_WENT_WRONG"),
              key: "error"
            });
            return;
          }

          try {
            setBlockUI(true);
            await AMCService.updateAMCConfigurations(buildUpdateAMCConfigurationRequest(persistedFormData));
            await queryClient.invalidateQueries(["AMC_CONFIGURATION"]);
            dispatch(
              populateResponsePage({
                response: {},
                message: t("PM_COMMON_AMC_UPDATED"),
                secondaryRedirectionLabel: t("PM_LABEL_GO_TO_PROJECT"),
                onSecondaryRedirection: () => history.push(`/${window?.contextPath}/employee/pm/project/${createdProject.id}/field-plans`),
              })
            );
            history.push(`/${window?.contextPath}/employee/pm/response`);
          } catch (error) {
            console.error("Error updating AMC configuration", error);
            setToast({
              label: t("CORE_COMMON_SOMETHING_WENT_WRONG"),
              key: "error"
            })
          } finally {
            setBlockUI(false);
          }
        } else if (!(file && uploadedValidFile)) {
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
        if (getFormData) {
          const currentActivityAssignments = getFormData("activityUserAssignment");
          setPersistedFormData((prevState) => ({
            ...prevState,
            activityDetails: {
              activityUserAssignment: currentActivityAssignments,
            },
          }));
        }
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
        if (getFormData) {
          const currentActivityAssignments = getFormData("activityUserAssignment");
          setPersistedFormData((prevState) => ({
            ...prevState,
            activityDetails: {
              activityUserAssignment: currentActivityAssignments,
            },
          }));
        }
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
