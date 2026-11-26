---
name: backend-not-ready
description: Build frontend work in installation-ui or micro-ui ahead of the backend - when an endpoint, a response field or an MDMS master the UI needs doesn't exist yet. Covers where the temporary data goes and how the mock must behave so it is easy to remove.
---

Make the change in one place, mark it as temporary, and make it easy to
remove later. Don't build an abstraction around it.

- **A whole endpoint is missing:** the service method returns static data
  instead of calling `Request`, with a comment marking where the real call
  goes. Switching to the real endpoint should be a change inside that one
  method.
- **A field is missing from a real response** (for example, a flag the
  backend will add later): add a mock module, `utilities/<feature>Mock.js`,
  and apply it to the response inside the one service method that returns
  it. The mock must:
  - have a header saying it is TEMPORARY and what to delete when the backend
    is ready;
  - be switchable off with one constant (`MOCK_<FEATURE> = true`);
  - never overwrite a value the backend actually sent;
  - be deterministic (derive the value from an id, not `Math.random()`), so
    the same record looks the same on every screen.
- **An MDMS master doesn't exist yet:** hardcode the options in
  `constants/<name>.js`, not in a service. Once the master exists, switch to
  the MDMS hook pattern in `frontend/AGENTS.md` and delete the constant.

In the summary, say what is mocked and what to delete once the backend is
ready.
