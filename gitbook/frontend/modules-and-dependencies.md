# Modules and dependencies

This page summarizes frontend packages and dependency areas that affect local setup, builds, and deployment.

## Runtime and build expectations

- Node engine: `>=14`.
- Package manager: Yarn.
- App framework: React `17.0.2`.
- Build system: React Scripts and Webpack.
- Workspaces: used by the micro UI internal packages.

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
| Internal libraries | `frontend/micro-ui/web/micro-ui-internals/packages` | Shared libraries, components, modules, CSS, and SVG components. |

## Build commands

Use commands from the relevant app directory:

- `yarn start`
- `yarn build`
- `yarn build:libraries`
- `yarn build:prod`
- `yarn build:webpack`

Do not run documentation or source scans against `node_modules`; it is generated dependency output and is not part of the GitBook source.
