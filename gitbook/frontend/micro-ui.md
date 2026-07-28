# Micro UI

The micro UI application lives under `frontend/micro-ui`.

## Purpose

The micro UI contains the main DIGIT-based web application modules used by implementation teams and platform users. It is a React 17 application with DIGIT UI packages and SELCO-specific modules.

## Main app

Path:

- `frontend/micro-ui/web`

Package facts:

- Package name: `micro-ui`
- Version: `1.8.0`
- Node engine: `>=14`
- Homepage: `/digit-ui`
- React: `17.0.2`

Configured Yarn workspaces (`frontend/micro-ui/web/package.json`) include:

- `micro-ui-internals/packages/libraries`
- `micro-ui-internals/packages/react-components`
- `micro-ui-internals/packages/modules/core`
- `micro-ui-internals/packages/modules/im`
- `micro-ui-internals/packages/modules/dss`
- `micro-ui-internals/packages/modules/hrms`

The inner monorepo at `micro-ui-internals/package.json` declares its own, slightly different workspace list (`packages/libraries`, `packages/css`, `packages/react-components`, `packages/modules/{core,im,dss,hrms}`, plus an `example` app) and defines the `dev:*`/`build:*` scripts used to work on each package in isolation.

### Local source vs. published dependency — read this before editing a module

Only `core`, `im`, `dss`, `hrms`, `libraries`, and `react-components` are real Yarn workspaces here — Yarn symlinks them from local source, so `yarn.lock` has no resolved entry for them and edits take effect immediately on `yarn start`/`yarn build`.

`common`, `engagement`, `utilities`, and `workbench` are **not** workspaces of `frontend/micro-ui/web`. Even though their source exists locally under `micro-ui-internals/packages/modules/`, the app depends on them as ordinary npm packages — `yarn.lock` resolves each one straight from `registry.yarnpkg.com` (e.g. `@egovernments/digit-ui-module-workbench@1.0.1` resolves to `.../digit-ui-module-workbench-1.0.1.tgz`, not to the local folder). **Editing the local source under these four module folders has no effect on the running micro-ui app** until the package is rebuilt, republished, and the dependency version in `frontend/micro-ui/web/package.json` is bumped (or you manually `yarn link` it for local development).

## Feature modules

All feature modules live under `frontend/micro-ui/web/micro-ui-internals/packages/modules/`. Not every module here is active in every deployable variant — see [Overview](overview.md) for which `enabledModules` each variant turns on.

| Directory | Package | Version | What it does |
| --- | --- | --- | --- |
| `core` | `@selco/digit-ui-module-core` | `1.8.0` | The `DigitUI` shell: citizen/employee route split (`Switch`/`Route` in `App.js`), header/sidebar/topbar, home page composition (`AppModules`), Redux store setup, citizen feedback, error boundaries. |
| `im` | `@selco/digit-ui-module-pgr` | `1.0.10` | Issue management (built on the DIGIT PGR complaint pattern): employee inbox (desktop/mobile), complaint form composer, complaint timeline, RMS-paused banners. Exposes `initIMComponents` / `IMReducers`. |
| `hrms` | `@selco/digit-ui-module-hrms` | `1.0.1` | Employee records: create/edit employee, employee details, HR inbox. |
| `dss` | `@selco/digit-ui-module-dss` | `1.0.4` | Dashboards: overview/drill-down pages, bar/pie/area/horizontal-bar charts, an India topojson map view, date-range and national filters. |
| `engagement` | `@egovernments/digit-ui-module-engagement` | `1.8.0` | Citizen/employee engagement: events, messages, surveys, document create/update flows. |
| `bills` | `@egovernments/digit-ui-module-bills` | `1.7.0-beta.2` | Billing: group bill and bill-cancel flows, bill inbox/search. Present in the package tree but not imported by the main app's `App.js`. |
| `receipts` | `@egovernments/digit-ui-module-receipts` | `1.7.0-beta.2` | Payment receipts: receipt inbox, receipt details, acknowledgement, cancellation. Also not imported by the main app's `App.js`. |
| `common` | `@egovernments/digit-ui-module-common` | `1.8.0` | Shared payment components (`PaymentModule`, `PaymentLinks`, `paymentConfigs`) registered directly via `ComponentRegistryService.setupRegistry`, plus form-composer HOCs (`subform-composer`). Consumed as a published dependency, not a workspace — see below. |
| `workbench` | `@egovernments/digit-ui-module-workbench` | `1.0.1` | MDMS v2 and localisation management: JSON-schema-driven forms, localisation search, bulk edit modal, XLSX export. Consumed as a published dependency, not a workspace. |
| `utilities` | `@egovernments/digit-ui-module-utilities` | `1.0.0` | Utility module package; has a `package.json` but no `src` in this checkout — treat as a thin/placeholder dependency until populated. Consumed as a published dependency, not a workspace. |
| `templates` | *(no package.json — plain source folder)* | — | A single shared `ApplicationDetails` page template (config-driven detail view + modal), not published as its own npm package and not a dependency of any of the three micro-ui app variants. |

