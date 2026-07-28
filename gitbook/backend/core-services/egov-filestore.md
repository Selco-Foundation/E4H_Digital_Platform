# eGov Filestore

## Purpose

The filestore service provides file upload and download capability for other platform modules.

## Source location

- Service path: `backend/core-services/egov-filestore`
- README: `backend/core-services/egov-filestore/README.md`
- Local setup: `backend/core-services/egov-filestore/LOCALSETUP.md`
- Changelog: `backend/core-services/egov-filestore/CHANGELOG.md`

## Responsibilities

- Accepts file uploads from platform services and applications.
- Stores files through a configured backend such as AWS S3, Azure, MinIO, or local filesystem.
- Returns encrypted download URLs for stored files.
- Creates additional thumbnails for image uploads.
- Validates uploaded file formats.

## API surface

Documented APIs include:

- `/v1/files`: validates and uploads files.
- `/v1/files/url`: returns encrypted URLs for file UUIDs.

For image files, URL lookup can return multiple comma-separated URLs for generated thumbnail variants. For non-image files, one URL is returned.

## Dependencies and configuration

The service must have at least one storage backend configured. The README notes that the application starts successfully only when one of Azure, AWS, MinIO, or filesystem storage is enabled.

## Operational notes

Use this service for assets such as installation images, documents, videos, and other uploads that must be shared across backend, frontend, and mobile workflows.
