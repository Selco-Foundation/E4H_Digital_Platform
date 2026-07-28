# Troubleshooting

## Troubleshooting

### Troubleshooting

Use this page as the first checklist when local setup, integration, or deployment behavior is unclear.

#### Backend

* Check the service-level `README.md` and `LOCALSETUP.md`.
* Confirm required environment variables and external dependencies.
* Verify API contracts under `docs`.
* Check related workflow or cron manifests if behavior is scheduled or asynchronous.

#### Frontend

* Confirm Yarn dependencies are installed in the correct app directory.
* Check `.env` values for backend and asset endpoints.
* Confirm `REACT_APP_USER_TYPE` is correct for the flow being tested.
* Compare UI behavior with sequence diagrams under `docs/ui-sequence-diagrams` where available.

#### Mobile

* Run dependency resolution from the `mobile` directory.
* Confirm Flutter SDK and platform tooling are installed.
* Validate backend endpoint and environment configuration.
* Test cache-backed workflows on a device or emulator when changing field flows.

#### Documentation

* If a GitBook page does not appear, check the sidebar structure.
* If GitBook sync fails, check `.gitbook.yaml` and the configured branch.
* If a link breaks, verify the path relative to the page location.
