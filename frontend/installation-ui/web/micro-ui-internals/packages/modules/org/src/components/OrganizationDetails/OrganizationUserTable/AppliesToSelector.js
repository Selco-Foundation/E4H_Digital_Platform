import React from "react";
import { MultiSelectDropdown } from "@egovernments/digit-ui-react-components";
import { appliesToAllRoles, describeAllRoles } from "../../../utilities/jurisdictionRoles";

/**
 * "Applies to" — which of the user's roles one jurisdiction scopes.
 *
 * The same MultiSelectDropdown the user form uses for the roles field, so picking roles
 * works the same way in both places.
 *
 * `value` is a list of OrgRoleGroups, or null/empty for "every role the user holds". The two
 * are different things and the dropdown alone cannot say which it is: an empty multiselect
 * reads as *none*, the exact opposite. So while nothing is picked, the unscoped state is
 * spelled out as an `All roles` chip with the roles it currently resolves to underneath.
 *
 * Narrowing is a live rule, not a snapshot — `All roles` keeps meaning "whatever the user
 * holds", so granting a role later widens every unscoped jurisdiction with it.
 */
const AppliesToSelector = ({ t, value, options = [], onChange, disabled = false }) => {
  const selected = value || [];
  const isAllRoles = appliesToAllRoles({ roles: selected });

  return (
    <div style={{ minWidth: "220px" }}>
      {isAllRoles && (
        <div style={{ marginBottom: "8px" }}>
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
      )}
      {!disabled && (
        <MultiSelectDropdown
          options={options}
          optionsKey={"name"}
          selected={selected}
          onSelect={(e) => {
            const picked = (e || []).map((row) => (row?.[1] ? row[1] : null)).filter(Boolean);
            // Clearing the last one means "stop narrowing", not "applies to nothing".
            onChange(picked.length ? picked : null);
          }}
          defaultLabel={t("ORG_ASSIGNMENT_NARROW_TO_ROLE")}
          defaultUnit={t("ORG_ASSIGNMENT_ROLES_SELECTED")}
          config={{ isDropdownWithChip: true }}
        />
      )}
    </div>
  );
};

export default AppliesToSelector;
