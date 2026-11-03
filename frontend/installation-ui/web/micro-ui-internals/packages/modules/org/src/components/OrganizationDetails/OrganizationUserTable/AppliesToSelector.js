import React from "react";
import { Dropdown, RemoveableTag } from "@egovernments/digit-ui-react-components";
import { appliesToAllRoles, describeAllRoles } from "../../../utilities/jurisdictionRoles";

/**
 * "Applies to" — which of the user's roles one jurisdiction scopes.
 *
 * `value` is a list of OrgRoleGroups, or null/empty for "every role the user holds". The two
 * are different things and the UI has to say which it is: an empty multiselect reads as
 * *none*, the exact opposite, so the unscoped state is drawn as an explicit `All roles` chip
 * with the roles it currently resolves to underneath it.
 *
 * Narrowing is a live rule, not a snapshot — `All roles` keeps meaning "whatever the user
 * holds", so granting a role later widens every unscoped jurisdiction with it.
 */
const AppliesToSelector = ({ t, value, options = [], onChange, disabled = false }) => {
  const selected = value || [];
  const isAllRoles = appliesToAllRoles({ roles: selected });
  const selectedCodes = new Set(selected.map((group) => group?.code));
  const remaining = options.filter((group) => !selectedCodes.has(group?.code));

  const addGroup = (group) => {
    if (!group) return;
    onChange([...selected, group]);
  };

  const removeGroup = (code) => {
    const next = selected.filter((group) => group?.code !== code);
    // Clearing the last chip means "stop narrowing", not "applies to nothing".
    onChange(next.length ? next : null);
  };

  return (
    <div style={{ minWidth: "220px" }}>
      {isAllRoles ? (
        <div>
          <span
            style={{
              display: "inline-block",
              padding: "4px 12px",
              borderRadius: "4px",
              backgroundColor: "#EEEEEE",
              fontSize: "13px",
              fontWeight: 500,
            }}
          >
            {t("ORG_ASSIGNMENT_ALL_ROLES")}
          </span>
          {!!options.length && (
            <div style={{ fontSize: "12px", color: "#787878", marginTop: "4px" }}>
              {`= ${describeAllRoles(options)}`}
            </div>
          )}
        </div>
      ) : (
        <div style={{ display: "flex", flexWrap: "wrap", gap: "4px", marginBottom: "4px" }}>
          {selected.map((group) => (
            <RemoveableTag
              key={group.code}
              text={group.name}
              onClick={() => !disabled && removeGroup(group.code)}
            />
          ))}
        </div>
      )}
      {!disabled && !!remaining.length && (
        <Dropdown
          t={t}
          option={remaining}
          optionKey={"name"}
          selected={""}
          select={addGroup}
          placeholder={t("ORG_ASSIGNMENT_NARROW_TO_ROLE")}
        />
      )}
    </div>
  );
};

export default AppliesToSelector;
