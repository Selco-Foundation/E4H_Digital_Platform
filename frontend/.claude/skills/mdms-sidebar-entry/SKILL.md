---
name: mdms-sidebar-entry
description: Prepare the MDMS records that add a new entry to the employee sidebar in installation-ui or micro-ui (an actions-test record plus one roleactions record per role). Use when a new page needs a sidebar link or a role needs to see an existing one.
---

The sidebar is built from MDMS, not code. Rendering logic:
`core/.../TopBarSideBar/SideBar/EmployeeSideBar.js`.

**Never call MDMS `_create` or `_update` yourself.** Produce ready-to-paste
`data` payloads; the developer applies them through workbench-ui.

1. Ask which environment (dev/staging/prod) to look ids up against. Each has
   its own data.
2. One entry needs:
   - one `ACCESSCONTROL-ACTIONS-TEST.actions-test` record for the entry
     itself;
   - one `ACCESSCONTROL-ROLEACTIONS.roleactions` record per role that should
     see it.
3. The two masters have **independent id sequences**. Fetch each master once
   with the public `POST <host>/egov-mdms-service/v1/_search`, take the max
   `id`, and count up from there locally.
4. Look role codes up in `Organisation.OrgRoles` and show the developer each
   role's code, name and description. Don't ask them to remember codes.
5. For `leftIcon`, check the name is an export of the installed
   `@egovernments/digit-ui-svg-components`. An unknown name renders a blank
   icon and only logs a console warning.
6. Hand over the payloads, saying which environment the ids were taken from.
