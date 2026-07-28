# Modules and dependencies

This page summarizes frontend packages and dependency areas that affect local setup, builds, and deployment. For the full per-module breakdown, see [Micro UI](micro-ui.md) and [Installation UI](installation-ui.md).

## Runtime and build expectations

- Node engine: `>=14`.
- Package manager: Yarn, using workspaces.
- App framework: React `17.0.2`.
- Build system: React Scripts and Webpack.

## Two independent monorepos, each with a workspace/registry split

`frontend/micro-ui/web/micro-ui-internals` and `frontend/installation-ui/web/micro-ui-internals` are separate Yarn monorepos — they don't share packages, and their versions of same-named packages (`core`, `libraries`, `react-components`) have diverged. See the "Independent package tree" / "Local source vs. published dependency" notes on the [Micro UI](micro-ui.md) and [Installation UI](installation-ui.md) pages for the specifics.

Within each monorepo, only the packages listed in that app's `workspaces` field are symlinked from local source — everything else with the same name is a normal npm dependency resolved from `registry.yarnpkg.com` via `yarn.lock`, so local edits to those folders have no effect on a build until published:

| App | Workspaces (symlinked, local edits apply) | Same-named local folders that are NOT workspaces (edits do nothing) |
| --- | --- | --- |
| `frontend/micro-ui/web` | `libraries`, `react-components`, `modules/core`, `modules/im`, `modules/dss`, `modules/hrms` | `modules/common`, `modules/engagement`, `modules/utilities`, `modules/workbench` (resolved from the registry); `modules/bills`, `modules/receipts`, `modules/templates` (not a dependency of any variant at all) |
| `frontend/installation-ui/web` | `modules/*` (i.e. `qc`, `pm`, `amc`, `fa`, `org`, `core`) | `libraries`, `react-components` (no `package.json` present — not valid packages), `css` (resolved from the registry as `@selco/installation-ui-css`) |

If a change to a module doesn't show up when running the app, check this table first before assuming a build/cache problem.

## Shared libraries

Common frontend dependencies include:

- React Router for routing.
- React Query for server-state fetching.
- React Hook Form for form handling.
- React i18next for translations.
- DIGIT UI libraries for platform components and behavior.
- SELCO-specific DIGIT modules for E4H features.
- CSS packages for DIGIT and installation UI styling.

## Main package areas

| Area | Path | Purpose |
| --- | --- | --- |
| Main micro UI | `frontend/micro-ui/web` | Main deployable `/digit-ui` app. |
| Core UI | `frontend/micro-ui/web/core` | Core-focused `/core-ui` app. |
| Workbench UI | `frontend/micro-ui/web/workbench` | Workbench `/workbench-ui` app for MDMS/localisation management. |
| Installation QC UI | `frontend/installation-ui/web` | Installation `/installation-qc` app. |
| Internal libraries (micro-ui) | `frontend/micro-ui/web/micro-ui-internals/packages` | Shared libraries, components, modules, CSS, and SVG components for the micro-ui monorepo. |
| Internal libraries (installation-ui) | `frontend/installation-ui/web/micro-ui-internals/packages` | Separate `qc`/`pm`/`amc`/`fa`/`org`/`core` module workspace for the installation-ui monorepo — not shared with the row above. |

## Build commands

Use commands from the relevant app directory:

- `yarn start`
- `yarn build`
- `yarn build:libraries`
- `yarn build:prod`
- `yarn build:webpack`

Do not run documentation or source scans against `node_modules`; it is generated dependency output and is not part of the GitBook source.
