import React, { useEffect, useMemo, useState } from "react";
import useBoundary from "../../hooks/useBoundary";
import useMDMS from "../../hooks/useMDMS";
import useProject from "../../hooks/useProject";
import { useTranslation } from "react-i18next";
import { IngestionService } from "../../services/Ingestion";
import CustomArrowRight from "../../components/Custom/CustomArrowRight";
import CustomCloseSvg from "../../components/Custom/CustomCloseSvg";
import { Button, FormComposerV2, Loader, PopUp, Toast } from "@egovernments/digit-ui-react-components";
import {Stepper} from "@egovernments/digit-ui-components";

const CreateFieldPlan = () => {

  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getStateId();
  const [currentKey, setCurrentKey] = useState(1);
  const [persistedFormData, setPersistedFormData] = useState({});
  const [defaultFormData, setDefaultFormData] = useState({});
  const [createdProject, setCreatedProject] = useState(null);
  const { key, fieldPlanId } = Digit.Hooks.useQueryParams();
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [toast, setToast] = useState(null);
  const [blockUI, setBlockUI] = useState(null);
  const [invalidDataError, setInvalidDataError] = useState(null);
  const [getFormData, setGetFormData] = useState(null);
  const [showBackAlert, setShowBackAlert] = useState(false);
  const [boundaryData, setBoundaryData] = useState({});
  const url = window.location.href;
  const projectId = url.split("project/")[1].split("/")[0];

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

  const { data: projectData } = useProject({
    id: [projectId],
  });

  useEffect(() => {
    if (fieldPlanId && key) {
      setCurrentKey(parseInt(key));
    }
  }, []);

  useEffect(() => {
    const project = projectData?.projects?.[0];
    if (project) {
      setCreatedProject(project);
    }
  }, [projectData]);

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

  useEffect(() => {
    if (createdProject) {
      const formData = {
        fieldPlanDetails: {
          state: createdProject.additionalDetails.geographyDetails.state,
        }
      }

      setPersistedFormData(formData);
    }
  }, [createdProject])

  const handleFacilityDataUpload = async (file) => {
    setBlockUI(true);
    let uploadedFile;
    try {
      const response = await IngestionService.uploadTemplateFile(file, succeedFileUpload || false, errorCode || "INVALID_DATA");

      if (response.status === 200) {
        setToast({
          key: "success",
          label: `Successfully uploaded file`,
        });
        uploadedFile = {
          name: file.name,
          fileStoreId: "dummy_id",
        };
      }
    } catch (e) {
      console.error("Error uploading template", e);
      if (e.status === 400) {
        if (e.data.code === "INVALID_TEMPLATE") {
          setToast({
            key: "error",
            label: `The uploaded file does not match the required template structure. Please download and use the latest template.`,
          });
        } else if (e.data.code === "INVALID_DATA") {
          setInvalidDataError({
            label: `${e.data.invalidFacilitiesCount} ${t("PM_HEALTH_FACILITIES_VALIDATION_FAILED")}`,
          });
          uploadedFile = {
            name: file.name,
            fileStoreId: "dummy_id",
          };
        }
      } else {
        setToast({
          key: "error",
          label: `Error uploading file`,
        });
      }
    } finally {
      setBlockUI(false);
    }

    return uploadedFile;
  };

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
              disabledOptions: [],
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
              disabledOptions: [],
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
              t,
              activityTypeData: activityData
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
              allowedFileTypes: [".csv", ".xls", ".xlsx"],
              handleFileUpload: handleFacilityDataUpload,
              invalidDataError: invalidDataError,
              errorViewLabel: "CORE_COMMON_VIEW_ERRORS",
              heading: "PM_CREATE_FIELD_PLAN_HEAD_UPLOAD_FACILITY_DATA",
              description: "PM_CREATE_FIELD_PLAN_HEAD_UPLOAD_FACILITY_DATA_DESC",
              t,
              setToast,
              setBlockUI,
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
            inline: true,
            label: "PM_CREATE_FIELD_PLAN_LABEL_ACTIVITIES",
            isMandatory: true,
            key: "dummyActivities",
            type: "dropdown",
            disable: false,
            route: "dummyActivities",
            nextRoute: "",
            populators: {
              name: "dummyActivities",
              error: "Required",
              optionsKey: "name",
              required: true,
              options: activityData || [],
            },
          },
        ],
      },
    ],
    [t, activityData, boundaryData, invalidDataError]
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
    }
  }, [persistedFormData, currentKey]);

  const upsertFieldPlan = async (projectData) => {};

  const handleFormSubmit = async (data) => {
    console.debug("data", data);
    switch (currentKey) {
      case 1:
        setPersistedFormData((prev) => ({ ...prev, fieldPlanDetails: data }));
        setCurrentKey((prev) => prev + 1);
        break;
      case 2:
        const newFormData = { ...persistedFormData, facilityData: data };
        setPersistedFormData(newFormData);
        setCurrentKey((prev) => prev + 1);
        // await upsertFieldPlan(newFormData);
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
      return t("CORE_COMMON_SAVE");
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
    if (key >= currentKey) return;
    setCurrentKey(key + 1);
  };

  const handleBackNavigation = () => {
    switch (currentKey) {
      case 1:
        setShowBackAlert(true);
        break;
      case 2:
        setShowBackAlert(true);
        break;
      case 3:
        setShowBackAlert(true);
    }
  };

  const handleConfirmBackNavigation = () => {
    switch (currentKey) {
      case 1:
        setShowBackAlert(false);
        history.push(`/${window?.contextPath}/employee`);
        break;
      case 2:
        setShowBackAlert(false);
        setCurrentKey((prev) => prev - 1);
        break;
      case 3:
        setShowBackAlert(false);
        setCurrentKey((prev) => prev - 1);
    }
  };

  const getDefaultValues = () => {
    switch (currentKey) {
      case 1:
        return persistedFormData.fieldPlanDetails;
      case 2:
        return persistedFormData.facilityData;
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

export default CreateFieldPlan;