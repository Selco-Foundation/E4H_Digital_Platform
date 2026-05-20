# Zuul Gateway

## Purpose

Zuul provides gateway and routing support for backend services.

## Source location

- Service path: `backend/core-services/zuul`

## Responsibilities

- Routes client or service traffic to backend services.
- Centralizes gateway behavior for service endpoints.
- Supports deployment layouts where services are exposed behind a common entry point.

## Operational notes

When adding a new backend service endpoint that must be exposed through the platform gateway, check the Zuul configuration and deployment routing. Keep route changes aligned with frontend and mobile endpoint configuration.
