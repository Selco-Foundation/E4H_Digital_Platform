# Installation UI

The installation UI lives under `frontend/installation-ui`.

## Purpose

This area contains web UI code for installation quality-control workflows. Use it when working on installation flows that are separate from the main micro UI.

## App facts

Top-level package:

- Path: `frontend/installation-ui`
- Package name: `installation-qc`
- Version: `1.0.0`

Web app package:

- Path: `frontend/installation-ui/web`
- Package name: `micro-ui`
- Version: `1.0.1`
- Node engine: `>=14`
- Homepage: `/installation-qc`
- React: `17.0.2`

## Setup

Start with `frontend/installation-ui/README.md` for app-specific setup and run instructions.

Typical frontend expectations still apply:

- Install dependencies with Yarn.
- Configure required `.env` values.
- Point the UI to the correct backend and asset endpoints.
- Validate against the related backend workflows and API contracts.

## Feature modules

`frontend/installation-ui/web/src/App.js` enables `["QC", "PM", "AMC", "FA", "ORG", "Utilities"]` and passes `allowedUserTypes={["employee"]}` to the `DigitUI` shell — this app has no citizen-facing routes at all, unlike the main micro-ui. The `DigitUI` component itself is lazy-loaded behind a `Suspense`/`Loader` boundary.

Each module lives under `frontend/installation-ui/web/micro-ui-internals/packages/modules/`:

| Directory | Package | Version | What it does |
| --- | --- | --- | --- |
| `qc` | `@selco/digit-ui-module-qc` | `1.0.0` | Installation quality-control review: field-plan table, facility table/details, a "custom request" flow for raising QC actions. Maps to backend facility/field-plan data — see [Health Facility Registry](../backend/core-services/health-facility-registry.md) and [Field Planner](../backend/e4h-services/field-planner.md). |
| `pm` | `@selco/digit-ui-module-pm` | `1.0.0` | Project management: create/view projects, create and list field plans per project, create an AMC from within a project, assign an org user. Maps to [Project Service](../backend/e4h-services/project.md) and [Field Planner](../backend/e4h-services/field-planner.md). |
| `amc` | `@selco/digit-ui-module-amc` | `1.0.0` | AMC (Annual Maintenance Contract) tracking: project table, visit table, visit details. Maps to [AMC Scheduler Service](../backend/e4h-services/amc-scheduler-service.md). |
| `fa` | `@selco/digit-ui-module-fa` | `1.0.0` | Field activity: facility CRUD (including bulk facility add), boundary upload/table, activity details. Maps to [Field Planner Activity](../backend/e4h-services/field-planner-activity.md), [Asset Registry](../backend/e4h-services/asset-registry.md), and [Boundary service](../backend/core-services/boundary-service.md). |
| `org` | `@selco/digit-ui-module-org` | `1.0.0` | Organization management: platform org table, vendor org table, organization details/modal. Maps to [Vendor Registry](../backend/e4h-services/vendor-registry.md). |
| `core` | `@selco/digit-ui-module-core` | `1.8.44` | This app's own copy of the `DigitUI` shell — see the note below on why it's not shared with micro-ui's `core`. |

Each module follows the same internal layout as micro-ui's modules: `Module.js`, `src/pages/employee` (no `citizen` pages — this app is employee-only), `src/components`, plus `redux/`, `constants/`, `hooks/`, `services/`, and `utilities/`.

## Independent package tree — do not confuse with micro-ui

`frontend/installation-ui/web/micro-ui-internals` is its own monorepo, completely separate from `frontend/micro-ui/web/micro-ui-internals`. The two `core` packages have diverged: installation-ui's `core` is at `1.8.44` (adds `allowedUserTypes` route gating, `Otp`/`SignUp-v2`/`Login-v2` pages, a `CustomErrorComponent`) versus micro-ui's `core` at `1.8.0`. A fix made in one tree's `core` package does not automatically apply to the other — port it by hand if it's relevant to both apps.

`frontend/installation-ui/web/package.json` declares its Yarn workspaces as the glob `micro-ui-internals/packages/modules/*` — so `qc`, `pm`, `amc`, `fa`, `org`, and `core` are all real, locally-symlinked workspaces here (unlike micro-ui, where only 4 of its modules are workspaces). But `libraries` and `react-components` folders exist under this tree's `micro-ui-internals/packages/` **without a `package.json`** — they aren't valid packages at all, just leftover source. The app's actual `@egovernments/digit-ui-libraries` (`1.8.19`) and `@egovernments/digit-ui-react-components` (`1.8.24`) dependencies resolve straight from the npm registry per `yarn.lock`, same as `@selco/installation-ui-css` (`1.0.23`). Editing files under those two local folders does nothing.

## Key dependencies

Beyond the modules above, the installation UI package includes:

- `@egovernments/digit-ui-libraries`
- `@egovernments/digit-ui-module-utilities`
- `@egovernments/digit-ui-components`
- `@egovernments/digit-ui-react-components`
- `@egovernments/digit-ui-css`
- `@egovernments/digit-ui-components-css`
- `@selco/installation-ui-css`
- React Router, React Query, React Hook Form, React i18next, and Webpack tooling.

## Scripts

Common scripts include:

- `start`
- `build`
- `build:prepare`
- `build:libraries`
- `build:prod`
- `build:webpack`
- `clean`

## Recent additions

- **Terms/Privacy Policy consent.** This app's `core` module (1.8.44) gained a `PolicyConsentModal.js` that gates login until the user accepts the terms/privacy policy, plus `PolicyDocumentContent.js`, a `usePolicyDocument` hook, a `TermsPrivacyPolicy` page, and a `consentCookies` utility — wired into `Login/login.js`. Implemented independently from the equivalent feature in [Micro UI](micro-ui.md#recent-additions) since the two `core` packages are separate, diverged trees (see the note above).

## Related backend areas

See the per-module backend links in the [Feature modules](#feature-modules) table above for specific service mappings. More generally, installation workflows also touch mobile installation image and asset submission flows — see [Flutter app](../mobile/flutter-app.md) — and should be validated against [API reference](../backend/api-reference.md) for request/response contracts.