Modules that ship UI generally follow the same internal layout: `Module.js` (registration entry point), `pages/employee` and `pages/citizen`, `components/`, and module-specific `redux/`, `constants/`, or `utils/`.

## Shared packages

Also under `micro-ui-internals/packages/`, used across the feature modules above:

| Directory | Package | Version | Purpose |
| --- | --- | --- | --- |
| `libraries` | `@egovernments/digit-ui-libraries` | `1.8.1` | `initLibraries()` bootstrap; cross-module data hooks (`hooks/bills`, `hooks/pgr`, `hooks/im`, `hooks/receipts`, `hooks/engagement`, `hooks/core`, `hooks/hrms`, `hooks/dss`, `hooks/surveys`, `hooks/events`, `hooks/billAmendment`); API service primitives (`services/atoms`, `services/molecules`, `services/elements`); translations; enums; domain utils (`utils/pt`, `utils/obps`, `utils/fsm`, `utils/dss`, `utils/Analytics`). |
| `react-components` | `@selco/digit-ui-react-components` | `1.8.0` | SELCO's shared component library (`atoms`, `molecules`, `hoc`), with Storybook stories under `src/stories`. |
| `svg-components` | `@egovernments/digit-ui-svg-components` | `1.0.0` | Shared SVG/icon components. |
| `css` | *(styling package, no separate published name found in this checkout)* | — | DIGIT v2 and page/component styling (`src/digitv2`, `src/pages`, `src/components`), plus shared images/SVGs. |
| `config` | *(shared build config)* | — | `config/index.js` consumed by the app-level `webpack.config.js` files. |

**Important:** this is a separate, independently-versioned package tree from the one under `frontend/installation-ui/web/micro-ui-internals` — see the note in [Overview](overview.md). Don't assume a change here is also present in the installation UI's copy, or vice versa.

## Deployable variants

`frontend/micro-ui/web/core` (`/core-ui`) and `frontend/micro-ui/web/workbench` (`/workbench-ui`) are separate deployable entry points that reuse the same `micro-ui-internals` package tree but ship their own `App.js`, `webpack.config.js`, `Dockerfile`, and `nginx.conf` — each is built and containerized independently of the main `/digit-ui` app. See the enabled-modules table in [Overview](overview.md) for exactly which modules each variant activates; Workbench's distinguishing module is `workbench` itself, used to manage MDMS v2 master data and localisation data.

Actual enabled modules may vary by branch and deployment — treat `App.js` in each directory as the source of truth.

## Setup

Use the setup instructions in `frontend/README.md` and the README inside `frontend/micro-ui`.

Typical steps:

1. Open the micro UI web directory.
2. Install dependencies with Yarn.
3. Add the required `.env` file.
4. Start the local development server.

## Scripts

Common scripts include:

- `start`
- `build`
- `build:prepare`
- `build:libraries`
- `build:prod`
- `build:webpack`
- `clean`

## Documentation updates

When adding or changing a web module, update:

- The module README or nearest frontend README.
- This GitBook page if the change affects platform-level navigation or setup.
- Any UI sequence diagrams under `docs/ui-sequence-diagrams` if user flow behavior changes.
