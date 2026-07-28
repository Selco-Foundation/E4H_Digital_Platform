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

## Key dependencies

The installation UI package includes:

- `@egovernments/digit-ui-libraries`
- `@selco/digit-ui-module-core`
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

## Related backend areas

Installation workflows may touch:

- Asset registry.
- Facility registry.
- Field planning.
- Mobile installation image and asset submission flows.

Use API and schema references from [API reference](../backend/api-reference.md).
