# Boundary service

## Purpose

The boundary service handles boundary and geography-related platform data. In this repository it is a Spring Boot service generated from Swagger/OpenAPI scaffolding.

## Source location

- Service path: [`backend/core-services/boundary-service`](https://github.com/Selco-Foundation/E4H_Digital_Platform/tree/add-gitbook-docs/backend/core-services/boundary-service)
- README: [`backend/core-services/boundary-service/README.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/core-services/boundary-service/README.md)
- Build file: [`backend/core-services/boundary-service/pom.xml`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/core-services/boundary-service/pom.xml)

## Responsibilities

- Exposes boundary/geography APIs through a Spring Boot server.
- Provides Swagger UI when running locally.
- Supports services and applications that need administrative or geographic boundaries.

## Runtime notes

The README describes the service as a Swagger-generated Spring Boot server. It can be started as a Java application, and Swagger UI is available at `http://localhost:8080/` unless the default port is changed in application properties.

## Operational notes

Use this service when a backend or UI flow depends on boundary lookup, hierarchy, or geography context. Keep API-specific behavior documented in the service README and generated contract files when available.
