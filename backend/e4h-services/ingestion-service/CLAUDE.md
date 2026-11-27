# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

The only **Python** service in the backend: a FastAPI app for Excel-driven bulk operations. It generates Excel templates pre-filled from live data, validates uploaded workbooks, and ingests rows by calling the other backend services' REST APIs. It covers vendors, boundaries, facilities, projects, field plans, staff/supervisors, AMC configuration, ICC reports, legacy tickets and facility QR codes. It has no business logic of its own beyond validation and mapping. The Java services own the data.

## Commands

```bash
uv sync                  # install deps (Python >= 3.12, uv.lock is the source of truth)
uv run -m app.main       # run on :8080
uv add <package>         # add a dependency (updates pyproject.toml + uv.lock)
```

Running needs a `.env` with the service URLs (`MDMS_URL`, `BOUNDARY_SERVICE_URL`, `FACILITY_SERVICE_URL`, `PROJECT_SERVICE_URL`, `FIELDPLAN_SERVICE_URL`, `FIELDPLAN_ACTIVITY_SERVICE_URL`, `HRMS_SERVICE_URL`, `USER_SERVICE_URL`, `IM_SERVICES_URL`, `AMC_SCHEDULER_SERVICE_URL`, `VENDOR_SERVICE_URL`, `FILESTORE_SERVICE_URL`, `LOCALIZATION_SERVICE_URL`), DB settings (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`), `TIME_OUT` and `ENVIRONMENT`. Point them at `kubectl port-forward`ed services. Unit tests live in `tests/` and use the standard library only: `.venv/bin/python -m unittest discover -s tests -t .` from the service root. `Ingestion.postman_collection.json` is the API test artifact.

## Structure

- `app/main.py` creates the FastAPI app; `app/api/routes.py` mounts four routers under `/ingestion-service/`:
  - `ingest/*` → `app/api/endpoints/file_ingestion.py` (upload, validate, ingest)
  - `template/*` → `template_generation.py` (download Excel templates, QR generation)
  - `document/*` → `document_merge.py` (append images/PDFs to a parent PDF in filestore)
  - `health/*` → `health_check.py`
- Endpoint files are very large (`file_ingestion.py` is about 5,000 lines). Grep for the route path (e.g. `'/facilities'`) instead of reading the whole file.
- `app/ingest/` handles Excel loading and writing (`excel_data_loader.py`, `excel_data_writer.py`, `boundary_excel_data_loader.py`) and the validators (required fields, patterns, identifiers, boundary codes and hierarchy).
- `app/processor/` holds per-entity processors created through factories (`boundary_data_processor`, `vendor_data_processor`).
- `app/utils/*_service_client.py` contains one HTTP client per backend service (facility, project, fieldplan, fieldplan-activity, HRMS, IM, AMC, boundary, filestore, localization, organization/vendor, MDMS). They are built on `app/utils/http_client.py` (async `httpx`). Add calls to the existing client for a service rather than calling `httpx`/`requests` inline.
- `app/schemas/` holds Pydantic models, including DIGIT `RequestInfo`, user and role.
- `app/config/` holds static config: ICC templates, `tenant_creator_mapping.json`, `user_profiles.json`.

## Conventions

- Requests carry a DIGIT `RequestInfo` as a JSON string form field next to the uploaded file (`request_info: str = Form(...)`, parsed with `request_info_from_json`). Role checks use `app/decorators/rbac_validator.py#get_authorized_request_info`, called directly, against `app/core/user_role.py#UserRole`. It is active on some template endpoints but commented out on the `ingest/*` endpoints.
- Ingestion endpoints return the processed workbook with validation results as a `FileResponse`, not a JSON error list. Keep that pattern for new ingestion types.
- Data is written through the owning service's API. There are no direct DB writes; `psycopg`/SQLAlchemy is only for lookups. Kafka (`app/producer/producer.py`) is used only to send SMS (`egov.core.notification.sms`).
