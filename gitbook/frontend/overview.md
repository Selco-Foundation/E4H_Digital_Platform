# Frontend overview

The frontend is a React-based implementation built on DIGIT UI patterns. It contains the main micro UI, deployable variants such as Core UI and Workbench UI, and a separate Installation QC UI.

The root frontend README describes the web layer as a React app built on DIGIT UI Core and includes local run instructions, environment variables, libraries, and DIGIT references.

## Main areas

- `frontend/micro-ui`: main micro UI implementation.
- `frontend/installation-ui`: installation-specific UI.
- `frontend/build`: frontend build configuration.

## Deployable UI variants

| Area | Path | Homepage | Notes |
| --- | --- | --- | --- |
| Main Micro UI | `frontend/micro-ui/web` | `/digit-ui` | Main React 17 DIGIT UI application. |
| Core UI | `frontend/micro-ui/web/core` | `/core-ui` | Core-focused deployable variant. |
| Workbench UI | `frontend/micro-ui/web/workbench` | `/workbench-ui` | Workbench-focused deployable variant for MDMS/localisation management. |
| Installation QC | `frontend/installation-ui/web` | `/installation-qc` | Installation quality-control UI. |

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
