import React, { useMemo } from "react";
import { FormComposerV2, TextInput } from "@egovernments/digit-ui-react-components";

const OrganizationForm = ({ t, onSubmit, orgType }) => {
  const orgStatusOptions = useMemo(
    () => [
      { code: "ACTIVE", name: t("ACTIVE") },
      { code: "INACTIVE", name: t("INACTIVE") },
    ],
    [t]
  );

  const defaultValues = useMemo(
    () => ({
      orgStatus: orgStatusOptions[0],
    }),
    [orgStatusOptions]
  );

  const formConfig = useMemo(() => {
    return [
      {
        key: "ORG_CREATE",
        body: [
          { inline: true, label: "ORG_NAME", isMandatory: true, key: "orgName", type: "text", populators: { name: "orgName", validation: { required: true }, error: t("CORE_COMMON_REQUIRED") } },
          { inline: true, label: "ORG_CODE", isMandatory: true, key: "orgCode", type: "text", populators: { name: "orgCode", validation: { required: true }, error: t("CORE_COMMON_REQUIRED") } },

          // ✅ removed ORG_TYPE dropdown completely

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
  }, [t, orgStatusOptions]);

  return (
    <div
      style={{
        paddingBottom: "90px",
        position: "relative",
        maxHeight: "70vh",
        overflow: "auto",
      }}
    >
      {/* ✅ Disabled orgType field (visual only; payload is enforced in AdminActions) */}
      <div style={{ marginTop: "10px", marginBottom: "20px" }}>
        <TextInput
          name="orgTypeDisplay"
          value={orgType || "VENDOR"}
          onChange={() => {}}
          disable={true}
          disabled={true}
          style={{ width: "100%" }}
        />
        <div style={{ fontSize: "12px", marginTop: "6px", opacity: 0.8 }}>
          {t("ORG_TYPE") || "Org Type"}
        </div>
      </div>

      <FormComposerV2
        heading={""}
        label={t("CORE_COMMON_SUBMIT")}
        config={formConfig}
        defaultValues={defaultValues}
        submitInForm={false}
        onSubmit={onSubmit}
        actionClassName={"reverse-actionbar-absolute"}
        noCardStyle={true}
        fieldStyle={{ marginRight: 0 }}
        cardStyle={{ padding: 0, boxShadow: "none" }}
      />
    </div>
  );
};

export default OrganizationForm;