# Frontend overview

The frontend is a React-based implementation built on DIGIT UI patterns. There are two independent frontend apps in the repo:

- `frontend/micro-ui`: the main citizen/employee DIGIT app (issue management, HRMS, dashboards, engagement, billing, workbench).
- `frontend/installation-ui`: an employee-only app for installation quality-control, project/field-plan management, AMC tracking, and vendor/org management.

They are **not** two variants of the same codebase — each has its own `micro-ui-internals` package tree (its own copy of the `core` shell module, `libraries`, `react-components`, and `css` packages). The two trees have diverged: for example `installation-ui`'s `core` module is at version `1.8.44` with employee/citizen access gating (`allowedUserTypes`) and OTP/SignUp v2 pages, while `micro-ui`'s `core` module is at `1.8.0` without those. Do not assume a fix in one tree's `core`/`libraries` automatically applies to the other — it has to be ported by hand.

The root frontend README describes the web layer as a React app built on DIGIT UI Core and includes local run instructions, environment variables, libraries, and DIGIT references. Note the READMEs at `frontend/README.md`, `frontend/micro-ui/README.md`, and `frontend/installation-ui/README.md` are copy-pasted DIGIT-Core boilerplate (they still say "workbench ui" and reference the upstream `DIGIT-Frontend` repo) — treat this GitBook section as the source of truth over those files for anything specific to this repo.

## Main areas

- `frontend/micro-ui`: main micro UI implementation, with its own `micro-ui-internals` package tree.
- `frontend/installation-ui`: installation-specific UI, with its own independent `micro-ui-internals` package tree.
- `frontend/build`: frontend build configuration.

## Deployable UI variants

Each variant is a separate deployable app with its own `App.js` that sets `enabledModules` and wires up module reducers via the `DigitUI` shell component.

| Area | Path | Homepage | Enabled modules | Notes |
| --- | --- | --- | --- | --- |
| Main Micro UI | `frontend/micro-ui/web` | `/digit-ui` | IM, DSS, NDSS, Utilities, HRMS, Engagement, Workbench, PGR | Main React 17 DIGIT UI application; citizen and employee routes both active. |
| Core UI | `frontend/micro-ui/web/core` | `/core-ui` | DSS, NDSS, Utilities, Engagement, IM | Lighter deployable variant; defaults to employee landing. |
| Workbench UI | `frontend/micro-ui/web/workbench` | `/workbench-ui` | DSS, NDSS, Utilities, HRMS, Engagement, Workbench, IM | Workbench-focused variant for MDMS v2 / localisation management. |
| Installation QC | `frontend/installation-ui/web` | `/installation-qc` | QC, PM, AMC, FA, ORG, Utilities | Employee-only (`allowedUserTypes: ["employee"]`); lazy-loads the `DigitUI` shell behind a `Suspense` boundary. |

See [Micro UI](micro-ui.md) for what each module in the first three rows does, and [Installation UI](installation-ui.md) for QC/PM/AMC/FA/ORG.

## Module registration pattern

Both apps follow the same DIGIT UI convention, visible in `frontend/micro-ui/web/src/App.js` and `frontend/installation-ui/web/src/App.js`:

1. `initLibraries()` (from the app's `digit-ui-libraries` package) bootstraps the global `window.Digit` registry and core services (session storage, user service, ULB service, hooks).
2. Each feature module package exports an `init<Name>Components()` function (e.g. `initIMComponents`, `initHRMSComponents`, `initQCComponents`) that registers its routes and components into `window.Digit.ComponentRegistryService`, and an optional `<Name>Reducers(initData)` for Redux state.
3. The app's `App.js` calls every `init<Name>Components()` for the modules it wants active, builds a `moduleReducers` function that combines each module's reducers, and lists the active module names in an `enabledModules` array.
4. The `DigitUI` component (from the app's `core` module) reads `enabledModules`, `moduleReducers`, and `stateCode` (from `STATE_LEVEL_TENANT_ID`) and renders the shell, which internally uses React Router v5 (`Switch`/`Route`) to split into `EmployeeApp` and `CitizenApp` based on the `/employee` or `/citizen` path segment.

A module package that isn't imported anywhere in `App.js` is inert — e.g. in the main micro-ui, the `bills`, `receipts`, and `templates` packages exist under `micro-ui-internals/packages/modules` but `frontend/micro-ui/web/src/App.js` never imports them. `common` is the odd one out: it's imported, but registered directly via `window.Digit.ComponentRegistryService.setupRegistry({ PaymentModule, ...paymentConfigs, PaymentLinks })` rather than through an `init<Name>Components()` / `enabledModules` entry.

## Common frontend stack

- Node `>=14` according to the app package metadata.
- React `17.0.2`.
- React Router `5.3.0`.
- React Hook Form `6.15.8`.
- React Query `3.6.1`.
- React i18next `11.16.2`.
- React Scripts `4.0.1`.
- Webpack and Webpack CLI.
- DIGIT UI libraries and SELCO-specific modules.
- Yarn workspaces.

## Environment configuration

The frontend commonly depends on backend and asset endpoints configured through `.env` values such as:

- `REACT_APP_PROXY_API`
- `REACT_APP_GLOBAL`
- `REACT_APP_PROXY_ASSETS`
- `REACT_APP_USER_TYPE`
- `SKIP_PREFLIGHT_CHECK`

Keep environment-specific values out of committed documentation unless they are safe public examples.

## Build and run scripts

Common scripts from the frontend package files include:

- `yarn start`: runs the local React development server.
- `yarn build`: builds the React app.
- `yarn build:libraries`: builds internal DIGIT UI libraries.
- `yarn build:prod`: runs production Webpack build.
- `yarn build:webpack`: builds libraries and then runs production Webpack.

Check the package file in the specific app directory before running commands because each deployable variant has its own package metadata.
