# Health Facility Registry

## Purpose

The health facility registry service manages facility registry data and exposes facility APIs.

## Source location

- Service path: `backend/core-services/health-facility-registry`
- README: `backend/core-services/health-facility-registry/README.md`
- Build file: `backend/core-services/health-facility-registry/pom.xml`

## Responsibilities

- Provides facility registry APIs.
- Supports facility search, creation, and update flows.
- Supplies facility data to asset, field, mobile, RMS, and ingestion workflows.

## API and schema references

- API specification: `docs/facility-registry/facility-v2-api.yaml`
- Master-data schemas: `docs/facility-registry/master-data-schema`
- SQL schema: `docs/facility-registry/schema/V1__create_facility_registry_schema.sql`
- Sequence diagrams: `docs/facility-registry/sequence-diagrams`

## Runtime notes

The README describes the service as a Swagger-generated Spring Boot server with Swagger UI available at `http://localhost:8080/` by default.

## Operational notes

Facility data is a core dependency for E4H workflows. Verify facility contracts carefully when changing asset submission, RMS mapping, scheduled visits, vendor data, or ingestion flows.
