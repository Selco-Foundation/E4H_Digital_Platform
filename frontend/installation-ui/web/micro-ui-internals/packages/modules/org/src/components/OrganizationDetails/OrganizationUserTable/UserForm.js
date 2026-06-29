import React, {Fragment, useCallback, useEffect, useMemo, useState } from "react";
import { FormComposerV2, Loader, Table, TextInput, Toast, Button } from "@egovernments/digit-ui-react-components";
import useBoundary from "../../../hooks/useBoundary";
import CommonUtils from "../../../utilities/CommonUtils";
import CustomDustbinIcon from "../../Custom/CustomDustbinIcon";
import CustomUndoIcon from "../../Custom/CustomUndoButton";

const UserForm = ({ t, createdUser = {}, onFormSubmit, wrapperStyle = {}, organizationType, organizationSubType, formToast, setFormToast }) => {

  const tenantId = Digit.ULBService.getCurrentTenantId();
  const { info } = Digit.UserService.getUser()
  const [defaultValues, setDefaultValues] = useState({});
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [assignments, setAssignments] = useState([]);
  const [savedAssignments, setSavedAssignments] = useState([]);
  const [savedAssignmentsToDisplay, setSavedAssignmentsToDisplay] = useState([]);
  const [pageSize, setPageSize] = useState(10);
  const [pageOffset, setPageOffset] = useState(0);
  const [jurisdictionSearch, setJurisdictionSearch] = useState("");
  const [debouncedJurisdictionSearch, setDebouncedJurisdictionSearch] = useState("");
  const isPlatformOrgAdmin = (info?.roles || []).map((role) => role?.code).includes("ORG_PLATFORM_ADMIN");
  const [totalAssignmentsToDisplay, setTotalAssignmentsToDisplay] = useState(0);

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const { data: boundaryData, isLoading: boundaryLoading } = useBoundary();

  const { data: mdmsResponse, isLoading: mdmsLoading } = Digit.Hooks.useCustomMDMS(
    tenantId,
    "Organisation",
    [
      {
        name: "OrgRoles",
      },
      {
        name: "OrgRoleGroups",
      },
    ],
    {
      select: (data) => data,
      enabled: !!tenantId,
    }
  );

  const roles = (mdmsResponse?.Organisation?.OrgRoles || [])
    .filter((role) => (role.orgType === organizationType && ((!role.orgSubType && !organizationSubType) || role.orgSubType === organizationSubType)));

  const roleGroups = (mdmsResponse?.Organisation?.OrgRoleGroups || [])
    .filter((role) => (role.orgType === organizationType && ((!role.orgSubType && !organizationSubType) || role.orgSubType === organizationSubType)));

  useEffect(() => {
    if (createdUser?.orgUserId && mdmsResponse) {
      const roleCodes = createdUser.roles.map(role => role.code);
      setDefaultValues({
        name: createdUser.name,
        userName: createdUser.userName,
        contact: createdUser.mobileNumber,
        email: createdUser.emailId,
        roles: roleGroups.filter((roleGroup) => roleGroup.roleCodes.every((roleCode) => roleCodes.includes(roleCode))),
      });
    }
  }, [mdmsResponse]);

  useEffect(() => {
    const createdAssessments = [];
    (createdUser?.jurisdiction || []).forEach(jurisdiction => {
      createdAssessments.push(jurisdiction);
    })
    setSavedAssignments(createdAssessments);
  }, []);

  useEffect(() => {
    const timeout = setTimeout(() => {setDebouncedJurisdictionSearch(jurisdictionSearch)}, 500);
    return () => clearTimeout(timeout);
  }, [jurisdictionSearch]);

  useEffect(() => {
    const filteredSavedAssignments = savedAssignments
      .filter((savedAssignment) => {
        if(!debouncedJurisdictionSearch) return true;
        const name = t(`Boundary_${savedAssignment.boundary}`)?.toUpperCase();
        const code = savedAssignment.boundary?.toUpperCase();
        const searchedValue = debouncedJurisdictionSearch?.toUpperCase();
        return name.includes(searchedValue) || code.includes(searchedValue);
      });
    setTotalAssignmentsToDisplay(filteredSavedAssignments.length);
    setSavedAssignmentsToDisplay(filteredSavedAssignments.slice(pageOffset, Math.min(filteredSavedAssignments.length, pageOffset + pageSize)));
  }, [t, savedAssignments, debouncedJurisdictionSearch, pageOffset, pageSize]);

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
              validation: { pattern: { value: Digit.Utils.getPattern("Email"), message: t("CS_PROFILE_EMAIL_ERRORMSG") } },
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
              options: roleGroups,
            },
          },
        ],
      },
    ],
    [t, mdmsResponse, createdUser]
  );

  const assignmentsConfig = useCallback((disable) => [
    {
      key: "FACILITY_CREATE",
      body: [
        {
          inline: true,
          label: "CS_COUNTRY",
          isMandatory: false,
          key: "country",
          type: "component",
          component: "ORGCountrySelector",
          customProps: {
            name: "country",
            t,
            boundaryData,
            disable: disable,
          },
          populators: {
            name: "country",
            error: t("CORE_COMMON_REQUIRED"),
          },
        },
        {
          inline: true,
          label: "CS_STATE",
          isMandatory: false,
          key: "state",
          type: "component",
          component: "ORGStateSelector",
          customProps: {
            name: "state",
            countryIdentifier: "country",
            t,
            boundaryData,
            disable: disable,
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
            disable: disable,
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
            disable: disable,
          },
          populators: {
            name: "block",
            error: t("CORE_COMMON_REQUIRED"),
          },
        },
        {
          inline: true,
          label: "CS_FACILITY",
          isMandatory: false,
          key: "facility",
          type: "component",
          component: "ORGFacilitySelector",
          customProps: {
            name: "facility",
            blockIdentifier: "block",
            t,
            boundaryData,
            disable: disable,
          },
          populators: {
            name: "facility",
            error: t("CORE_COMMON_REQUIRED"),
          },
        },
      ],
    },
  ], [boundaryData])

  const handleFormSubmit = useCallback((formData) => {
    if (formData?.roles?.length) {

      const userRoles = roles.filter((role) => formData.roles.some((roleGroup) => roleGroup.roleCodes.includes(role.code)));
      const formattedFormData = {
        ...formData,
        roles: userRoles,
      }

      const jurisdictions = (savedAssignments || [])
        .map((savedAssignment) => ({...savedAssignment, isActive: !savedAssignment.isDeleted}));

      assignments.forEach((assignment) => {
        let jurisdiction;
        if (assignment.facility?.code) {
          jurisdiction = {
            hierarchy: "SELCO",
            boundary: assignment.facility.code,
            boundaryType: "Facility",
            tenantId: tenantId,
            isActive: !assignment.isDeleted,
          }
        } else if (assignment.block?.code) {
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
        } else if (assignment.country?.code) {
          jurisdiction = {
            hierarchy: "SELCO",
            boundary: assignment.country.code,
            boundaryType: "Country",
            tenantId: tenantId,
            isActive: !assignment.isDeleted,
          }
        }
        if (jurisdiction) jurisdictions.push(jurisdiction);
      })

      onFormSubmit(formattedFormData, jurisdictions);
    } else {
      setFormToast({ key: "error", label: t("USER_CREATION_SELECT_ROLE_ERROR") });
    }
  }, [assignments, mdmsResponse, savedAssignments]);

  const handleAssignmentFormChange = useCallback((index, _, formData) => {
    if (CommonUtils.isNotEqual(assignments[index].country, formData.country) ||
      CommonUtils.isNotEqual(assignments[index].state, formData.state) ||
      CommonUtils.isNotEqual(assignments[index].district, formData.district) ||
      CommonUtils.isNotEqual(assignments[index].block, formData.block) ||
      CommonUtils.isNotEqual(assignments[index].facility, formData.facility)) {
      setAssignments((prevAssignments) => prevAssignments.map((assignment, i) => (i === index ? {...assignment, ...formData} : assignment)));
    }
  }, [assignments]);

  const handleAssignmentAddition = () => {
    setAssignments((prevAssignments) => [{ country: null, state: null, district: null, block: null, facility: null }, ...prevAssignments]);
  }

  const deleteAssignment = (index) => {
    setAssignments((prevAssignments) => prevAssignments.reduce(
      (aggregate, assignment, i) => {
        if (i !== index) {
          aggregate.push(assignment);
        }
        return aggregate;
      }, []
    ));
  }

  const deleteSavedAssignment = (id) => {
    setSavedAssignments((prevSavedAssignments) => prevSavedAssignments.reduce(
      (aggregate, savedAssignment) => {
        if (savedAssignment.id === id) {
          aggregate.push({ ...savedAssignment, isDeleted: true });
        } else {
          aggregate.push(savedAssignment);
        }
        return aggregate;
      }, []
    ));
  }

  const undoSavedAssignmentDeletion = (id) => {
    setSavedAssignments((prevSavedAssignments) => prevSavedAssignments.reduce(
      (aggregate, savedAssignment) => {
        if (savedAssignment.id === id) {
          aggregate.push({ ...savedAssignment, isDeleted: false });
        } else {
          aggregate.push(savedAssignment);
        }
        return aggregate;
      }, []
    ));
  }

  const GetCell = (value, isDeleted) => (
    <span className="cell-text" style={{ color: isDeleted ? "#bc210a" : "#000000" }}>
      {value}
    </span>
  );

  const columns = [
    {
      Header: t("BOUNDARY_NAME"),
      Cell: ({ row }) => {
        return GetCell(row.original["boundary"] ? t(`Boundary_${row.original["boundary"]}`) : "-", row.original["isDeleted"]);
      },
    },
    {
      Header: t("BOUNDARY_TYPE"),
      Cell: ({ row }) => {
        return GetCell(row.original["boundaryType"] ? row.original["boundaryType"] : "-", row.original["isDeleted"]);
      },
    },
    {
      Header: t("BOUNDARY_CODE"),
      Cell: ({ row }) => {
        return GetCell(row.original["boundary"] ? row.original["boundary"] : "-", row.original["isDeleted"]);
      },
    },
    {
      Header: t("CS_COMMON_ACTIONS"),
      Cell: ({ row }) => {
        return GetCell(
          row.original["isDeleted"] ?
            (
              <button
                type="button"
                style={{background: "none"}}
                onClick={() => undoSavedAssignmentDeletion(row.original["id"])}
              >
                <CustomUndoIcon colourFill={"#00703C"} height={"18"} strokeWidth={"3"} />
              </button>
            )
            :
            (
              <button
                type="button"
                style={{background: "none"}}
                onClick={() => deleteSavedAssignment(row.original["id"])}
              >
                <CustomDustbinIcon colourFill={"#bc210a"} />
              </button>
            )
        );
      },
    }
  ];

  const onPageSizeChange = (e) => {
    setPageSize(parseInt(e.target.value));
    setPageOffset(0);
  };

  const onNextPage = () => {
    setPageOffset(pageOffset + pageSize);
  };

  const onPrevPage = () => {
    setPageOffset(pageOffset - pageSize);
  };

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
      {isPlatformOrgAdmin && (
        <div style={{ marginBottom: "30px", padding: "10px" }}>
          <h2 style={{
            margin: 0,
            fontSize: "24px",
            fontWeight: "bold",
            marginBottom: "20px",
          }}>
            {t("ASSIGNMENTS")}
          </h2>
          <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "10px" }}>
            {createdUser?.orgUserId && (
              <TextInput
                t={t}
                onChange={(e) => setJurisdictionSearch(e.target.value)}
                placeholder={t("SEARCH_ASSIGNMENTS")}
                textInputStyle={{ maxWidth: "300px", marginBottom: "0" }}
                style={{ marginBottom: "0" }}
              />
            )}
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
          {assignments
            .map((assignment, index) => !assignment.isDeleted && (
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
                    {t("NEW_ASSIGNMENT") + ":"}
                  </h2>
                  <button onClick={() => deleteAssignment(index)} style={{background: 'none', border: 'none', fontSize: 18, cursor: 'pointer'}}>
                    <CustomDustbinIcon colourFill={"#bc210a"} />
                  </button>
                </div>
                <FormComposerV2
                  key={JSON.stringify(assignment)}
                  defaultValues={assignment}
                  config={assignmentsConfig(false)}
                  onFormValueChange={(_, formData) => handleAssignmentFormChange(index, _, formData)}
                  label={""}
                  heading={""}
                  cardStyle={{ boxShadow: "none" }}
                  submitInForm={false}
                />
              </div>
            ))
          }
          {!!savedAssignmentsToDisplay?.length ? (
            <Fragment>
              <div
                style={{
                  backgroundColor: "white",
                }}
              >
                <div
                  className={"health-facility-table-wrapper"}
                  style={{
                    margin: "0",
                    overflow: "auto",
                  }}
                >
                  <Table
                    t={t}
                    customTableWrapperClassName={"user-jurisdictions-table"}
                    data={savedAssignmentsToDisplay}
                    columns={columns}
                    getCellProps={() => {
                      return {
                        style: {
                          maxWidth: "100%",
                          padding: "17.24px 18px",
                          fontSize: "15px",
                        },
                      };
                    }}
                    onNextPage={onNextPage}
                    onPrevPage={onPrevPage}
                    currentPage={Math.floor(pageOffset / pageSize)}
                    totalRecords={totalAssignmentsToDisplay}
                    onPageSizeChange={onPageSizeChange}
                    pageSizeLimit={pageSize}
                  />
                </div>
              </div>
            </Fragment>
          ) : (!assignments?.length && createdUser?.orgUserId) && (
            <div style={{
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              height: "200px",
              fontSize: "18px",
              color: "#666"
            }}>
              {t("NO_ASSIGNMENTS_FOUND")}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default UserForm;
