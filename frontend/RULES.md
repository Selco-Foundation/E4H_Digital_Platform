# Frontend Development Rules

Conventions observed while building out the PM (Project Management) module's Assessment/Field Plan/AMC features (`installation-ui/web/micro-ui-internals/packages/modules/pm`). Follow these for consistency with the rest of the codebase. Most of these patterns are established elsewhere in the DIGIT/eGov micro-frontend architecture (`qc`, `fa`, `amc` modules) — the PM module mirrors them rather than inventing new ones.

## 1. Folder structure

Inside a module (`packages/modules/<module>/src/`):

```
src/
  pages/employee/       # top-level routed pages (one file per route)
  components/           # reusable/presentational pieces, grouped by owning page
    <PageName>/          #   e.g. components/AssessmentDetails/Filter.js
      Filter.js
      InfoCard.js
      SomeModal.js
  hooks/                # one React Query hook per entity/query, e.g. useAssessmentPlan.js
  services/             # one file per backend entity/API family, e.g. AssessmentPlan.js
  utilities/            # pure functions / constants, no React, no network calls
  redux/                # actions/ and reducers/, for cross-page "working entity" state
  constants/            # shared constant/enum-like values (e.g. ReduxActions.js)
```

- Components specific to one page live in a subfolder named after that page under `components/` (e.g. `components/AssessmentDetails/`, `components/ProjectFieldPlans/AMCTable/`).
- A `hooks/useX.js` file almost always pairs with exactly one `services/X.js` file — the hook owns the React Query wiring (cache key, `isLoading`/`revalidate`), the service owns the actual HTTP call and response-shape normalization.
- Business rules that don't need React (status-transition logic, eligibility checks, scenario evaluators) go in `utilities/`, not inline in a page/component. See `utilities/AssessmentPlanData.js`'s `canAssignForOnSiteAssessment`, `isUnanimousOverride`, `evaluateMarkResultScenario`.

## 2. Naming

- Hook: `useEntityName.js` exporting `useEntityName` (default export). Matches the service it wraps: `useAssessmentPlan` ↔ `AssessmentPlanService`.
- Service: `EntityName.js` exporting a single object `EntityNameService` with async methods, one per endpoint (e.g. `AssessmentPlanService.fetchAssessmentPlans`, `.upsertAssessmentPlan`).
- Avoid reusing a hook/file name for two conceptually different things even if superficially similar (e.g. a flat per-facility AMC list vs. a grouped per-project AMC-plan summary needed two different hooks — `useAMCConfigurationList` vs `useAMCConfiguration` — rather than overloading one name).
- Rename a component if its responsibility changes enough that the name becomes misleading (e.g. `SearchAction.js` → `FacilityActionBar.js` once the search feature was removed and it became a generic action bar).

## 3. DIGIT component usage

Prefer existing DIGIT components over hand-rolled UI. In this module:

