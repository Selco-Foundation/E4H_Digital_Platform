# Agent instructions — installation-ui (Management Hub)

Read `../AGENTS.md` first. It covers git, PRs, localization, MDMS, the data
layer and verification. This file adds what is specific to this app.

The app is served at `/e4hhub` and covers solar installation and asset
lifecycle management: facility registry, field plans, installation QC
review, AMC, and vendor organizations.

## Modules

All of `packages/modules/*` are live (see `enabledModules` in
`web/src/App.js`). There are no dormant modules here.

| Module | Purpose |
|---|---|
| `core` | Shell: routing, login/OTP, top bar/sidebar, home |
| `pm` | Projects, field plans (installation plans), AMC setup, assessment plans |
| `qc` | Installation report review: approve/reject, audit trail |
| `amc` | AMC visit review |
| `fa` | Facility registry, boundaries, per-facility asset/AMC/activity tabs |
| `org` | Platform and vendor organizations and their users |

- Each module gates itself on a role in its `Module.js`, returning `null`
  for users without it. There are no per-route guards.
- Role codes can change, so they aren't listed here. Read them from the code
  when a requirement needs them, and match the existing checks.
- The same feature often exists in several modules with different depth. For
  example, activity-facility details is read/write in `pm`, review in `qc`,
  and read-only in `fa`. A change to one usually needs the matching change in
  the others.

## Module layout

```
src/
  Module.js        # role gate + component registry (componentsToRegister)
  App.js           # EmployeeAppContainer wrapper
  pages/employee/  # one file per route; routes + breadcrumbs in pages/employee/index.js
  components/      # grouped by owning page: components/<PageName>/Filter.js, InfoCard.js, …
  components/Custom/  # per-module copies of shared helpers (CustomRequest, icons, shared badges)
  hooks/           # useX.js — one React Query hook per entity/query
  services/        # X.js — one XService object per backend API family
  utilities/       # pure functions: mappers, business rules, CommonUtils
  redux/           # actions/ + reducers/ for "working entity" state
  constants/       # e.g. ReduxActions.js, Routes.js, Localization.js, Roles.js
```

Naming:
- `useAssessmentPlan` goes with `AssessmentPlanService`.
- Don't reuse one hook name for two different shapes.
- Rename a component when its job changes enough that the old name misleads.

## DIGIT components

Use existing DIGIT components before writing your own:

- **Wizards:** `FormComposerV2` + `Stepper`.
  - `Stepper` comes from `@egovernments/digit-ui-components`.
  - `FormComposerV2` is imported from both packages: mostly
    `@egovernments/digit-ui-react-components`, sometimes
    `@egovernments/digit-ui-components`. Match the import already used in
    the module or file you're editing.
  - Step configs are keyed by step number.
  - Custom step components are registered by name in `Module.js` and
    referenced as `component: "PMSomething"`.
- **Tables:** `Table` from `@egovernments/digit-ui-react-components`.
  - Columns are `{ id, Header, Cell }`. Each table defines its own local
    `GetCell`/`GetHead` helpers.
  - Per-row styling goes in `getCellProps(cell)`, which receives the
    react-table cell, so `cell.row.original` and `cell.column.id` are
    available.
  - The QC worklist's `selection` (checkbox) column is drawn outside the
    table border. Don't style it as part of the row.
  - Anything added inside a cell must not change the row height. Put it
    inline on the same line, sized to the text line.
- **Multi-select filters:** a single-select `Dropdown` (always given an
  empty `selected` value) plus `RemoveableTag` chips below it. See
  `qc/.../FacilityTable/Filter.js`.
- **Modals:** `PopUp` as the backdrop, with a styled `<div>` dialog: title
  row with a close button, description, content, then right-aligned
  Cancel + primary buttons.
  - Add a `singleAction` prop to hide Cancel for dialogs that only need
    acknowledging.
  - Any action that can't be undone (approve, reject, bulk approve, delete)
    must go through a confirmation modal.
- **Loading:**
  - `Loader` while a page or section is loading.
  - While an action is running, show a full-screen semi-transparent overlay
    around a `Loader`, driven by a `blockUI`/`actionLoading` flag.
- **Feedback:** the `Toast` component, driven by
  `{ key: "success"|"error"|"warning", label }`, dismissed automatically
  with a `setTimeout`.
  - Error text: `CommonUtils.getApiErrorMessage(error) || t("…")`.
  - Use `warning` for partial bulk results. Bulk approve can return HTTP 207
    when only some items succeed.
- **Styles:** existing components use inline style objects with fixed hex
  colours. Status badges are 4px radius, `4px 12px` padding, 13px/500 text.
  Green, amber and red mean status, and orange (`#C84C0E`) means action.
  Don't use those colours for non-status tags.

## Mutation shape

