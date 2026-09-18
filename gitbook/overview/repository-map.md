# Repository map

This page maps the main folders in the repository and what they are used for.

## Root

- `README.md`: short repository description.
- `.gitbook.yaml`: GitBook configuration for this documentation set.
- `gitbook/`: GitBook source pages and sidebar.
- `.github/workflows/`: CI pipelines for backend services, frontend builds, mobile checks, and code quality.

## Backend

- `backend/core-services/`: shared platform services.
- `backend/e4h-services/`: E4H domain services.
- `backend/docs/`: operational cron manifests and related backend deployment assets.

## Frontend

- `frontend/README.md`: web application overview and setup notes.
- `frontend/micro-ui/`: main DIGIT-based React micro UI.
- `frontend/installation-ui/`: installation-focused UI.
- `frontend/build/`: frontend build configuration and build images.

## Mobile

- `mobile/README.md`: Flutter project starting notes.
- `mobile/lib/`: application source code.
- `mobile/test/`: tests.
- `mobile/android/` and `mobile/ios/`: platform-specific Flutter app projects.
- `mobile/scripts/`: helper scripts.

## Technical documentation assets

- `docs/asset-registry/`: asset-registry API, schemas, SQL, and workflow definitions.
- `docs/facility-registry/`: facility API, master data, SQL, and sequence diagrams.
- `docs/ingestion/`: ingestion schemas and sequence diagrams.
- `docs/project-service/`: project service API definition.
- `docs/ui-sequence-diagrams/`: UI workflow sequence diagrams.

## Build and migration

- `build/`: shared build images and configuration.
- `migration/`: migration scripts, including old ticket subtype migration logic.