- **Forms/wizards**: `FormComposerV2` + `Stepper` (`@egovernments/digit-ui-components`) for multi-step create/edit flows. Step configs are keyed by step number; `label`/`description`/`heading` strings inside a step's field config are **bare strings** (e.g. `label: "PM_CREATE_ASSESSMENT_LABEL_STATE"`), not wrapped in `t()` — `FormComposerV2` resolves them internally. Custom step components are registered by name in `Module.js`'s component map and referenced via `component: "PMSomeComponent"` in the config.
- **Tables**: the shared `Table` component (`@egovernments/digit-ui-react-components`), with a `columns` array of `{id, Header, Cell}` and local `GetHead`/`GetCell` render helpers per table component (each table component defines its own copies rather than sharing one global helper — matches the existing per-table duplication convention in `qc`/`fa`).
- **Dropdowns / multi-select filters**: build multi-select behavior from a single-select `Dropdown` (always shown with an empty `selected` value) plus `RemoveableTag` chips rendered below it for each selected value — there is no dedicated "multi-select dropdown" component in use here. See `components/AssessmentDetails/Filter.js` or `qc`'s `components/FacilityTable/Filter.js`.
- **Modals**: `PopUp` (`@egovernments/digit-ui-react-components`) as the positioning/backdrop primitive, with a custom `<div>` styled inline for the actual dialog box (fixed position, centered, white background, `borderRadius: 5px`, `padding: 24px`). `ConfirmActionModal`/`ReasonRequiredModal`/`CompleteAssessmentPlanModal` all follow this exact shape: title row with a close `button`, description paragraph, optional content, then a right-aligned button row (`Cancel` + primary action). A `singleAction` prop pattern hides the Cancel button for pure-acknowledgement dialogs (blocking/"not supported" notices).
- **Buttons**: `Button` component for primary/secondary actions inside modals and toolbars; plain styled `<button>` for icon-shaped nav/utility buttons (filter clear, download) where `Button`'s API doesn't fit.
- **Loader**: `Loader` for full-page/section loading states; a semi-transparent full-screen overlay `<div>` (`backgroundColor: "gray", opacity: 0.5, position: "fixed"`) wrapping a `Loader` for in-place "action in progress" blocking, driven by a `blockUI`/`actionLoading` boolean.
- **Toast**: `Toast` component driven by local `{key: "success"|"error"|"warning", label}` state, auto-dismissed via a `setTimeout` in a `useEffect`.

## 4. Data fetching (React Query)

- Every read goes through a `useX` hook wrapping `useQuery` from `react-query`. Query key is an array literal: `[CACHE_KEY_STRING, ...paramsThatShouldInvalidate]` — include every filter/pagination param that should trigger a refetch, e.g. `["ASSESSMENT_FACILITY", planId, filters, limit, offset]`.
- Hooks return `{isLoading, isFetching, isError, error, data, revalidate}`. `revalidate` calls `queryClient.invalidateQueries([CACHE_KEY])` and, where the caller needs the fresh value immediately after a mutation, also returns `queryClient.getQueryData([...])`.
- Disable a query until its inputs are ready with `{ enabled: !!requiredParam }` rather than guarding inside the query function.
- Give two hooks distinct cache keys even if they happen to fetch from the same underlying search endpoint, if they represent different shapes/entities (e.g. `ASSESSMENT_FACILITY` vs a plan-detail-derived `ASSESSMENT_PLAN_DETAIL`) — don't let two different data shapes collide under one key.

## 5. Backend API conventions (this is inconsistent across services — check the specific endpoint's actual convention, don't assume)

