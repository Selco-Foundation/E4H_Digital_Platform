import React, {useEffect, useMemo, useRef, useState} from "react";
import { FormComposerV2, Loader, Toast } from "@egovernments/digit-ui-react-components";

const OrganizationForm = ({ t, onSubmit, orgType, createdOrganization, formToast, setFormToast }) => {

  const tenantId = Digit.ULBService.getCurrentTenantId();
  const [defaultValues, setDefaultValues] = useState({});
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const formAccessors = useRef(null);
  const formContainerRef = useRef(null);

  useEffect(()=>{
    const root = formContainerRef.current;
    if(!root) return;
    root.querySelectorAll('input[name="orgPocPassword"], input[name="orgPocConfirmPassword"]')
        .forEach((input) => input.setAttribute("autocomplete", "new-password"));
    const username = root.querySelector('input[name="orgPocUsername"]');
    if (username) username.setAttribute("autocomplete", "off");
  })

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
        ...(orgType === "VENDOR"
          ? { orgSubType: organizationSubTypes.find((organizationSubType) => organizationSubType.code === "INSTALLATION_VENDOR") }
          : {}),
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
              hideInForm: !createdOrganization?.id,
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
              hideInForm: !createdOrganization?.id,
              options: organizationSubTypes,
              optionsKey: "name",
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
              hideInForm: !createdOrganization?.id,
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
            isMandatory: true,
            populators: {
              name: "orgPocName",
              error: t("CORE_COMMON_REQUIRED")
            }
          },
          {
            inline: true,
            label: "ORG_POC_PHONE",
            key: "orgPocPhone",
            type: "text",
            isMandatory: true,
            populators: {
              name: "orgPocPhone",
              validation: { minlength: 10, maxlength: 10, pattern: { value: /^[0-9]\d{9}$/, message: "Enter a valid mobile number" } },
              error: t("CORE_COMMON_REQUIRED")
            }
          },
          {
            inline: true,
            label: "ORG_POC_EMAIL",
            key: "orgPocEmail",
            type: "text",
            isMandatory: true,
            populators: {
              name: "orgPocEmail",
              validation: { pattern: { value: Digit.Utils.getPattern("Email"), message: t("CS_PROFILE_EMAIL_ERRORMSG") } },
              error: t("CORE_COMMON_REQUIRED")
            }
          },
          {
            inline: true,
            label: "ORG_POC_USERNAME",
            key: "orgPocUsername",
            type: "text",
            isMandatory: true,
            populators: {
              name: "orgPocUsername",
              error: t("CORE_COMMON_REQUIRED")
            }
          },
          {
            inline: true,
            label: "ORG_POC_PASSWORD",
            key: "orgPocPassword",
            type: "password",
            isMandatory: false,
            description : createdOrganization?.id ? t("ORG_POC_PASSWORD_HINT_EDIT") : t("ORG_POC_PASSWORD_HINT_CREATE"),
            populators: {
              name: "orgPocPassword",
              validation: {
                minLength: {value :7,message:"Password has to be at least 7 characters long"},
                maxLength: {value:15,message:"Password has to be at most 15 characters long"},
                pattern: {value: /^(?=.*\d)(?=.*[^A-Za-z0-9\s])(?=\S+$).*$/, message: t("ORG_POC_PASSWORD_PATTERN")},
              }
            }
          },
          {
            inline: true,
            label: "ORG_POC_CONFIRM_PASSWORD",
            key: "orgPocConfirmPassword",
            type: "password",
            isMandatory: false,
            populators: {
              name: "orgPocConfirmPassword",
              validation: {
                customValidation : (value) =>
                    (value||"") === (formAccessors.current?.getValues("orgPocPassword") || "") || t("CORE_COMMON_PROFILE_PASSWORD_MISMATCH"),
              },
            }
          }
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
      ref={formContainerRef}
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
        label={t("CORE_COMMON_SAVE")}
        config={formConfig}
        defaultValues={defaultValues}
        submitInForm={false}
        onSubmit={(data) => onSubmit({ ...defaultValues, ...data })}
        getFormAccessors={(accessors) =>(formAccessors.current = accessors)}
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
