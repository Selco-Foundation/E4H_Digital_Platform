# Frontend setup

The frontend applications are React applications based on DIGIT UI patterns.

## Main locations

- `frontend/micro-ui`: main micro UI implementation.
- `frontend/installation-ui`: installation-focused UI.
- `frontend/README.md`: frontend overview and setup notes.

## Required tooling

- Node `>=14`.
- Yarn.
- Access to backend and asset endpoints for the target environment.

## Typical setup flow

1. Open the relevant frontend app directory.
2. Install dependencies with Yarn.
3. Create the required `.env` file from the sample or README guidance.
4. Configure backend URLs with environment variables.
5. Start the app with the local start command documented by the app.

## Common environment variables

The frontend README references variables such as:

- `REACT_APP_PROXY_API`
- `REACT_APP_GLOBAL`
- `REACT_APP_PROXY_ASSETS`
- `REACT_APP_USER_TYPE`
- `SKIP_PREFLIGHT_CHECK`

Confirm exact values with the environment you are targeting.

## References

- [Frontend overview](../frontend/overview.md)
- [Micro UI](../frontend/micro-ui.md)
- [Installation UI](../frontend/installation-ui.md)
- [Modules and dependencies](../frontend/modules-and-dependencies.md)