- Some endpoints use a capitalized array envelope: `{FieldPlans: [...], apiOperation: "CREATE"|"UPDATE"}` for field plans.
- Newer assessment-plan endpoints use a lowercase singular envelope instead: `{plan: {...}}` for create/update, `{criteria: {...}}` for search filters, `{decisions: [...]}` for bulk facility updates. **Do not assume one convention applies platform-wide** — confirm the exact shape from the API spec/curl given for that specific endpoint.
- `Request` (`@egovernments/digit-ui-libraries`) auto-injects `RequestInfo` into whatever data object is passed — don't build `RequestInfo` by hand for standard JSON APIs.
- File upload/download/export endpoints (ingestion-service) go through `CustomRequest` (`components/Custom/CustomRequest.js`) instead of `Request`, using `fileDownload: true, responseType: "blob"` for downloads, or a `FormData` body with `attachRequestInfo: (data, RequestInfo) => data.append("request_info", JSON.stringify(RequestInfo))` for multipart validate/apply flows.
- For `responseType: "blob"` endpoints, a failed request's error body is *also* a Blob, not parsed JSON — `error.response.data` isn't usable directly. Use `CommonUtils.getBlobApiErrorMessage(error)` (async — reads the blob's text, parses it, then reuses the normal envelope-detection logic) instead of the synchronous `getApiErrorMessage` for these, or you'll silently get axios's generic `"Request failed with status code 400"` instead of the real backend message.
- When a response's exact envelope key is unknown, **ask for a real sample response (or the API spec) and confirm the exact key before writing the extraction** — don't guess with a multi-key fallback chain:
  ```js
  // Avoid this — it hides which key is actually correct, and silently keeps working
  // even if the assumed key is wrong and a different fallback happens to match.
  const plan = response?.plan || response?.Plan || response?.assessmentPlan || response?.AssessmentPlan || extractAssessmentPlans(response)?.[0];

  // Prefer this, once the real shape is confirmed:
  const plan = response?.plan;
  ```
  A fallback chain reads as if the shape is genuinely ambiguous across backends, but in practice there is one real envelope — the chain just papers over not having confirmed it. It also makes a future shape change (e.g. the backend renaming the key) fail silently by falling through to the wrong branch instead of erroring. If a sample response genuinely isn't available yet and the code must be written before it is, leave a `// TODO: confirm response envelope` comment naming the assumed key, rather than a silent multi-key guess.
- Service functions normalize/reshape the raw response into the field names the UI expects (e.g. mapping `facilityName`/`phoneStatus`/`fieldStatus`/`overallStatus` → `name`/`remoteStatus`/`onSiteStatus`/`result`) at the service layer, so every downstream consumer (table columns, CSV export, modals) can rely on one consistent internal shape regardless of what the backend calls things.
- Convert dates to epoch millis with `(new Date(value)).getTime()` when sending to the backend, and back to a display/form string with a local `formatDate` helper when reading — never pass a raw date string straight into a request body if the API expects millis.

## 6. MDMS-schema-driven dynamic rendering

- When the set of fields/questions to render isn't fixed in code but comes from an MDMS master (e.g. `assessment.AssessmentMobileFormSchema`, one schema entry per submission `formType`), don't hardcode a fixed list of fields to display. Instead: match the submission to its schema by an explicit discriminator field (`formType`), then walk the schema's own structure (`pages` → `properties`, sorted by an `order` field) to build the display sections dynamically.
- Resolve enum-backed values (dropdown/select/radio, `property.enums: [{code, name}]`) from the submitted `code` to the schema's own `name` at the point of reading the data, not in the render layer — the render layer should only ever see already-resolved display values plus enough metadata (see §8) to know whether that value is safe to further translate.
- A field/page only shows up if it actually has data in the submission (filter out empty/`null` fields, and drop pages left with zero populated fields). This naturally matches a mobile form's own conditional-visibility logic (`visibilityCondition` expressions) without needing to evaluate those expressions in the web UI — an unfilled field simply isn't in the submission's data at all.
- Keep the schema-walking logic in `utilities/`, taking the raw submission + schema array and returning plain `{label, value, ...}` data, independent of any single page/component — this is what let the same logic get reused for two different modules' consumers (see §7).

## 7. Cross-module feature porting

When the same feature needs a lighter version in another module (e.g. `pm` builds the full read/write assessment facility details experience, `fa` only needs a read-only view reached from its own Activity tab):

- Port only what that consumer actually needs — don't bring over action bars, confirmation modals, or mutation logic a read-only page will never use. Check what the *existing* equivalent page in that module already does (e.g. `fa`'s `ActivityDetails.js` has no action buttons) before deciding scope; match that module's own established pattern rather than the richer source module's.
- Check for an existing identical component in the target module before creating a new one — `fa` already had a byte-identical `Section.js` to what we were about to add, so we imported the existing one instead of duplicating it.
- Reused localization keys need to resolve in *every* module that uses them, not just the one that originated them. If module B's `t()` calls only ever load module B's own bundle plus `rainmaker-common`, a key registered under `rainmaker-<module-a>` won't resolve in module B even though the string content is identical — register genuinely shared keys under `rainmaker-common` instead.
- Give the ported feature its own redux "working entity" slot in the target module rather than piggybacking on an existing one with a different shape, even if a slot with a superficially similar name already exists there — mixing shapes into one slot risks a stale/wrong-shaped value leaking into a breadcrumb or header from an unrelated flow.

## 8. State management

