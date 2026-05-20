# Vendor Registry

## Purpose

The vendor registry stores organisation records such as vendors, contractors, and community-based organisations.

## Source location

- Service path: `backend/e4h-services/vendor-registry`
- README: `backend/e4h-services/vendor-registry/README.md`
- Local setup: `backend/e4h-services/vendor-registry/LOCALSETUP.md`
- Changelog: `backend/e4h-services/vendor-registry/CHANGELOG.md`

## Responsibilities

- Creates, updates, and searches organisation entities.
- Stores organisation details and contact details.
- Stores tax identifiers and classifications.
- Links organisations to functional areas where they operate.

## Dependencies

The README lists:

- DIGIT backbone services.
- Persister.
- Indexer.
- IDGen.
- Individual.

## API reference

The README links to the Organisation Registry API specification:

- `https://raw.githubusercontent.com/egovernments/DIGIT-Specs/master/Domain%20Services/Works/Organisation-V1.0.0.yaml`

## Operational notes

Vendor data may be consumed by ingestion, facility/asset operations, and field workflows. Keep organisation schema and integration expectations aligned when onboarding new vendor categories.
