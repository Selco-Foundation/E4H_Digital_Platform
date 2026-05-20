# Platform overview

The E4H Digital Platform is the support and maintenance application for Selco Foundation's Energy for Health program.

The repository combines backend services, web user interfaces, a Flutter mobile application, data contracts, workflow definitions, and operational automation. It builds on DIGIT platform concepts and adapts them for E4H workflows such as facility management, asset management, ticketing, scheduling, ingestion, and field operations.

## Primary capabilities

- Facility and boundary registry flows for organizing health facilities and administrative geography.
- Asset registry flows for tracking systems, asset types, brands, counts, warranties, and installation workflows.
- Incident management and RMS services for tickets, service requests, and operational follow-up.
- Field planning and scheduled visit flows for implementation and maintenance work.
- Ingestion flows for facility, boundary, and vendor data.
- Web modules for employees and implementation teams.
- Mobile workflows for field teams, asset submission, scheduled visits, installation images, and offline/cache-backed operations.
- CI pipelines and cron manifests for build, deployment, and scheduled backend jobs.

## Audience

This GitBook is written for a mixed audience:

- Developers need setup, service boundaries, API references, and troubleshooting guidance.
- Product and implementation teams need workflow, module, and data-model context.
- Operators need CI/CD, cron, deployment, and operational checks.

## Source material

This documentation summarizes and links to the source files in the repository. For implementation details, use the linked service READMEs, OpenAPI files, JSON schemas, SQL files, workflows, and sequence diagrams.