- **Redux** is used narrowly, for "working entity" state that needs to be visible across pages without prop-drilling (e.g. `populateWorkingProject`, `populateWorkingAssessmentPlan`) — dispatched from a `useEffect` once the entity loads, consumed elsewhere (breadcrumbs, headers). It is not used as a general app-state store.
- **URL query params** (`Digit.Hooks.useQueryParams()` + `history.replace(...)`) persist wizard/list-page state that should survive a refresh or back/forward navigation: current wizard step + entity id (`?assessmentId=...&key=2`), and list filters/pagination (`?filter=...&pageSize=...&pageOffset=...`).
- Everything else is local `useState`/`useMemo` in the owning component.

## 9. Filters (list pages)

- Filter state shape lives in the parent page as `projectQueryFilter = {entityFilter: {...}, entitySearch: {...}, entityFilterQuery: {}, entitySearchQuery: {}}`, restored from the URL's `filter` param on mount.
- The `Filter` component owns `entityFilter` (the UI-facing selection, e.g. arrays of `{code, name}` objects) and derives `entityFilterQuery` (the backend-facing filter payload, arrays of plain codes) from it on every change, calling a single `onFilterChange({entityFilter, entityFilterQuery})` prop.
- Normalize a filter object defensively when reading it back (`filter?.category || []` per field, not just `filter || EMPTY_FILTER` at the top level) — a filter persisted in the URL from an older shape can have a field present-but-`null` instead of missing, which a top-level fallback won't catch.
- Cascading filters (category→type, district→block) filter the *dependent* dropdown's option list client-side based on the current selection, and prune now-invalid dependent selections when a parent selection is removed. Selecting more parent values only ever widens the dependent options (never narrows) — the narrowing only happens when a parent value is removed.
- Boundary (district/block) and MDMS-backed (category/type) dropdown data is fetched unscoped (e.g. `useBoundary("State")` fetches nationwide) and then restricted client-side to whatever scope is relevant (e.g. the current project's or assessment plan's own `geographyDetails`) — don't rely on the fetch itself being pre-scoped unless the hook explicitly documents that.

## 10. Localization

- Every new user-facing string needs a translation key. Track new keys in a personal local repo, kept outside the codebase, rather than in an ad hoc notes file — see "Personal localization tracking repo" below.
- `t()` wraps every rendered string that is (or could plausibly become) a real translation key — **except** bare strings inside `FormComposerV2` step configs (see §3), and it must **never** be called on raw data (dates, times, free text, numbers). i18next's default `nsSeparator` is `:`, so any value containing a colon gets silently mangled — `t("19:02")` returns `"02"`, not the string back, and an ISO timestamp comes back with chunks missing. This isn't a theoretical risk; it broke assessment date/time rendering in production-shaped data.
- For dynamic, MDMS-driven display values (enum-backed dropdown/select/radio options, whose `name` is a finite, curated vocabulary that could reasonably become a real key later): still route through `t()` for future-proofing, but tag the value explicitly as translatable (this codebase uses a `translateValue` boolean set at the point the value is resolved) so the render side can branch — `field.translateValue ? t(field.value) : field.value`. Never assume "it's from MDMS so it's safe to translate" — only enum option `name`s are; free-text/date/numeric MDMS-sourced fields are not.
- Reuse an existing key if the copy is close enough in meaning; add a new key when the exact wording matters (e.g. a specific mockup-provided sentence) rather than repurposing an unrelated existing key.
- Don't delete keys that become unused after a refactor — leave them (harmless) rather than editing a past, already-committed entry in the personal tracking repo for a pure removal.
- A key shared across modules (a consuming module's own `t()` calls only load its own bundle plus `rainmaker-common`) must be registered under `rainmaker-common`, not the originating module's `rainmaker-<module>` — see §7.

### Personal localization tracking repo

Every new key gets recorded in a small local git repo on the developer's own machine — a durable, diffable personal record of localization additions, kept outside the actual codebase (it's not the real localization service, just a staging record a developer copies from later).

