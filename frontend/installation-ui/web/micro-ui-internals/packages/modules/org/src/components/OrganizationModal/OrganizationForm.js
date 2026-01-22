import React, {useEffect, useMemo, useState} from "react";
import { FormComposerV2, Loader, Toast } from "@egovernments/digit-ui-react-components";

const OrganizationForm = ({ t, onSubmit, orgType, createdOrganization, formToast, setFormToast }) => {

  const tenantId = Digit.ULBService.getCurrentTenantId();
  const [defaultValues, setDefaultValues] = useState({});
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const { data: mdmsResponse, isLoading: mdmsLoading } = Digit.Hooks.useCustomMDMS(
    tenantId,
    "Organisation",
    [
      {
        name: "OrgType",
      },
      {
        name: "OrgSubType",
      },
      {
        name: "OrgStatus",
      },
    ],
    {
      select: (data) => data,
      enabled: !!tenantId,
    }
  );

  const organizationTypes = mdmsResponse?.Organisation?.OrgType || [];
  const organizationSubTypes = (mdmsResponse?.Organisation?.OrgSubType || []).filter((orgSubType) => orgSubType.orgType === orgType);
  const organizationStatuses = mdmsResponse?.Organisation?.OrgStatus || [];

  useEffect(() => {
    if (createdOrganization?.id) {
      setDefaultValues({
        orgType: organizationTypes.find((organizationType) => organizationType.code === orgType),
        orgSubType: organizationSubTypes.find((organizationSubType) => organizationSubType.code === createdOrganization.orgSubType),
        orgName: createdOrganization.name,
        orgCode: createdOrganization.code,
        orgStatus: organizationStatuses.find((organizationStatus) => organizationStatus.code === createdOrganization.orgStatus),
        orgPocName: createdOrganization.orgPocName,
        orgPocPhone: createdOrganization.orgPocPhone,
        orgPocEmail: createdOrganization.orgPocEmail,
        orgPocUsername: createdOrganization.orgPocUsername,
      })
    } else {
      setDefaultValues({
        orgType: organizationTypes.find((organizationType) => organizationType.code === orgType),
        orgStatus: organizationStatuses.find((organizationStatus) => organizationStatus.code === "ACTIVE"),
      })
    }
  }, [mdmsResponse]);

  const formConfig = useMemo(() => [
      {
        key: "ORG_CREATE",
        body: [
          {
            inline: true,
            label: "ORG_NAME",
            isMandatory: true,
            key: "orgName",
            type: "text",
            populators: {
              name: "orgName",
              error: t("CORE_COMMON_REQUIRED")
            }
          },
          {
            inline: true,
            label: "ORG_TYPE",
            isMandatory: true,
            disable: true,
            key: "orgType",
            type: "dropdown",
            populators: {
              name: "orgType",
              options: organizationTypes,
              optionsKey: "name",
              error: t("CORE_COMMON_REQUIRED")
            }
          },
          {
            inline: true,
            label: "ORG_SUB_TYPE",
            isMandatory: !!organizationSubTypes?.length,
            disable: (!organizationSubTypes?.length || createdOrganization?.id),
            key: "orgSubType",
            type: "dropdown",
            populators: {
              name: "orgSubType",
              options: organizationSubTypes,
              optionsKey: "name",
              error: t("CORE_COMMON_REQUIRED")
            }
          },
          {
            inline: true,
            label: "ORG_CODE",
            isMandatory: true,
            key: "orgCode",
            type: "text",
            populators: {
              name: "orgCode",
              error: t("CORE_COMMON_REQUIRED")
            }
          },
          {
            inline: true,
            label: "ORG_STATUS",
            isMandatory: true,
            disable: !createdOrganization?.id,
            key: "orgStatus",
            type: "dropdown",
            populators: {
              name: "orgStatus",
              options: organizationStatuses,
              optionsKey: "name",
              error: t("CORE_COMMON_REQUIRED")
            }
          },
          {
            inline: true,
            label: "ORG_POC_NAME",
            key: "orgPocName",
            type: "text",
            populators: {
              name: "orgPocName"
            }
          },
          {
            inline: true,
            label: "ORG_POC_PHONE",
            key: "orgPocPhone",
            type: "text",
            populators: {
              name: "orgPocPhone"
            }
          },
          {
            inline: true,
            label: "ORG_POC_EMAIL",
            key: "orgPocEmail",
            type: "text",
            populators: {
              name: "orgPocEmail"
            }
          },
          {
            inline: true,
            label: "ORG_POC_USERNAME",
            key: "orgPocUsername",
            type: "text",
            populators: {
              name: "orgPocUsername"
            }
          },
        ],
      },
    ],
    [t, orgType, createdOrganization, mdmsResponse]
  );

  if (mdmsLoading) {
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
        padding: "15px 15px 30px",
        maxHeight: "70vh",
        overflow: "auto",
      }}
    >
      <FormComposerV2
        key={JSON.stringify(defaultValues)}
        heading={""}
        label={t("CORE_COMMON_SUBMIT")}
        config={formConfig}
        defaultValues={defaultValues}
        submitInForm={false}
        onSubmit={onSubmit}
        actionClassName={"reverse-actionbar-absolute"}
        cardStyle={{ boxShadow: "none" }}
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

export default OrganizationForm;