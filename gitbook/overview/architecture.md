# Architecture

The repository is organized as a multi-application platform with backend services, web frontends, a Flutter mobile app, and shared data/API assets.

## High-level structure

```text
Users and field teams
  -> Frontend React applications
  -> Flutter mobile application
  -> Backend core services and E4H services
  -> Registries, workflows, MDMS/master data, ingestion, and operational jobs
```

## Backend layer

Backend services are Java/Maven services grouped into:

- `backend/core-services`: shared platform services such as boundary, filestore, ID generation, MDMS, SMS notification, workflow, facility registry, and gateway support.
- `backend/e4h-services`: E4H domain services such as asset registry, RMS, ingestion, project, field planning, HRMS, inbox, analytics, vendor registry, AMC scheduling, and processor services.

Many services include their own `README.md`, `LOCALSETUP.md`, or `CHANGELOG.md`.

## Frontend layer

The frontend is based on DIGIT UI patterns and React/Yarn projects under `frontend`.

- `frontend/micro-ui` contains the main micro UI implementation.
- `frontend/installation-ui` contains installation-related web UI.
- `frontend/build` contains frontend build configuration.

## Mobile layer

The mobile app is a Flutter project under `mobile`. Its code is grouped around BLoCs, repositories, models, pages, routing, utilities, and widgets.

The app includes modules for auth, localization, asset submission, scheduled visits, installation images, summaries, MDMS-backed data, and local cache flows.

## Data and integration layer

Shared technical contracts live under `docs`:

- OpenAPI specifications.
- Master-data JSON schemas.
- SQL schema files.
- Workflow definitions.
- Sequence diagrams for backend and UI flows.

These assets are referenced from this GitBook rather than moved.