- **Locating the repo**: the path is read from the environment variable `SELCO_LOCALIZATIONS_REPO` (set once by the developer in their shell profile, analogous to `$HOME` — not something set ad hoc with a one-off `export`, since that wouldn't survive to the next shell/session). Before using it in a session, verify both that the variable is set *and* that the path it points to exists and is a git repo (has a `.git` dir).
- **Detecting which profile file to use**: don't assume `~/.zshrc` (or `~/.bashrc`) — check `echo $SHELL` to see the developer's actual login shell and map it to the matching file (`/bin/zsh`/`/usr/bin/zsh` → `~/.zshrc`, `/bin/bash`/`/usr/bin/bash` → `~/.bashrc`). If the result is ambiguous (an unrecognized shell, or the mapped file doesn't exist while some other rc file does), ask the developer which file they want it added to rather than guessing.
- **If either check fails** (variable unset, or set but the folder is missing/not a repo), don't guess or silently recreate anything — **stop and ask the developer** what to do (e.g. point at an existing folder, create a fresh one at a suggested default like `~/Selco/Localizations`, or provide a different path). A missing variable or folder may well be intentional (developer cleanup), so treat it as a decision point, not an error to route around.
- **First-time setup**, once the developer picks a path (and the profile file is resolved as above): print the exact literal commands rather than just describing the steps — don't assume the developer will infer syntax from a description. For example, for path `~/Selco/Localizations` and a detected shell of zsh:
  ```bash
  mkdir -p ~/Selco/Localizations && git -C ~/Selco/Localizations init
  echo 'export SELCO_LOCALIZATIONS_REPO=~/Selco/Localizations' >> ~/.zshrc
  source ~/.zshrc
  ```
  The `mkdir`/`git init` are local and non-destructive, so run them directly. The shell-profile line and the `source` touch the developer's personal environment outside the project, so print both and have the developer run them (or confirm before running them) — the running session won't pick up the new variable without the `source` step (or a new shell), so don't leave it out or treat it as a "you'll get to it eventually" step.
- **The `export` won't reach the agent's own shell tool, even after setup.** Verified in-session: the shell tool always runs non-interactively (`$-` has no `i`), and a standard `~/.bashrc` starts with an interactive-only guard (`case $- in *i*) ;; *) return;; esac`) that returns *before* reaching the `export` line on every non-interactive invocation — so `$SELCO_LOCALIZATIONS_REPO` is invisible to the agent's own commands by design of that guard, even though `source`-ing it worked fine and it resolves correctly in the developer's real interactive terminals. Don't try to work around this by re-sourcing the profile in every command or re-raising it with the developer as if it were still unresolved — it's a structural property of non-interactive shells, not a fluke to keep re-diagnosing. Instead, every command the agent runs against this repo should read the variable with a fallback to the known/just-confirmed path: `REPO="${SELCO_LOCALIZATIONS_REPO:-<the path confirmed with the developer this session>}"`.
- **Layout**: `<repo>/<module>/<locale>.json`, e.g. `rainmaker-pm/en_IN.json`, `rainmaker-common/en_IN.json` — one JSON file per module+locale, holding a plain array of `{"code", "message", "module", "locale"}` objects (the same shape the real localization service's upload envelope expects, so the file can be used as a direct upload payload later, not just a reading reference).
- **Adding a key**: read the target module+locale file (create it — `[]` — if it doesn't exist yet), check whether the `code` already exists before appending — if it does with a *different* message, flag the conflict to the developer rather than silently overwriting; if it matches, skip re-adding it.
- **Committing**: commit to this repo periodically as keys accumulate (e.g. once per feature/session), not necessarily one commit per key. There's no remote, so these commits are a low-risk personal audit trail, not something that needs the same care as commits to the actual project repo.

## 11. Error handling & loading state

- Every mutating action follows the same shape:
  ```js
  const handleAction = async () => {
    setActionLoading(true);
    try {
      await SomeService.doThing(...);
      await revalidate();
      setToast({ key: "success", label: t("...") });
    } catch (error) {
      console.error("Error doing thing", error);
      setToast({ key: "error", label: CommonUtils.getApiErrorMessage(error) || t("...") });
    } finally {
      setActionLoading(false);
    }
  };
  ```
- `CommonUtils.getApiErrorMessage(error)` (utilities/CommonUtils.js) extracts a human-readable message from the backend's error envelope, falling back to a generic translated message — always prefer it over a hardcoded error string when a real backend call could fail.
- For a `responseType: "blob"` call (see §5), use `await CommonUtils.getBlobApiErrorMessage(error)` instead in the `catch` block — `getApiErrorMessage` can't read a Blob-typed `error.response.data` and will silently fall through to axios's generic status-code message.
- Don't add error handling for cases that can't happen (e.g. validating a value that's already guaranteed non-null by the calling context) — only guard real boundaries (API calls, user input).

## 12. Business-rule code

- Multi-condition eligibility/validation logic (e.g. the Mark Eligible/Not Eligible scenario checks) lives in a single pure function in `utilities/`, taking plain data and returning a scenario/result string, so the calling component just does `if (scenario === "X") { ... }`. Keep the strict-priority-order nature of such rules explicit in comments, since re-ordering the checks silently changes behavior.
- When a UI condition depends on several boolean sub-conditions (e.g. "assign for on-site" enablement), express it as a small named predicate function (`canAssignForOnSiteAssessment(facility)`) rather than inlining the boolean expression at every call site — it gets called from both the bulk-action gate and the single-facility-modal gate and must stay in sync.

## 13. Git / commit practices

- One commit per logical change; don't bundle unrelated fixes into a commit whose message describes something else.
- Never include a co-author trailer unless explicitly asked.
- Only amend or force-push when explicitly requested — flag the force-push requirement (e.g. after an amend of an already-pushed commit) and get confirmation before doing it.
- Verify a fix compiles (`babel.transformFileSync` sanity check, or the project's real build) before reporting a change as done — this codebase's build pipeline (`babel-plugin-transform-async-to-promises` under `microbundle`) has caught real bugs (e.g. a missing `break` in the last case of a `switch` inside an `async` function) that plain visual review missed.
- **Before finalizing a feature's commits, sweep for dead code the feature itself introduced and remove it** — util files/functions superseded by a later iteration of the same feature (e.g. dummy/placeholder data or a first-pass helper replaced once the real data source was wired up), components stopped being rendered, exports nothing calls anymore. This is scoped to code *this feature's own work* introduced and then moved away from while solving the same problem — not a general dead-code audit of pre-existing code you happen to notice nearby, which is an unrelated, out-of-scope cleanup that shouldn't ride along in this diff. Verify with a repo-wide grep for references before deleting (not a visual guess) — a JS export can still be picked up by another module or a dynamic import that a local scan misses. This is the opposite case from the localization-key rule in §10 ("don't delete unused keys") — that exception exists because a key lives in a *shared* tracking file where deletion risk outweighs the tidiness gain; dead code in your own feature's diff has no such coordination cost, so leaving it in is just debt with no offsetting benefit.
- **Promoting a fix between `develop` and `staging` (either direction)**: checkout the target branch, `git pull origin <branch>` to get latest, create a new branch off it, then `GIT_EDITOR=true git cherry-pick <sha1> <sha2> ...` the specific commits (not a merge — the two branches otherwise diverge too much for a clean merge). Compile-check afterward, push, and open a PR.
- A promotion PR's title and summary describe the actual change, matching the originating PR's title/summary when it's a straight promotion — never mention "cherry-pick" in either. Cherry-picking develop→staging is the normal, expected mechanism for every promotion, so calling it out in the title/summary is meaningless noise, not useful information.
- When a CodeRabbit (or other reviewer) suggestion is being skipped or wasn't implemented as proposed, reply **in-thread** on that specific review comment (`gh api repos/.../pulls/<n>/comments/<id>/replies -X POST -f body=...`) explaining why — not a new top-level PR comment (`gh pr comment`), which reads as unrelated to the reviewer's specific finding.
- `gh pr edit` intermittently fails on an unrelated GitHub GraphQL "Projects (classic) deprecation" error, aborting the whole edit. Work around it with `gh api repos/.../pulls/<n> -X PATCH -f title=... -f body=...` instead.
- Always pass an explicit, descriptive `--title` to `gh pr create` — without it, the title defaults to the raw branch name (slashes and all, e.g. "Fix/assessment module ui"), which reads as generic and unpolished.

## 14. Adding a new sidebar (access-control) entry

The employee sidebar is not driven by anything in this codebase — it's built at runtime from role-filtered MDMS data (`Digit.Hooks.useAccessControl()` → `POST /access/v1/actions/mdms/_get`, backed by MDMS module `ACCESSCONTROL-ACTIONS-TEST` master `actions-test`, cross-referenced against module `ACCESSCONTROL-ROLEACTIONS` master `roleactions` for per-role visibility). Adding a new sidebar entry means generating MDMS data, not writing frontend code — see `packages/modules/core/src/components/TopBarSideBar/SideBar/EmployeeSideBar.js` for how the two masters get turned into rendered nav items.

- **One `actions-test` record for the entry itself, plus one `roleactions` record per role granted visibility.** These are two independent MDMS masters with two independent id sequences — never assume one counter serves both.
- **Only the `data` object needs to be authored.** The envelope fields (outer row `id`, `auditDetails`) are populated by the platform itself on creation; `uniqueIdentifier` is just the new `data.id` as a string, and `tenantId`/`schemaCode`/`isActive` are fixed/known values, not generated data.
- **Look up each master's next id via `POST <host>/egov-mdms-service/v1/_search`** (`MdmsCriteria: {tenantId, moduleDetails: [{moduleName, masterDetails: [{name}]}]}` — this is a public endpoint, no `authToken` needed) and take the max `id` from the full returned array. The endpoint has no server-side sort/pagination — the whole master's dataset always comes back — but that's fine, only the max matters, not the full contents.
- **Fetch once per master per invocation, then increment locally in memory** — don't re-fetch per record. `actions-test` needs exactly one new id (max+1); `roleactions` needs one new id per role being granted access, computed as max+1, max+2, ..., max+N off that single fetch.
- **Don't call `_create` directly** — generate the ready-to-paste `data` payload(s) for the developer to review and apply through their existing MDMS-authoring process (e.g. workbench-ui). Writing to a live MDMS environment is a shared-system mutation outside this repo, so it goes through the same human-in-the-loop step as any other cross-service change.
- **Staleness between lookup and apply is the developer's problem, not something to auto-mitigate.** If the generated ids sit unapplied long enough that someone else adds data to the same master in the meantime, that's on the developer to notice — only re-run the lookup if explicitly asked to regenerate; don't add automatic re-validation/re-fetch logic.
- **Prompt for**: which environment host to query (dev/staging/prod each have independent MDMS data — picking the wrong one silently computes the wrong next id), `tenantId`, the entry's own fields (`navigationURL`, `name`/`displayName`, `path`, `orderNumber`, `serviceCode`, `parentModule`, `leftIcon`), and the list of role codes to grant. For `leftIcon`, verify the chosen name is an actual exported key in the installed `@egovernments/digit-ui-svg-components` package rather than assuming any plausible Material-Icons-style name works — a name that looks valid but isn't in that specific package version resolves silently to a blank icon (a console warning, not an error) rather than failing loudly.
- **Don't ask the developer to recall role codes from memory** — look them up first via the same public `_search` endpoint against MDMS module `Organisation` master `OrgRoles`, and present the full `code`/`name`/`description` for each (not just the bare code — codes like `STATE_POC` vs `CENTRAL_POC` vs `CENTRAL_ONM_PROJECT_MANAGER` aren't self-explanatory on their own) so the developer can pick accurately.
