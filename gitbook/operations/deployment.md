# Deployment

Deployment details are spread across service-level configuration, build files, workflow files, and operational manifests.

## Deployment-related areas

- `.github/workflows`: CI and deployment automation.
- `build`: shared backend build images and configuration.
- `frontend/build`: frontend build configuration.
- `backend/docs`: cron manifests and operational YAML files.
- Service directories: service-level Docker, Maven, or setup files where present.

## Cron deployment assets

Scheduled jobs and backend automation manifests live in `backend/docs`.

Review [Workflows and crons](../backend/workflows-and-crons.md) for the current index.

## Recommended release documentation

Each deployment should identify:

- Services or apps changed.
- API/schema changes.
- Database or master-data changes.
- Workflow or cron changes.
- Required environment variables.
- Rollback notes.

Keep sensitive values out of committed documentation.
