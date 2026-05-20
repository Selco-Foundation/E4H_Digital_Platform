# IM Services Analytics

## Purpose

IM Services Analytics provides analytics support for incident-management data.

## Source location

- Service path: `backend/e4h-services/im-services-analytics`
- README: `backend/e4h-services/im-services-analytics/README.md`
- Build file: `backend/e4h-services/im-services-analytics/pom.xml`

## Responsibilities

- Supports analytics or reporting over IM Services data.
- Provides a Spring Boot service generated from Swagger/OpenAPI scaffolding.
- Exposes Swagger UI locally when running with default configuration.

## Runtime notes

The README describes a Swagger-generated Spring Boot server. Swagger UI is available at `http://localhost:8080/` unless the default port is changed.

## Operational notes

Use this service for dashboard, reporting, or analytics flows related to incident management. Keep reporting assumptions aligned with IM Services status, workflow, and ticket data.
