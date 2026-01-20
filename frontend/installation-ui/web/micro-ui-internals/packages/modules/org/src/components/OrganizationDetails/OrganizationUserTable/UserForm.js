import React, {useCallback, useEffect, useMemo, useState } from "react";
import { FormComposerV2, DustbinIcon, Loader, Toast, Button } from "@egovernments/digit-ui-react-components";
import useBoundary from "../../../hooks/useBoundary";
import CommonUtils from "../../../utilities/CommonUtils";

const UserForm = ({ t, createdUser = {}, onFormSubmit, wrapperStyle = {}, organizationType, formToast, setFormToast }) => {

  const tenantId = Digit.ULBService.getCurrentTenantId();
  const [defaultValues, setDefaultValues] = useState({});
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [assignments, setAssignments] = useState([])

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  useEffect(() => {
    if (createdUser?.id) {
      setDefaultValues({ ...createdUser });
    }
  }, []);

  const { data: boundaryData, isLoading: boundaryLoading } = useBoundary();

  const { data: mdmsResponse, isLoading: mdmsLoading } = Digit.Hooks.useCustomMDMS(
    tenantId,
    "Organisation",
    [
      {
        name: "OrgRoles",
      },
    ],
    {
      select: (data) => data,
      enabled: !!tenantId,
    }
  );

  const roles = (mdmsResponse?.Organisation?.OrgRoles || []).filter((role) => role.orgType === organizationType);

  const isFormLoading = boundaryLoading || mdmsLoading;

  const userFormConfig = useMemo(
    () => [
      {
        key: "USER_UPSERT",
        body: [
          {
            inline: true,
            label: "ORG_USER_NAME",
            isMandatory: true,
            key: "name",
            type: "text",
            populators: {
              name: "name",
              error: t("CORE_COMMON_REQUIRED"),
            },
          },
          {
            inline: true,
            label: "ORG_USER_USERNAME",
            isMandatory: true,
            key: "userName",
            type: "text",
            populators: {
              name: "userName",
              error: t("CORE_COMMON_REQUIRED"),
            },
          },
          {
            inline: true,
            label: "ORG_USER_CONTACT",
            isMandatory: true,
            key: "contact",
            type: "text",
            populators: {
              name: "contact",
              error: t("CORE_COMMON_REQUIRED"),
              validation: { minlength: 10, maxlength: 10, pattern: { value: /^[0-9]\d{9}$/, message: "Enter a valid mobile number" } },
            },
          },
          {
            inline: true,
            label: "ORG_USER_EMAIL",
            isMandatory: true,
            key: "email",
            type: "text",
            populators: {
              name: "email",
              error: t("CORE_COMMON_REQUIRED"),
            },
          },
          {
            inline: true,
            label: "ORG_USER_ROLES",
            isMandatory: true,
            key: "roles",
            type: "multiselectdropdown",
            populators: {
              name: "roles",
              error: t("CORE_COMMON_REQUIRED"),
              optionsKey: "name",
              required: true,
              options: roles,
            },
          },
        ],
      },
    ],
    [t, mdmsResponse, createdUser, roles]
  );

  const assignmentsConfig = useMemo(
    () => [
      {
        key: "FACILITY_CREATE",
        body: [
          {
            inline: true,
            label: "CS_STATE",
            isMandatory: false,
            key: "state",
            type: "component",
            component: "ORGStateSelector",
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
            isMandatory: false,
            key: "district",
            type: "component",
            component: "ORGDistrictSelector",
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
            isMandatory: false,
            key: "block",
            type: "component",
            component: "ORGBlockSelector",
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
        ],
      },
    ],
    [boundaryData]
  )

  const handleFormSubmit = useCallback((formData) => {
    if (formData?.roles?.length) {
      const jurisdictions = [];

      assignments.forEach((assignment) => {
        let jurisdiction;
        if (assignment.block?.code) {
          jurisdiction = {
            hierarchy: "SELCO",
            boundary: assignment.block.code,
            boundaryType: "Block",
            tenantId: tenantId,
            isActive: !assignment.isDeleted,
          }
        } else if (assignment.district?.code) {
          jurisdiction = {
            hierarchy: "SELCO",
            boundary: assignment.district.code,
            boundaryType: "District",
            tenantId: tenantId,
            isActive: !assignment.isDeleted,
          }
        } else if (assignment.state?.code) {
          jurisdiction = {
            hierarchy: "SELCO",
            boundary: assignment.state.code,
            boundaryType: "State",
            tenantId: tenantId,
            isActive: !assignment.isDeleted,
          }
        }
        if (jurisdiction) jurisdictions.push(jurisdiction);
      })

      onFormSubmit(formData, jurisdictions);
    } else {
      setFormToast({ key: "error", label: t("USER_CREATION_SELECT_ROLE_ERROR") });
    }
  }, [assignments]);

  const handleAssignmentFormChange = useCallback((index, _, formData) => {
    if (CommonUtils.isNotEqual(assignments[index], formData)) {
      setAssignments((prevAssignments) => prevAssignments.map((assignment, i) => (i === index ? formData : assignment)));
    }
  }, [assignments]);

  const handleAssignmentAddition = () => {
    setAssignments((prevAssignments) => [...prevAssignments, { state: null, district: null, block: null }]);
  }

  const deleteAssignment = (index) => {
    setAssignments((prevAssignments) => prevAssignments.reduce(
      (aggregate, assignment, i) => {
        if (i === index) {
          if (assignment.id) {
            aggregate.push({ ...assignment, isDeleted: true });
          }
        } else {
          aggregate.push(assignment);
        }
        return aggregate;
      }, []
    ));
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
        padding: "15px 15px 30px",
        maxHeight: "70vh",
        overflow: "auto",
        ...wrapperStyle,
      }}
    >
      <FormComposerV2
        key={JSON.stringify(defaultValues)}
        defaultValues={defaultValues}
        config={userFormConfig}
        onSubmit={handleFormSubmit}
        label={t("CORE_COMMON_SUBMIT")}
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
      {organizationType === "PLATFORM" && (
        <div style={{ marginBottom: "30px", padding: "10px" }}>
          <h2 style={{
            margin: 0,
            fontSize: "24px",
            fontWeight: "bold",
            marginBottom: "20px",
          }}>
            {t("ASSIGNMENTS")}
          </h2>
          {assignments
            .filter((assignment) => !assignment.isDeleted)
            .map((assignment, index) => (
              <div
                className={"org-user-assignment"}
                style={{
                  border: "1px solid #ccc",
                  padding: "20px 20px 0",
                  marginBottom: "10px",
                  borderRadius: "10px",
                }}
              >
                <div style={{display: "flex", justifyContent: "space-between", marginBottom: "10px"}}>
                  <h2 style={{
                    margin: 0,
                    fontSize: "18px",
                    fontWeight: "bold",
                  }}>
                    {t("ASSIGNMENT") + " " + (index + 1) + ":"}
                  </h2>
                  <button onClick={() => deleteAssignment(index)} style={{background: 'none', border: 'none', fontSize: 18, cursor: 'pointer'}}>
                    <DustbinIcon />
                  </button>
                </div>
                <FormComposerV2
                  key={JSON.stringify(assignment)}
                  defaultValues={assignment}
                  config={assignmentsConfig}
                  onFormValueChange={handleAssignmentFormChange.bind(this, index)}
                  label={""}
                  heading={""}
                  cardStyle={{ boxShadow: "none" }}
                  submitInForm={false}
                />
              </div>
            ))
          }
          <Button
            variation="secondary"
            label={t("ADD_ASSIGNMENT")}
            onButtonClick={handleAssignmentAddition}
            style={{
              backgroundColor: "white",
              border: "1px solid #d35400",
              color: "#d35400",
              padding: "8px 20px",
              cursor: "pointer",
              fontWeight: "bold",
              fontSize: "16px",
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              gap: "5px",
              height: "40px",
            }}
          />
        </div>
      )}
    </div>
  );
};

export default UserForm;