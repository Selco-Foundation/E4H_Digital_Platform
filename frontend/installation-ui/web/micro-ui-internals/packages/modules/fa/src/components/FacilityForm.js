import React, { useEffect, useMemo, useState } from "react";
import { FormComposerV2, Loader } from "@egovernments/digit-ui-react-components";
import useBoundary from "../hooks/useBoundary";

const FacilityForm = ({ t, createdFacility = {}, onFormSubmit, wrapperStyle = {} }) => {

  const tenantId = Digit.ULBService.getCurrentTenantId();
  const [defaultValues, setDefaultValues] = useState({});

  useEffect(() => {
    if (createdFacility?.id) {
      setDefaultValues({ ...createdFacility });
    } else {
      setDefaultValues({ ...createdFacility, isOperational: { code: "YES", name: t("TL_COMMON_YES") } });
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
    ],
    {
      select: (data) => data,
      enabled: !!tenantId,
    }
  );

  const booleanChoiceMenu = [
    { code: "YES", name: t("TL_COMMON_YES") },
    { code: "NO", name: t("TL_COMMON_NO") },
  ];

  const solarSolutionDesignTypes = mdmsResponse?.facility?.SolarSolutionDesignType || [];
  const facilityTypes = mdmsResponse?.facility?.FacilityType || [];

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
            label: "FACILITY_TYPE",
            isMandatory: true,
            key: "facilityType",
            type: "dropdown",
            populators: {
              name: "facilityType",
              error: t("CORE_COMMON_REQUIRED"),
              optionsKey: "name",
              required: true,
              options: facilityTypes,
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
            },
          },
          {
            inline: true,
            label: "FACILITY_POC_PHONE",
            isMandatory: true,
            key: "facilityPocPhone",
            type: "text",
            populators: {
              name: "facilityPocPhone",
              error: t("CORE_COMMON_REQUIRED"),
              validation: { minlength: 10, maxlength: 10, pattern: { value: /^[0-9]\d{9}$/, message: "Enter a valid mobile number" } },
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
            },
          },
          {
            inline: true,
            label: "FACILITY_HFR_ID",
            isMandatory: true,
            disable: !!createdFacility?.id,
            key: "hfrId",
            type: "text",
            populators: {
              name: "hfrId",
            },
          },
          {
            inline: true,
            label: "FACILITY_NIN_ID",
            isMandatory: true,
            disable: !!createdFacility?.id,
            key: "ninId",
            type: "text",
            populators: {
              name: "ninId",
            },
          },
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
            disable: false,
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
    [t, mdmsResponse, createdFacility, boundaryData, solarSolutionDesignTypes, facilityTypes]
  );

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
        onSubmit={onFormSubmit}
        label={t("CORE_COMMON_SUBMIT")}
        heading={""}
        cardStyle={{ boxShadow: "none" }}
        submitInForm={false}
        actionClassName={"reverse-actionbar-fixed"}
      />
    </div>
  );
};

export default FacilityForm;