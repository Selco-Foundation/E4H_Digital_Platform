# E4H Digital Platform

This GitBook documents the E4H Digital Platform repository for developers, implementation teams, operators, and product stakeholders.

The platform supports Selco Foundation's Energy for Health program through backend services, web applications, a Flutter mobile application, data ingestion flows, registries, workflows, and operational automation.

## How this documentation works

This repository is the source of truth for documentation. GitBook should be connected to the GitHub repository through Git Sync and configured to read this `gitbook/` directory.

The important files are:

- `.gitbook.yaml` at the repository root, which points GitBook to this folder.
- `gitbook/README.md`, which is the GitBook landing page.
- `gitbook/SUMMARY.md`, which defines the sidebar navigation.

When documentation changes are needed, update the Markdown files in this folder and review them through the normal pull request flow. Existing API specifications, schemas, SQL files, workflow definitions, and sequence diagrams remain in the repository's existing `docs/` directory and are linked from these pages.

## Repository areas

- Backend services live under `backend/core-services` and `backend/e4h-services`.
- Web applications live under `frontend`.
- The Flutter mobile application lives under `mobile`.
- API specifications, schemas, SQL setup files, workflows, and sequence diagrams live under `docs`.
- Build images and shared build configuration live under `build` and `frontend/build`.
- CI pipelines live under `.github/workflows`.
- Operational cron manifests live under `backend/docs`.

## GitBook publishing flow

1. Create a GitBook space.
2. Enable Git Sync with GitHub.
3. Connect `Selco-Foundation/E4H_Digital_Platform`.
4. Select the stable default branch used for documentation publishing.
5. Let GitBook read `.gitbook.yaml`, which sets `gitbook/` as the documentation root.
6. Verify the sidebar matches `SUMMARY.md`.

## Start here

- [Platform overview](overview/platform-overview.md)
- [Repository map](overview/repository-map.md)
- [Local setup](getting-started/local-setup.md)
- [Backend services overview](backend/services-overview.md)
- [API reference](backend/api-reference.md)
