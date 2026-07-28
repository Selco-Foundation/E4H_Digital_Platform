# Local setup

This repository contains multiple applications and services. Set up only the area you need unless you are doing cross-platform integration work.

## Prerequisites

Common tools used across the repo:

- Git.
- Java and Maven for backend services.
- Node.js and Yarn for frontend applications.
- Flutter for the mobile application.
- Docker if you need to build container images or run containerized dependencies.

Check service-level `README.md` and `LOCALSETUP.md` files for exact versions, environment variables, and dependency requirements.

## Recommended first steps

1. Clone the repository.
2. Check the branch you are working from.
3. Review the root `README.md`.
4. Pick the application area:
   - Backend: start with [Backend setup](backend-setup.md).
   - Frontend: start with [Frontend setup](frontend-setup.md).
   - Mobile: start with [Mobile setup](mobile-setup.md).
5. Use the service or app README closest to the code you are changing.

## Documentation workflow

Documentation changes are made in `gitbook/`. When adding a page:

1. Create the Markdown file under the appropriate section.
2. Add it to `gitbook/SUMMARY.md`.
3. Link to existing source files rather than duplicating long API specs or schemas.
4. Open a pull request for review.
