import React, {Fragment, useCallback, useEffect, useMemo, useState } from "react";
import { FormComposerV2, Loader, Table, TextInput, Toast, Button } from "@egovernments/digit-ui-react-components";
import useBoundary from "../../../hooks/useBoundary";
import CommonUtils from "../../../utilities/CommonUtils";
import CustomDustbinIcon from "../../Custom/CustomDustbinIcon";
import CustomUndoIcon from "../../Custom/CustomUndoButton";
import CustomEditIcon from "../../Custom/CustomEditIcon";
import AppliesToSelector from "./AppliesToSelector";
import {
  appliesToAllRoles,
  collapseRoleCodesToGroups,
  expandGroupsToRoleCodes,
  pruneAssignmentGroups,
} from "../../../utilities/jurisdictionRoles";

/**
 * A saved jurisdiction stores only its own boundary code and type. The add-assignment
 * cascade needs the whole chain above it, and every selector matches its options on
 * parentCode, so the ancestors have to be the boundaryData entries themselves rather than
 * bare codes - otherwise each selector drops the value it was given as stale.
 */
const buildBoundaryCascade = (boundary, boundaryType, boundaryData, t) => {
  const cascade = { country: null, state: null, district: null, block: null, facility: null };
  const levels = [
    { key: "country", type: "Country", list: boundaryData?.countries },
    { key: "state", type: "State", list: boundaryData?.states },
    { key: "district", type: "District", list: boundaryData?.districts },
    { key: "block", type: "Block", list: boundaryData?.blocks },
    { key: "facility", type: "Facility", list: boundaryData?.facilities },
  ];

  let index = levels.findIndex((level) => level.type === boundaryType);
  let code = boundary;
  while (index >= 0 && code) {
    const match = (levels[index].list || []).find((entry) => entry.code === code);
    if (!match) break;
    cascade[levels[index].key] = { ...match, name: t(`Boundary_${match.code}`) };
    code = match.parentCode;
    index -= 1;
  }
  return cascade;
};

