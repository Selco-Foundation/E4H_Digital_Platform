import React, { useEffect, useMemo, useState } from "react";
import { FormComposerV2, Loader, Toast } from "@egovernments/digit-ui-react-components";
import useBoundary from "../hooks/useBoundary";
import CommonUtils from "../utilities/CommonUtils";

const FacilityForm = ({ t, createdFacility = {}, onFormSubmit, wrapperStyle = {}, formToast, setFormToast }) => {

  const tenantId = Digit.ULBService.getCurrentTenantId();
  const [defaultValues, setDefaultValues] = useState({});
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [selectedFacilityCategory, setSelectedFacilityCategory] = useState({});
  const [selectedIsOperational, setSelectedIsOperational] = useState({});
  const yesChoice = { code: "YES", name: t("TL_COMMON_YES") };
  const noChoice = { code: "NO", name: t("TL_COMMON_NO") };

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  useEffect(() => {
    if (createdFacility?.id) {
      setDefaultValues(createdFacility?.isOperational?.code === "NO" ? { ...createdFacility, isOnmReady: noChoice } : { ...createdFacility });
      setSelectedIsOperational(createdFacility?.isOperational);
    } else {
      setDefaultValues({ ...createdFacility, isOperational: yesChoice });
      setSelectedIsOperational(yesChoice);
    }
  }, []);

  const { data: boundaryData, isLoading: boundaryLoading } = useBoundary();

  const { data: mdmsResponse, isLoading: mdmsLoading } = Digit.Hooks.useCustomMDMS(
    tenantId,
    "facility",
    [
      {
        name: "SolarSolutionDesignType",
      },
      {
        name: "FacilityType",
      },
      {
        name: "FacilityCategory",
      },
    ],
    {
      select: (data) => data,
      enabled: !!tenantId,
    }
  );

  const booleanChoiceMenu = [
    yesChoice,
    noChoice,
  ];

  const sortMenu = (options, field = "name") => {
    return options.sort((a, b) => a[field].localeCompare(b[field]));
  }

  const solarSolutionDesignTypes = sortMenu(mdmsResponse?.facility?.SolarSolutionDesignType || []);
  const facilityTypes = sortMenu(mdmsResponse?.facility?.FacilityType || []);
  const facilityCategories = sortMenu(mdmsResponse?.facility?.FacilityCategory || []);

  const isFormLoading = boundaryLoading || mdmsLoading;

  const addFacilityFormConfig = useMemo(
    () => [
      {
        key: "FACILITY_CREATE",
        body: [
          {
            inline: true,
            label: "CS_STATE",
            isMandatory: true,
            key: "state",
            type: "component",
            component: "FAStateSelector",
            customProps: {
              name: "state",
              t,
              boundaryData,
              disable: !!createdFacility?.id,
            },
            populators: {
              name: "state",
              error: t("CORE_COMMON_REQUIRED"),
            },
          },
          {
            inline: true,
            label: "CS_DISTRICT",
            isMandatory: true,
            key: "district",
            type: "component",
            component: "FADistrictSelector",
            customProps: {
              name: "district",
              stateIdentifier: "state",
              t,
              boundaryData,
              disable: !!createdFacility?.id,
            },
            populators: {
              name: "district",
              error: t("CORE_COMMON_REQUIRED"),
            },
          },
          {
            inline: true,
            label: "CS_BLOCK",
            isMandatory: true,
            key: "block",
            type: "component",
            component: "FABlockSelector",
            customProps: {
              name: "block",
              districtIdentifier: "district",
              t,
              boundaryData,
              disable: !!createdFacility?.id,
            },
            populators: {
              name: "block",
              error: t("CORE_COMMON_REQUIRED"),
            },
          },
          {
            inline: true,
            label: "FACILITY_NAME",
            isMandatory: true,
            key: "facilityName",
            type: "text",
            populators: {
              name: "facilityName",
              error: t("CORE_COMMON_REQUIRED"),
            },
          },
          {
            inline: true,
            label: "FACILITY_CATEGORY",
            isMandatory: true,
            disable: !!createdFacility?.id,
            key: "facilityCategory",
            type: "dropdown",
            populators: {
              name: "facilityCategory",
              error: t("CORE_COMMON_REQUIRED"),
              optionsKey: "name",
              required: true,
              options: facilityCategories,
            },
          },
          {
            inline: true,
            label: "FACILITY_TYPE",
            isMandatory: true,
            key: "facilityType",
            type: "dropdown",
            populators: {
              name: "facilityType",
              error: t("CORE_COMMON_REQUIRED"),
              optionsKey: "name",
              required: true,
              options: facilityTypes.filter((facilityType) => facilityType.facilityCategory === selectedFacilityCategory?.code),
            },
          },
          {
            inline: true,
            label: "FACILITY_SOLAR_SOLUTION_DESIGN_TYPE",
            isMandatory: true,
            key: "solarSolutionDesignType",
            type: "dropdown",
            populators: {
              name: "solarSolutionDesignType",
              error: t("CORE_COMMON_REQUIRED"),
              optionsKey: "name",
              required: true,
              options: solarSolutionDesignTypes,
            },
          },
          {
            inline: true,
            label: "FACILITY_POC_NAME",
            isMandatory: true,
            key: "facilityPocName",
            type: "text",
            populators: {
              name: "facilityPocName",
              error: t("CORE_COMMON_REQUIRED"),
              validation: { pattern: { value: /^[^"$<>?\\~`!@#%^()+={}\[\]*,:;“”‘’]*$/, message: t("FACILITY_POC_NAME_VALIDATION_ERROR") } },
            },
          },
          ...(selectedFacilityCategory?.code && selectedFacilityCategory?.code !== "HEALTH" ? [
            {
              inline: true,
              label: "FACILITY_POC_USERNAME",
              isMandatory: true,
              disable: !!createdFacility?.id,
              key: "facilityPocUsername",
              type: "text",
              populators: {
                name: "facilityPocUsername",
                error: t("CORE_COMMON_REQUIRED"),
                validation: { pattern: { value: /^\S*$/, message: t("FACILITY_POC_USERNAME_VALIDATION_ERROR") } },
              },
            },
          ] : []),
          {
            inline: true,
            label: "FACILITY_POC_PHONE",
            isMandatory: true,
            key: "facilityPocPhone",
            type: "text",
            populators: {
              name: "facilityPocPhone",
              error: t("CORE_COMMON_REQUIRED"),
              validation: { minlength: 10, maxlength: 10, pattern: { value: /^[0-9]\d{9}$/, message: t("FACILITY_POC_PHONE_VALIDATION_ERROR") } },
            },
          },
          {
            inline: true,
            label: "FACILITY_POC_EMAIL",
            isMandatory: false,
            key: "facilityPocEmail",
            type: "text",
            populators: {
              name: "facilityPocEmail",
              error: t("CORE_COMMON_REQUIRED"),
              validation: { pattern: { value: Digit.Utils.getPattern("Email"), message: t("CS_PROFILE_EMAIL_ERRORMSG") } },
            },
          },
          ...(selectedFacilityCategory?.code && selectedFacilityCategory?.code === "HEALTH" ? [
            {
              inline: true,
              label: "FACILITY_HFR_ID",
              isMandatory: false,
              disable: !!createdFacility?.id,
              key: "hfrId",
              type: "text",
              populators: {
                name: "hfrId",
                validation: { pattern: { value: /^\S*$/, message: t("FACILITY_HFR_ID_VALIDATION_ERROR") } },
              },
            },
            {
              inline: true,
              label: "FACILITY_NIN_ID",
              isMandatory: false,
              disable: !!createdFacility?.id,
              key: "ninId",
              type: "text",
              populators: {
                name: "ninId",
                validation: { pattern: { value: /^\S*$/, message: t("FACILITY_NIN_ID_VALIDATION_ERROR") } },
              },
            },
          ] : []),
          {
            inline: true,
            label: "FACILITY_IS_OPERATIONAL",
            isMandatory: false,
            disable: !createdFacility?.id,
            key: "isOperational",
            type: "dropdown",
            populators: {
              name: "isOperational",
              options: booleanChoiceMenu,
              optionsKey: "name",
              required: true,
              error: t("CORE_COMMON_REQUIRED"),
            },
          },
          {
            inline: true,
            label: "FACILITY_IS_ONM_READY",
            isMandatory: false,
            disable: selectedIsOperational?.code === "NO",
            key: "isOnmReady",
            type: "dropdown",
            populators: {
              name: "isOnmReady",
              options: booleanChoiceMenu,
              optionsKey: "name",
              required: true,
              error: t("CORE_COMMON_REQUIRED"),
            },
          },
          {
            inline: true,
            label: "FACILITY_LATITUDE",
            isMandatory: false,
            key: "latitude",
            type: "text",
            populators: {
              name: "latitude",
              validation: {
                min: -90,
                max: 90,
                pattern: {
                  value: /^-?(90(\.0+)?|[1-8]?\d(\.\d+)?)$/,
                  message: "Enter a valid latitude (-90 to 90)",
                },
              },
            },
          },
          {
            inline: true,
            label: "FACILITY_LONGITUDE",
            isMandatory: false,
            key: "longitude",
            type: "text",
            populators: {
              name: "longitude",
              validation: {
                min: -180,
                max: 180,
                pattern: {
                  value: /^-?(180(\.0+)?|1[0-7]\d(\.\d+)?|\d{1,2}(\.\d+)?)$/,
                  message: "Enter a valid longitude (-180 to 180)",
                },
              },
            },
          },
        ],
      },
    ],
    [t, mdmsResponse, createdFacility, boundaryData, selectedFacilityCategory, selectedIsOperational]
  );

  const handleFormSubmit = (formData) => {
    const updatedFormData = formData?.isOperational?.code === "NO" ? { ...formData, isOnmReady: noChoice } : formData;
    const hasHfrId = updatedFormData?.hfrId && `${updatedFormData.hfrId}`.trim().length > 0;
    const hasNinId = updatedFormData?.ninId && `${updatedFormData.ninId}`.trim().length > 0;
    if (updatedFormData?.facilityCategory?.code !== "HEALTH" || hasHfrId || hasNinId) {
      onFormSubmit(updatedFormData);
    } else {
      setFormToast({
        key: "error",
        label: t("FACILITY_HFR_OR_NIN_REQUIRED"),
      });
    }
  };

  const handleFormChange = (setValue, formData) => {
    if (formData?.isOperational?.code === "NO" && formData?.isOnmReady?.code !== "NO") {
      setValue("isOnmReady", noChoice);
      setDefaultValues({
        ...formData,
        isOnmReady: noChoice,
      });
      setSelectedIsOperational(formData?.isOperational);
    } else if (CommonUtils.isNotEqual(formData?.isOperational, selectedIsOperational)) {
      setSelectedIsOperational(formData?.isOperational);
    }

    if (CommonUtils.isNotEqual(formData?.facilityCategory, selectedFacilityCategory)) {
      setSelectedFacilityCategory(formData?.facilityCategory);
      if (formData?.facilityType?.facilityCategory !== formData?.facilityCategory?.code) {
        setDefaultValues({
          ...formData,
          facilityType: undefined,
        })
      }
    }
  }

  if (isFormLoading) {
    return (
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          minHeight: "200px",
        }}
      >
        <Loader />
      </div>
    );
  }

  return (
    <div
      style={{
        position: "relative",
        paddingBottom: "30px",
        maxHeight: "70vh",
        overflow: "auto",
        ...wrapperStyle,
      }}
    >
      <FormComposerV2
        key={JSON.stringify(defaultValues)}
        defaultValues={defaultValues}
        config={addFacilityFormConfig}
        onSubmit={handleFormSubmit}
        onFormValueChange={handleFormChange}
        label={t("CORE_COMMON_SAVE")}
        heading={""}
        cardStyle={{ boxShadow: "none" }}
        submitInForm={false}
        actionClassName={"reverse-actionbar-fixed"}
      />
      {formToast && (
        <Toast
          error={formToast.key === "error"}
          warning={formToast.key === "warning"}
          style={{
            zIndex: 100000000,
            ...(formToast.key === "error" ? { backgroundColor: "#B91900" } : {}),
            ...(mobileView ? { bottom: "120px" } : {}),
          }}
          label={formToast.label}
          isDleteBtn={true}
          onClose={() => setFormToast(null)}
        />
      )}
    </div>
  );
};

export default FacilityForm;
