# Agent instructions — E4H frontend

Read this before changing anything under `frontend/`. It records conventions
that aren't obvious from the code, so every agent (Claude or otherwise) and
every developer stays consistent with what's already there.

This file covers practices **shared by both apps**. Each app has its own file
with app-specific details — read it as well:

- `installation-ui/AGENTS.md` — Management Hub (`/e4hhub`): QC, PM, AMC, FA, ORG.
- `micro-ui/AGENTS.md` — Incident Management (`im`) app.

Where an app file contradicts this one, the app file wins for that app.

## Stack (both apps)

DIGIT/eGov micro-frontend monorepos: React 17, react-router-dom 5,
react-query 3, react-hook-form 6, react-i18next, Redux (per-module reducers),
Create React App (react-scripts 4), yarn workspaces. Feature code lives in
`web/micro-ui-internals/packages/modules/<module>/src/`. Each module is its
own package, built with `microbundle-crl` into `dist/`.

There are **no unit tests and no working ESLint** in either app today. The
only automated check is a successful module build — see
[Verifying a change](#verifying-a-change).

## Module boundaries

- **Never import from another module's package.** `pm` must not import from
  `qc`, and so on. The DIGIT convention here is that each module carries its
  own copy of shared helpers (`CustomRequest.js`, `CommonUtils.js`,
  `installationImages.js`, the Filestore/Boundary services). Follow it.
- When the same new component or helper is needed in several modules, put an
  **identical copy** in each, at the same path, with a first-line comment
  naming the other copies ("Identical copies live in pm, qc and fa … keep them
  in sync"). Check they are byte-identical (`md5sum`) before committing.
- Before creating a component in a module, check whether that module already
  has an identical one, and reuse it.
- Shared runtime services come from the `Digit` global (`Digit.UserService`,
  `Digit.ULBService`, `Digit.Hooks.useCustomMDMS`, `Digit.SessionStorage`,
  `Digit.ComponentRegistryService`), not from imports between modules.

## Data layer: services, hooks, utilities

The current code is mixed: some services reshape responses and others return
them raw. **New code** follows this split. Don't refactor existing code to
match unless asked.

- **`services/X.js`** exports one `XService` object with one async method per
  endpoint. A method takes the criteria, makes one call through `Request`
  (`@egovernments/digit-ui-libraries`), and returns the response as the
  backend sent it. Don't call another service from inside a service.
  `Request` injects `RequestInfo` itself, so don't build it by hand.
- **`hooks/useX.js`** wraps `useQuery`. It builds the criteria, calls the
  service, and maps the raw response into the shape the UI renders, using
  mapper functions from `utilities/`. It returns
  `{ isLoading, isFetching, isError, error, data, revalidate }`.
  - Query key: `["CACHE_KEY", ...every param that should trigger a refetch]`.
  - Two hooks that return different shapes get different cache keys, even if
    they call the same endpoint.
  - Gate a query with `enabled: !!requiredParam`, not with a check inside the
    query function.
- **`utilities/*.js`** holds pure functions only: no React, no network.
  Response mappers, business rules (eligibility, status transitions), and
  formatters belong here.
- Upload, download and multipart (ingestion-service) calls go through the
  module's `CustomRequest`, not `Request`. For `responseType: "blob"`, a
  failed call's error body is also a Blob: read it with
  `CommonUtils.getBlobApiErrorMessage` (where the module has it), not
  `getApiErrorMessage`.
- **Response envelopes differ between services** (`{FieldPlans: [...]}`,
  `{plan: {...}}`, `{facility: [...], TotalCount}`, …). Confirm the real key
  from a sample response or the API spec before writing the extraction. Don't
  write a fallback chain like `res?.plan || res?.Plan || res?.assessmentPlan`.
  If the shape really is unknown yet, read one key and leave a
  `// TODO: confirm response envelope` comment.
- Dates go to the backend as epoch millis (`new Date(v).getTime()`) and are
  formatted for display with a local `formatDate` helper.

## Backend not ready yet

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
  the MDMS hook pattern below and delete the constant.

## MDMS

- **Reading a master:** use `Digit.Hooks.useCustomMDMS(tenantId, moduleName,
  [{ name: masterName }], { select, enabled: !!tenantId })` in the component,
  or the module's `useMDMS` hook where one exists. Map raw rows into the
  shape you need in `select` or in a `utilities/` mapper.
- **Rendering from an MDMS schema** (form schemas, BOM schemas, image
  criteria): don't hardcode the field list. Match the record to its schema by
  an explicit discriminator (for example `formType`), walk the schema
  (`pages` → `properties`, sorted by `order`), and show only fields that
  actually have data. Put the walking logic in `utilities/`.
- **Resolve enum values** (`enums: [{code, name}]`) from code to name when
  you read the data, not when you render it.
- **Writing MDMS data** (new masters, sidebar entries, role actions): never
  call `_create` or `_update` yourself. Generate a ready-to-paste `data`
  payload and let the developer apply it through their normal process
  (workbench-ui). Ask which environment (dev/staging/prod) to look ids up
  against, because each has its own data.
- **Adding a sidebar entry:** the sidebar is built from MDMS, not code. Use
  the `mdms-sidebar-entry` skill (`.claude/skills/mdms-sidebar-entry/`).

## Localization

Labels come from the localization service, in modules per app
(`rainmaker-common`, `rainmaker-<module>`, `rainmaker-<tenant>`, …).

- **Wrap every user-facing string in `t("KEY")`**, with one exception: bare
  `label`, `heading` and `description` strings inside a `FormComposerV2` step
  config. `FormComposerV2` translates those itself.
- **Never pass raw data through `t()`**: dates, times, free text, numbers,
  names. i18next's namespace separator is `:`, so `t("19:02")` returns
  `"02"`. This has broken production rendering before.
- **MDMS enum option names** (a finite, curated vocabulary) may go through
  `t()`, but mark them at the point they're resolved (for example
  `translateValue: true`) and branch on that flag when rendering. Free-text,
  date and numeric MDMS fields never go through `t()`.
- **Adding, reusing or debugging keys** (which bundle, key prefixes, a key
  showing raw): use the `localization-keys` skill
  (`.claude/skills/localization-keys/`).

## Git

- Commit message: `type: short description`, using `feature:`, `fix:`,
  `refactor:`, `simplify:`, `docs:` or `chore:` (lowercase, colon, space).
- One logical change per commit. Split bundled fixes into separate commits,
  even when they ship in one PR.
- Amend or force-push only when asked, and say first that a force-push will
  be needed.
- Before finalizing a feature, remove dead code **the feature itself
  introduced** (superseded helpers, placeholder data, exports nothing calls).
  Grep the whole repo for references before deleting. Don't clean up
  unrelated existing code in the same diff.
- Never commit `.idea/`, `.vscode/`, build output (`dist/`), or local notes.
- Stage paths explicitly rather than `git add -A`. The working tree carries
  untracked notes and IDE directories on purpose.

## Pull requests and promotion

Branches: `develop` (integration) → `staging` (UAT) → `main` (production).

- **New work:** branch off `develop` as `feature/<short-desc>` or
  `fix/<short-desc>`, and open the PR against `develop`.
  - Title: short, prefixed with the module or area, for example
    `QC: show rectification badge on worklist`.
  - Body: a `## Summary` section of plain bullet points. No test-plan
    section.
- **Promoting to `staging`, then `main`:** cherry-pick onto a fresh branch off
  the target, never retarget the original PR. Use the `promote-pr` skill
  (`.claude/skills/promote-pr/`).
- `gh` usage (reading PRs and CI, replying to review comments, the
  `gh pr edit` workaround) is in the repo-wide `github` skill.

## Dependencies

- **Never add a third-party dependency without asking first**, npm or
  otherwise. This holds even when another module already uses the package, and
  even when it is small or dev-only.
- Say what it is, what it buys, and what the alternative costs without it —
  then wait. "Another module already has it" is not approval.
- Prefer something already in the workspace. Each module is its own package, so
  a new dependency tends to spread to the others that copy the same helper.

## Verifying a change

- **Build every module you touched** and fix any errors before reporting the
  change as done. The build (Babel async transforms under `microbundle`) has
  caught real bugs that review missed.
- For UI changes, **run the app and look at the screen** you changed. A
  successful build doesn't show layout problems. App-specific run steps are
  in each app's `AGENTS.md`.
- Say what you verified and what you didn't. For example: "built pm/qc/fa;
  checked the QC worklist in the browser; PM screens not viewed".
- Prefer checking a claim against something real — the actual file, the
  running app, a real API response — over reasoning about what the code
  should do.

## Running locally (both apps)

Run commands from the app's `web/micro-ui-internals`. Each app's `AGENTS.md`
gives its `.env` values, context path and the packages to rebuild.

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
   works. The developer signs in (see
   [Safety with shared systems](#safety-with-shared-systems)).

## Safety with shared systems

- Never write to shared environments yourself: MDMS, localization,
  workflow configs, or the dev/UAT databases. Prepare the payload and hand
  it to the developer.
- Never type credentials into login forms, and never accept Terms or
  Privacy checkboxes on the developer's behalf. Ask the developer to sign
  in.
