---
name: run-app
description: Run installation-ui (Management Hub) or micro-ui (Incident Management) locally against the dev backend and open it in a browser - env setup, rebuilding changed packages in the format the dev server loads, starting the dev server, and the proxy. Use when starting either frontend app or checking a UI change on screen.
---

Run commands from the app's `web/micro-ui-internals`.

## Steps (both apps)

1. `yarn install` (first time only), then set up `example/.env`
   (`REACT_APP_PROXY_API`, `REACT_APP_GLOBAL`, `REACT_APP_PROXY_ASSETS`,
   `REACT_APP_USER_TYPE`). The dev backend is
   `https://e4h-dev.selcofoundation.org`.
2. Rebuild what you changed in **both** formats. **This is a common trap.**
   - CRA loads each package through `package.json`'s `module` field, which
     points at `dist/index.modern.js`.
   - A package's `yarn build` writes only the CJS `dist/index.js`, so the app
     keeps running the old modern bundle and nothing tells you.
   - Run `npx microbundle-crl --no-compress --format modern,cjs` in each
     changed package, or the root `dev:<package>` watch scripts (both
     formats).
3. Start the dev server: `cd example && DISABLE_ESLINT_PLUGIN=true BROWSER=none yarn start`.
   Without the flag it fails with `Failed to load config "react-app"`.
4. API calls are proxied by `example/src/setupProxy.js`. A new backend path
   prefix must be added there, or local calls 404.
5. Use a browser on the same machine as the dev server, so `localhost`
   works. **The developer signs in.** Never type credentials or tick the
   Terms/Privacy checkboxes.

## installation-ui

- Packages to rebuild are the modules under `packages/modules/`. The root
  `yarn start` builds them all, then watches them in both formats.
- A login crash with "Element type is invalid … check the render method of
  `FieldV1`" means `core`'s modern bundle is stale.
- Open `http://localhost:3000/e4hhub/employee`. `/` redirects to
  `/digit-ui`, which redirects to the login page.

## micro-ui

Not yet run in an agent session; check these the first time.

- `REACT_APP_GLOBAL` points at a global config file that is currently shared
  across environments. It may be split per environment later, so read the
  context path and tenant from it rather than hardcoding them. The context
  path is `digit-ui` in the UAT config.
- Packages to rebuild: `libraries`, `react-components`, `modules/im` and/or
  `modules/core`. Watch scripts: `yarn dev:libraries`, `dev:components`,
  `dev:im`, `dev:core`.
