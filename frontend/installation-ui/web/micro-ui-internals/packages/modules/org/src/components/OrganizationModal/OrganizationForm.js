import React, { useMemo } from "react";
import { FormComposerV2 } from "@egovernments/digit-ui-react-components";

const OrganizationForm = ({ t, onSubmit }) => {
  const orgTypeOptions = useMemo(
    () => [
      { code: "VENDOR", name: t("VENDOR") },
      { code: "PLATFORM", name: t("PLATFORM") },
    ],
    [t]
  );

  const orgStatusOptions = useMemo(
    () => [
      { code: "ACTIVE", name: t("ACTIVE") },
      { code: "INACTIVE", name: t("INACTIVE") },
    ],
    [t]
  );

  const defaultValues = useMemo(
    () => ({
      orgType: orgTypeOptions[0],
      orgStatus: orgStatusOptions[0],
    }),
    [orgTypeOptions, orgStatusOptions]
  );

  const formConfig = useMemo(() => {
    return [
      {
        key: "ORG_CREATE",
        body: [
          { inline: true, label: "ORG_NAME", isMandatory: true, key: "orgName", type: "text", populators: { name: "orgName", validation: { required: true }, error: t("CORE_COMMON_REQUIRED") } },
          { inline: true, label: "ORG_CODE", isMandatory: true, key: "orgCode", type: "text", populators: { name: "orgCode", validation: { required: true }, error: t("CORE_COMMON_REQUIRED") } },

          { inline: true, label: "ORG_TYPE", isMandatory: true, key: "orgType", type: "dropdown", populators: { name: "orgType", options: orgTypeOptions, optionsKey: "name", validation: { required: true }, error: t("CORE_COMMON_REQUIRED") } },
          { inline: true, label: "ORG_STATUS", isMandatory: true, key: "orgStatus", type: "dropdown", populators: { name: "orgStatus", options: orgStatusOptions, optionsKey: "name", validation: { required: true }, error: t("CORE_COMMON_REQUIRED") } },

          { inline: true, label: "ORG_POC_NAME", key: "orgPocName", type: "text", populators: { name: "orgPocName" } },
          { inline: true, label: "ORG_POC_PHONE", key: "orgPocPhone", type: "text", populators: { name: "orgPocPhone" } },
          { inline: true, label: "ORG_POC_EMAIL", key: "orgPocEmail", type: "text", populators: { name: "orgPocEmail" } },

          { inline: true, label: "ORG_LATITUDE", isMandatory: true, key: "latitude", type: "text", populators: { name: "latitude", validation: { required: true }, error: t("CORE_COMMON_REQUIRED") } },
          { inline: true, label: "ORG_LONGITUDE", isMandatory: true, key: "longitude", type: "text", populators: { name: "longitude", validation: { required: true }, error: t("CORE_COMMON_REQUIRED") } },
          { inline: true, label: "ORG_HQ_ADDRESS", isMandatory: true, key: "hqAddress", type: "text", populators: { name: "hqAddress", validation: { required: true }, error: t("CORE_COMMON_REQUIRED") } },
        ],
      },
    ];
  }, [t, orgTypeOptions, orgStatusOptions]);

  return (
    <div
      style={{
        paddingBottom: "90px",     // ✅ leave room for sticky action bar
        position: "relative",
        maxHeight: "70vh",         // ✅ popup scroll area
        overflow: "auto",
      }}
    >
      <FormComposerV2
        heading={""}               // ✅ FA style: header handled by modal, not inside form
        label={t("CORE_COMMON_SUBMIT")}
        config={formConfig}
        defaultValues={defaultValues}
        submitInForm={false}
        onSubmit={onSubmit}

        // ✅ critical: make popup submit-bar behave like FA
        actionClassName={"reverse-actionbar-absolute"}

        noCardStyle={true}
        fieldStyle={{ marginRight: 0 }}
        cardStyle={{ padding: 0, boxShadow: "none" }}
      />
    </div>
  );
};

export default OrganizationForm;