```js
const handleAction = async () => {
  setActionLoading(true);
  try {
    await SomeService.doThing(...);
    await revalidate();
    setToast({ key: "success", label: t("…") });
  } catch (error) {
    console.error("Error doing thing", error);
    setToast({ key: "error", label: CommonUtils.getApiErrorMessage(error) || t("…") });
  } finally {
    setActionLoading(false);
  }
};
```

Add error handling only at real boundaries: API calls and user input.

## State

- **Redux** holds only "working entity" state that has to cross pages
  without prop drilling: `workingProject`, `workingFieldPlan`,
  `workingAssessmentPlan`, and so on. Pages dispatch it once the entity
  loads, and breadcrumbs and headers read it.
  - Give a new flow its own slot. Don't reuse an existing slot that holds a
    different shape.
- **URL query params** (`Digit.Hooks.useQueryParams()` + `history.replace`)
  hold state that must survive a refresh:
  - the wizard step and entity id (`?projectId=…&key=2`);
  - list filters and pagination (`?filter=…&pageSize=…&pageOffset=…`).
- Everything else is local `useState`/`useMemo`.

## List-page filters

- The parent page holds
  `{ entityFilter, entitySearch, entityFilterQuery, entitySearchQuery }`,
  restored from the URL's `filter` param on mount.
- The `Filter` component owns `entityFilter`, the UI selection (`{code,
  name}` objects). It derives `entityFilterQuery` (plain codes for the
  backend) and reports both through one `onFilterChange` call.
- Normalize each field when reading filters back (`filter?.status || []`).
  A filter saved in the URL by an older version can contain `null` fields.
- Cascading filters (district → block, category → type) work like this:
  - Narrow the dependent options on the client.
  - When a parent value is removed, drop child selections that are no longer
    valid.
  - Adding parent values only ever widens the options.
- Boundary and MDMS dropdown data is fetched unscoped (for example
  `useBoundary("State")`) and limited on the client to the project's or
  plan's geography.

## Domain notes

- **Plan names:** a "field plan" and an "installation plan" are the same
  thing. `field_plans.plan_type` separates `FIELD_PLAN` from `ASSESSMENT`.
- **Activity facility data:** one installation at one facility is an
  activity facility, returned from `/activity/v1/activities/_search` as
  `{ facility: [{ activityFacility }], TotalCount }`. Per-installation
  attributes (system type, capacity, BOM pre-fill, feature flags) live in
  `activityFacility.additionalDetails` as a flat object.
- **Field-plan facility data:** facility rows on a field plan carry
  `additionalDetails` as an eGov `{ fields: [{ key, value }] }` bag. Helpers
  that read a flag should accept both shapes.
- **QC actions:** the action bar renders only at `SUBMITTED_BY_SUPERVISOR`.
  Approve sends `APPROVE`; Reject sends `REJECT_AND_ASSIGN_FOR_FIELD_QC`.
- **Background docs:** repo-root `INSTALLATION_FLOW.md` and
  `INSTALLATION_FLOW_REFERENCE.md` (when present) describe the whole flow:
  tables, endpoints, state machine and MDMS masters.

## Running locally

Run commands from `web/micro-ui-internals`.

1. `yarn install` (first time only).
2. Set up `example/.env`: `REACT_APP_PROXY_API`, `REACT_APP_GLOBAL`,
   `REACT_APP_PROXY_ASSETS`, `REACT_APP_USER_TYPE`. The dev backend is
   `https://e4h-dev.selcofoundation.org`.
3. Build the modules. **This is a common trap.**
   - CRA loads each module through `package.json`'s `module` field, which
     points at `dist/index.modern.js`.
   - A module's `yarn build` writes only the CJS `dist/index.js`. After a
     `yarn build`, the app keeps running the old modern bundle, and nothing
     tells you.
   - Fix it by running `npx microbundle-crl --no-compress --format modern,cjs`
     in each module you changed, or by using the root `yarn start` (watch
     mode, both formats).
   - A login crash with "Element type is invalid … check the render method
     of `FieldV1`" means `core`'s modern bundle is stale.
4. Start the dev server: `cd example && DISABLE_ESLINT_PLUGIN=true BROWSER=none yarn start`.
   Without the flag the dev server fails with
   `Failed to load config "react-app"`.
5. Open `http://localhost:3000/e4hhub/employee`. `/` redirects to
   `/digit-ui`, which redirects to the login page.
   - The developer signs in. The agent never enters credentials.
   - API calls are proxied by `example/src/setupProxy.js`. A new backend
     path prefix must be added there, or local calls 404.

### Driving it in a browser (agents)

- The browser must be on the **same machine as the dev server** for
  `localhost` to work.
- If the developer works over remote desktop, a browser extension can be
  connected on their own computer as well as on the host.
  - Use `list_connected_browsers` and pick the host's Chrome.
  - If only the other computer's browser is available, `http://<host-LAN-IP>:3000`
    works, but it needs its own login (sessions are per origin).
- Never submit the login form or tick the Terms/Privacy checkboxes. Ask
  the developer to sign in.
