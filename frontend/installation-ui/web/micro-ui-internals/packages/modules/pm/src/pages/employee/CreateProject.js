import React, {useEffect, useMemo, useState} from "react";
import {FormComposerV2} from "@egovernments/digit-ui-react-components";
import {Stepper} from "@egovernments/digit-ui-components";
import useMDMS from "../../hooks/useMDMS";
import {useTranslation} from "react-i18next";
import useBoundary from "../../hooks/useBoundary";

const CreateProject = () => {

  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getStateId();
  const [currentKey, setCurrentKey] = useState(1);
  const [projectFormData, setProjectFormData] = useState({});
  const { key, projectId } = Digit.Hooks.useQueryParams();

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

  const config = useMemo(
    () => [
      {
        key: "1",
        head: "PM_CREATE_PROJECT_HEAD_PROJECT_DETAILS",
        subHead: "PM_CREATE_PROJECT_HEAD_PROJECT_DETAILS_DESC",
        body: [
          {
            inline: true,
            label: "PM_CREATE_PROJECT_LABEL_PROJECT_TYPE",
            isMandatory: true,
            key: "projectType",
            type: "dropdown",
            disable: false,
            route: "name",
            nextRoute: "justification-code",
            populators: {
              name: "projectType",
              error: "Required",
              optionsKey: "name",
              required: true,
              options: projectTypeData || []
            }
          },
          {
            inline: true,
            label: "PM_CREATE_PROJECT_LABEL_JUSTIFICATION_CODE",
            isMandatory: true,
            key: "justificationCode",
            type: "text",
            disable: false,
            route: "justification-code",
            nextRoute: "project-dates",
            populators: {
              name: "justificationCode",
              error: "Required",
            },
          },
          {
            inline: true,
            label: "PM_CREATE_PROJECT_LABEL_PROJECT_DATES",
            isMandatory: true,
            key: "projectDates",
            type: "component",
            component: "PMDateRange",
            disable: false,
            customProps: {
              name: "projectDates",
            },
            route: "project-dates",
            nextRoute: "",
            populators: {
              name: "projectDates",
              error: "Required"
            }
          }
        ]
      },
      {
        key: "2",
        head: "PM_CREATE_PROJECT_HEAD_GEOGRAPHY_DETAILS",
        subHead: "PM_CREATE_PROJECT_HEAD_GEOGRAPHY_DETAILS_DESC",
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
              t,
              boundaryData
            },
            disable: false,
            route: "state",
            nextRoute: "districts",
            populators: {
              name: "state",
              error: "Required"
            }
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
              t,
              boundaryData
            },
            disable: false,
            route: "districts",
            nextRoute: "blocks",
            populators: {
              name: "districts",
              error: "Required"
            }
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
              t,
              boundaryData
            },
            disable: false,
            route: "blocks",
            nextRoute: "",
            populators: {
              name: "blocks",
              error: "Required"
            }
          }
        ]
      },
      {
        key: "3",
        head: "PM_CREATE_PROJECT_HEAD_FACILITY_DATA",
        body: [
          {
            inline: true,
            label: "PM_CREATE_PROJECT_LABEL_PROJECT_DATES",
            isMandatory: true,
            key: "projectDates2",
            type: "component",
            component: "PMDateRange",
            disable: false,
            customProps: {
              name: "projectDates2",
            },
            route: "project-dates-2",
            nextRoute: "",
            populators: {
              name: "projectDates2",
              error: "Required",
            },
          }
        ]
      }
    ], [t, projectTypeData, boundaryData]
  )

  const filterConfig = (config, currentKey) => {
    return config.filter((step) => parseInt(step.key) === currentKey);
  };

  const [filteredConfig, setFilteredConfig] = useState(filterConfig(config, currentKey));

  useEffect(() => {
    setFilteredConfig(filterConfig(config, currentKey));
  }, [config, currentKey]);

  useEffect(() => {
    console.debug("projectFormData", projectFormData);
  }, [projectFormData])

  const upsertProject = (projectData) => {

  }

  const handleFormSubmit = (data) => {
    switch (currentKey) {
      case 1:
        setProjectFormData(prev => ({...prev, projectDetails: data}));
        setCurrentKey(prev => prev + 1);
        break;
      case 2:
        const newProjectFormData = {...projectFormData, geographyDetails: data};
        setProjectFormData(newProjectFormData);
        upsertProject(newProjectFormData)
        break;
    }

  }

  const handleFormValueChange = (setValue, formData) => {
    // if (currentKey === 2) {
    //   console.debug("handleFormValueChange", formData);
    // }
  }

  const getNextActionLabel = () => {
    if (currentKey === 1 || currentKey === 2) {
      return t("CORE_COMMON_NEXT");
    } else {
      return t("CORE_COMMON_SAVE");
    }
  };

  const onStepClick = (key) => {
    if (key >= currentKey) return;
    setCurrentKey(key + 1);
  }

  const getDefaultValues = () => {
    switch (currentKey) {
      case 1:
        return projectFormData.projectDetails;
      case 2:
        return projectFormData.geographyDetails;
    }
  }

  return (
    <div>
      <Stepper
        customSteps={[
          "PM_CREATE_PROJECT_HEAD_PROJECT_DETAILS",
          "PM_CREATE_PROJECT_HEAD_GEOGRAPHY_DETAILS",
          "PM_CREATE_PROJECT_HEAD_FACILITY_DATA",
        ]}
        onStepClick={onStepClick}
        currentStep={currentKey}
      />
      <FormComposerV2
        config={filteredConfig}
        onSubmit={handleFormSubmit}
        label={getNextActionLabel()}
        showSecondaryLabel={true}
        secondaryLabel={t("CORE_COMMON_BACK")}
        onFormValueChange={handleFormValueChange}
        appData={getDefaultValues()}
      />
    </div>
  )

}

export default CreateProject;