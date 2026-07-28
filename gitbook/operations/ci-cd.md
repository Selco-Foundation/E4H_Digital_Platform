# CI/CD

## CI/CD

### CI/CD

CI workflows live under `.github/workflows`.

#### Workflow coverage

The repository includes workflows for:

* Backend services such as asset registry, boundary service, facility registry, field planner, HRMS, ID generation, IM services, inbox, ingestion service, project service, RMS service, vendor registry, and workflow v2.
* Frontend builds for multiple UI variants and state-specific UIs.
* Installation QC and workbench UI.
* SonarCloud analysis.

#### Build configuration

Shared build assets live under:

* `build/build-config.yml`
* `frontend/build/build-config.yml`
* `build/maven`
* `build/maven-java8`
* `build/python`
* `build/adhoc`

#### Documentation guidance

When adding a service or app pipeline:

1. Add or update the workflow under `.github/workflows`.
2. Update service or app README build instructions.
3. Update this GitBook page if the workflow changes platform-level CI/CD behavior.
