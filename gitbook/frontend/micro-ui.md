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

Configured Yarn workspaces include:

- `micro-ui-internals/packages/libraries`
- `micro-ui-internals/packages/react-components`
- `micro-ui-internals/packages/modules/core`
- `micro-ui-internals/packages/modules/im`
- `micro-ui-internals/packages/modules/dss`
- `micro-ui-internals/packages/modules/hrms`

## Module dependencies

The main app depends on DIGIT and SELCO packages such as:

- `@egovernments/digit-ui-libraries`
- `@egovernments/digit-ui-module-common`
- `@egovernments/digit-ui-module-engagement`
- `@egovernments/digit-ui-module-utilities`
- `@egovernments/digit-ui-module-workbench`
- `@egovernments/digit-ui-react-components`
- `@selco/digit-ui-module-core`
- `@selco/digit-ui-module-dss`
- `@selco/digit-ui-module-hrms`
- `@selco/digit-ui-module-pgr`
- `@selco/digit-ui-react-components`

## Deployable variants

### Core UI

Path:

- `frontend/micro-ui/web/core`

Package facts:

- Homepage: `/core-ui`
- Node engine: `>=14`
- Includes SELCO core and PGR modules with DIGIT workbench/utilities/react components.

### Workbench UI

Path:

- `frontend/micro-ui/web/workbench`

Package facts:

- Homepage: `/workbench-ui`
- Node engine: `>=14`
- Includes workbench, HRMS, PGR, engagement, core, utilities, and DIGIT React component packages.

Workbench is used to manage MDMS master data and localisation data.

Actual enabled modules may vary by branch and deployment.

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
