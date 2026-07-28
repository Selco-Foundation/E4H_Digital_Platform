# Ingestion Service

## Purpose

The ingestion service is a Python project for ingestion-driven processes in asset management.

## Source location

- Service path: `backend/e4h-services/ingestion-service`
- README: `backend/e4h-services/ingestion-service/README.md`
- TODO: `backend/e4h-services/ingestion-service/TODO.md`

## Responsibilities

- Runs ingestion processes for asset-management workflows.
- Uses repository schemas and sequence diagrams for ingestion contracts.
- Connects to relevant services during execution.

## Runtime and setup

The README describes the service as a `uv` managed Python project.

Typical run flow:

1. Install `uv`.
2. Run `uv sync` from the project root that contains `uv.lock`.
3. Ensure `.env` contains the correct values.
4. Port-forward relevant services from Kubernetes.
5. Run `uv run -m app.main` from the root folder.

## Schema references

- Boundary ingestion schema: `docs/ingestion/schema/BoundaryIngestionSchema.json`
- Facility ingestion schema: `docs/ingestion/schema/FacilityIngestionSchema.json`
- Vendor ingestion schema: `docs/ingestion/schema/VendorIngestionSchema.json`

## Sequence diagrams

- Boundary ingestion: `docs/ingestion/sequence-diagrams/boundary-ingestion-seq.txt`
- Facility ingestion: `docs/ingestion/sequence-diagrams/facility-ingestion-seq.txt`
- Vendor ingestion: `docs/ingestion/sequence-diagrams/vendor-ingestion-seq.txt`

## Operational notes

Ingestion jobs usually require correct environment variables, service access, and schema compatibility. Validate sample payloads against the schema before running against shared environments.
