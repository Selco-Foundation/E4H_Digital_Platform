# Backend setup

Backend services are Maven-based Java services split between core platform services and E4H domain services.

## Service locations

- Core services: `backend/core-services`.
- E4H services: `backend/e4h-services`.

Each service should be treated as its own application boundary. Many services include a service-level `README.md`, `LOCALSETUP.md`, and `CHANGELOG.md`.

## Typical setup flow

1. Open the service directory.
2. Read its `README.md` and `LOCALSETUP.md` if present.
3. Confirm required configuration and environment variables.
4. Run Maven build/test commands from the service directory.
5. Use the API specifications and schemas under `docs/` when testing integrations.

## Useful backend references

- [Core services](../backend/core-services.md)
- [E4H services](../backend/e4h-services.md)
- [API reference](../backend/api-reference.md)
- [Workflows and crons](../backend/workflows-and-crons.md)

## Notes

The backend contains several independent services. Avoid assuming one command builds or runs the entire backend unless a service-level document explicitly says so.
