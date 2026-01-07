import React, { useCallback, useMemo } from "react";
import { FormComposerV2, Loader } from "@egovernments/digit-ui-react-components";
import useBoundary from "../hooks/useBoundary";

const FacilityForm = ({ t, createdFacility = {}, onFormSubmit, wrapperStyle = {} }) => {

  const tenantId = Digit.ULBService.getCurrentTenantId();

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
              disable: false,
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
              disable: false,
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
              disable: false,
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
            },
          },
          {
            inline: true,
            label: "FACILITY_POC_EMAIL",
            isMandatory: true,
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
            isMandatory: false,
            key: "hfrId",
            type: "text",
            populators: {
              name: "hfrId",
            },
          },
          {
            inline: true,
            label: "FACILITY_NIN_ID",
            isMandatory: false,
            key: "ninId",
            type: "text",
            populators: {
              name: "ninId",
            },
          },
          {
            inline: true,
            label: "FACILITY_PINCODE",
            isMandatory: false,
            key: "pincode",
            type: "text",
            populators: {
              name: "pincode",
            },
          },
        ],
      },
    ],
    [t, mdmsResponse, boundaryData, solarSolutionDesignTypes, facilityTypes]
  );

  const handleFormValueChange = useCallback(
    (setValue, formData, formState, reset, setError, clearErrors) => {
      // const hasHfrId = formData?.hfrId && `${formData.hfrId}`.trim().length > 0;
      // const hasNinId = formData?.ninId && `${formData.ninId}`.trim().length > 0;
      //
      // if (!hasHfrId && !hasNinId) {
      //   const errorMessage = t("FACILITY_HFR_OR_NIN_REQUIRED");
      //   setError("hfrId", { type: "manual", message: errorMessage });
      //   setError("ninId", { type: "manual", message: errorMessage });
      // } else {
      //   clearErrors("hfrId");
      //   clearErrors("ninId");
      // }
    },
    [t]
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
        ...wrapperStyle,
      }}
    >
      <FormComposerV2
        key={JSON.stringify(createdFacility)}
        defaultValues={createdFacility}
        config={addFacilityFormConfig}
        onSubmit={onFormSubmit}
        label={t("CORE_COMMON_SUBMIT")}
        onFormValueChange={handleFormValueChange}
        heading={""}
        cardStyle={{ boxShadow: "none" }}
        submitInForm={false}
        actionClassName={"reverse-actionbar-absolute"}
      />
    </div>
  );
};

export default FacilityForm;