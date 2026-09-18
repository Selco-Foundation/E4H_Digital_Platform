# Ingestion

Ingestion assets live under `docs/ingestion`.

## Schema files

- `docs/ingestion/schema/BoundaryIngestionSchema.json`
- `docs/ingestion/schema/FacilityIngestionSchema.json`
- `docs/ingestion/schema/VendorIngestionSchema.json`

## Sequence diagrams

- Boundary ingestion: `docs/ingestion/sequence-diagrams/boundary-ingestion-seq.txt`
- Facility ingestion: `docs/ingestion/sequence-diagrams/facility-ingestion-seq.txt`
- Vendor ingestion: `docs/ingestion/sequence-diagrams/vendor-ingestion-seq.txt`

PNG versions of these diagrams are stored in the same directory.

## Related services

- `backend/e4h-services/ingestion-service`
- Facility registry services.
- Vendor registry services.
- Boundary service.

## Update guidance

When ingestion behavior changes, update the schema, sequence diagram, ingestion service README, and this page if the documented flow changes.