/** The deepest level the cascade has a value for is the jurisdiction's boundary. */
const resolveCascadeBoundary = (assignment) => {
  const levels = [
    { key: "facility", type: "Facility" },
    { key: "block", type: "Block" },
    { key: "district", type: "District" },
    { key: "state", type: "State" },
    { key: "country", type: "Country" },
  ];
  const level = levels.find(({ key }) => assignment?.[key]?.code);
  return level ? { boundary: assignment[level.key].code, boundaryType: level.type } : null;
};

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
  const [selectedRoleGroups, setSelectedRoleGroups] = useState([]);

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
      setSelectedRoleGroups(roleGroups.filter((roleGroup) => roleGroup.roleCodes.every((roleCode) => roleCodes.includes(roleCode))));
    }
  }, [mdmsResponse]);

  useEffect(() => {
    if (!mdmsResponse) return;
    const createdAssessments = [];
    (createdUser?.jurisdiction || []).forEach(jurisdiction => {
      createdAssessments.push({
        ...jurisdiction,
        // Stored as role codes; the form works in groups. An empty list stays empty, which
        // is "all roles" and must not be confused with "no match found".
        roles: jurisdiction.roles?.length ? collapseRoleCodesToGroups(jurisdiction.roles, roleGroups) : null,
      });
    })
    setSavedAssignments(createdAssessments);
  }, [mdmsResponse]);

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

  // Rule 3: dropping a role group strips it from every assignment scoped to it. A row left
  // with nothing is meaningless rather than invalid, so it goes and the admin is told which.
  useEffect(() => {
    if (!mdmsResponse) return;
    /**
     * Don't prune while the form has reported no roles for a user who demonstrably has some.
     *
     * selectedRoleGroups is seeded from createdUser in an effect above, and this one runs in
     * the same commit, where the state is still its initial []. Pruning against that reads as
     * "holds no roles at all" and deletes every narrowed jurisdiction the user has - which is
     * what made a scoped row vanish from the table on open.
     *
     * A ref set in the seeding effect would not help: refs change synchronously, so this pass
     * would see it set while the state it describes has not propagated yet.
     *
     * Clearing the roles field by hand lands here too, and is also skipped. That costs nothing:
     * roles are mandatory, so a save in that state is rejected before scoping matters.
     */
    if (createdUser?.orgUserId && !selectedRoleGroups.length && createdUser.roles?.length) return;

    const emptied = [];
    const pruneList = (list) => list.reduce((kept, assignment) => {
      const retained = pruneAssignmentGroups(assignment, selectedRoleGroups);
      if (retained === null) {
        kept.push(assignment);
      } else if (retained.length) {
        kept.push({ ...assignment, roles: retained });
      } else {
        emptied.push(assignment.boundary || assignment.facility?.code || assignment.block?.code
          || assignment.district?.code || assignment.state?.code || assignment.country?.code);
      }
      return kept;
    }, []);

    setAssignments((prev) => {
      const next = pruneList(prev);
      return next.length === prev.length ? prev : next;
    });
    setSavedAssignments((prev) => {
      const next = pruneList(prev);
      return next.length === prev.length ? prev : next;
    });

    if (emptied.length) {
      setFormToast({
        key: "warning",
        label: `${t("ORG_ASSIGNMENT_REMOVED_WITH_ROLE")}: ${emptied.filter(Boolean).join(", ")}`,
      });
    }
  }, [selectedRoleGroups, mdmsResponse]);

  const setAssignmentRoles = useCallback((index, roles) => {
    setAssignments((prev) => prev.map((assignment, i) => (i === index ? { ...assignment, roles } : assignment)));
  }, []);

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

      // §1.3: an incoming jurisdiction with an id and roles == null keeps its stored roles,
      // so every row the form rendered sends `roles` explicitly. [] is "all roles", and is
      // what clears stored scoping; omitting the key would mean "unchanged".
      const jurisdictions = (savedAssignments || [])
        .map((savedAssignment) => ({
          ...savedAssignment,
          isActive: !savedAssignment.isDeleted,
          roles: appliesToAllRoles(savedAssignment)
            ? []
            : roles.filter((role) => expandGroupsToRoleCodes(savedAssignment.roles).includes(role.code)),
        }));

      for (const assignment of assignments) {
        const resolved = resolveCascadeBoundary(assignment);
        let jurisdiction = resolved && {
          hierarchy: "SELCO",
          boundary: resolved.boundary,
          boundaryType: resolved.boundaryType,
          tenantId: tenantId,
          isActive: !assignment.isDeleted,
        };
        // A row lifted out of the table carries the stored identity, but takes its boundary
        // from the cascade like any other - so moving it edits the grant instead of looking
        // edited and saving the old boundary back.
        if (assignment.id) {
          if (!jurisdiction) {
            setFormToast({ key: "error", label: t("ORG_ASSIGNMENT_BOUNDARY_REQUIRED") });
            return;
          }
          jurisdiction = {
            ...jurisdiction,
            id: assignment.id,
            orgId: assignment.orgId,
            code: assignment.code,
            additionalDetails: assignment.additionalDetails,
            auditDetails: assignment.auditDetails,
          };
        }
        if (jurisdiction) {
          jurisdiction.roles = appliesToAllRoles(assignment)
            ? []
            : roles.filter((role) => expandGroupsToRoleCodes(assignment.roles).includes(role.code));
          jurisdictions.push(jurisdiction);
        }
      }

      // Rule 4 (Q9): a user with no active jurisdiction sees nothing. This holds on update as
      // well as create - removing the last assignment is the easiest way to produce such a
      // user, and the backend refuses it too (ORG_USER_JURISDICTION_REQUIRED).
      if (!jurisdictions.some((jurisdiction) => jurisdiction.isActive)) {
        setFormToast({ key: "error", label: t("ORG_USER_AT_LEAST_ONE_ASSIGNMENT") });
        return;
      }

      onFormSubmit(formattedFormData, jurisdictions);
    } else {
      setFormToast({ key: "error", label: t("USER_CREATION_SELECT_ROLE_ERROR") });
    }
  }, [assignments, mdmsResponse, savedAssignments, createdUser]);

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

  /**
   * Editing a saved assignment lifts it out of the table into the same card used for adding
   * one, with the boundary cascade pre-filled from its stored boundary. It keeps its id, so
   * on save it updates the jurisdiction it came from rather than adding a second one.
   */
  const editSavedAssignment = (id) => {
    const target = savedAssignments.find((savedAssignment) => savedAssignment.id === id);
    if (!target) return;
    setSavedAssignments((prev) => prev.filter((savedAssignment) => savedAssignment.id !== id));
    setAssignments((prev) => [
      { ...target, ...buildBoundaryCascade(target.boundary, target.boundaryType, boundaryData, t) },
      ...prev,
    ]);
  };

  /** Put it back in the table. Edits are kept — nothing is written until the form is saved. */
  const returnSavedAssignmentToTable = (index) => {
    const target = assignments[index];
    if (!target) return;
    // The table reads boundary/boundaryType, so a boundary changed in the card has to be
    // written back onto them or the row would keep showing the one it was saved with.
    const resolved = resolveCascadeBoundary(target);
    if (!resolved) {
      setFormToast({ key: "error", label: t("ORG_ASSIGNMENT_BOUNDARY_REQUIRED") });
      return;
    }
    setAssignments((prev) => prev.filter((_, i) => i !== index));
    setSavedAssignments((prev) => [...prev, { ...target, ...resolved }]);
  };

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

  /** Scoping as read-only chips, matching how the users table lists a user's roles. */
  const GetRoleTags = (groups, isDeleted) => (
    <div style={{ display: "flex", flexWrap: "wrap", gap: "10px", alignItems: "center" }}>
      {appliesToAllRoles({ roles: groups }) ? (
        <span style={{ backgroundColor: "#EEEEEE", color: isDeleted ? "#bc210a" : "#505A5F", width: "fit-content", padding: "5px 10px" }}>
          {t("ORG_ASSIGNMENT_ALL_ROLES")}
        </span>
      ) : (
        groups.map((group) => (
          <span
            key={group.code}
            style={{
              backgroundColor: "#F1FFF8",
              color: isDeleted ? "#bc210a" : "#00703C",
              width: "fit-content",
              padding: "5px 10px",
            }}
          >
            {group.name}
          </span>
        ))
      )}
    </div>
  );

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
      Header: t("ORG_ASSIGNMENT_APPLIES_TO"),
      Cell: ({ row }) => GetRoleTags(row.original["roles"], row.original["isDeleted"]),
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
              <div>
                <button
                  type="button"
                  style={{paddingTop: "3px", backgroundColor: "white", marginRight: "5px"}}
                  onClick={() => editSavedAssignment(row.original["id"])}
                >
                  <CustomEditIcon width={"18"} height={"18"} viewBox={"0 0 20 20"} />
                </button>
                <button
                  type="button"
                  style={{background: "none"}}
                  onClick={() => deleteSavedAssignment(row.original["id"])}
                >
                  <CustomDustbinIcon colourFill={"#bc210a"} />
                </button>
              </div>
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
        onFormValueChange={(_, formData) => {
          if (CommonUtils.isNotEqual(selectedRoleGroups, formData?.roles || [])) {
            setSelectedRoleGroups(formData?.roles || []);
          }
        }}
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
                    display: "flex",
                    alignItems: "center",
                    gap: "10px",
                  }}>
                    {assignment.id ? t("EDIT_ASSIGNMENT") + ":" : t("NEW_ASSIGNMENT") + ":"}
                    {assignment.id && (
                      <span style={{
                        backgroundColor: "#EEEEEE",
                        color: "#505A5F",
                        padding: "4px 12px",
                        borderRadius: "4px",
                        fontSize: "13px",
                        fontWeight: 500,
                      }}>
                        {t("ORG_ASSIGNMENT_SAVED_EARLIER")}
                      </span>
                    )}
                  </h2>
                  {assignment.id ? (
                    <Button
                      variation="secondary"
                      label={t("CORE_COMMON_DONE")}
                      onButtonClick={() => returnSavedAssignmentToTable(index)}
                      style={{ height: "32px" }}
                    />
                  ) : (
                    <button onClick={() => deleteAssignment(index)} style={{background: 'none', border: 'none', fontSize: 18, cursor: 'pointer'}}>
                      <CustomDustbinIcon colourFill={"#bc210a"} />
                    </button>
                  )}
                </div>
                <FormComposerV2
                  key={JSON.stringify({ ...assignment, roles: undefined })}
                  defaultValues={assignment}
                  config={assignmentsConfig(false)}
                  onFormValueChange={(_, formData) => handleAssignmentFormChange(index, _, formData)}
                  label={""}
                  heading={""}
                  cardStyle={{ boxShadow: "none" }}
                  submitInForm={false}
                />
                <div style={{ padding: "0 0 20px" }}>
                  <div style={{ fontWeight: 700, marginBottom: "8px" }}>{t("ORG_ASSIGNMENT_APPLIES_TO")}</div>
                  <AppliesToSelector
                    t={t}
                    value={assignment.roles}
                    options={selectedRoleGroups}
                    onChange={(roles) => setAssignmentRoles(index, roles)}
                  />
                </div>
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
