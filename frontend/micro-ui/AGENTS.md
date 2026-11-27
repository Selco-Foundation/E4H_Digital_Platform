# Agent instructions — micro-ui (Incident Management)

Read `../AGENTS.md` first. It covers git, PRs, localization, MDMS,
verification and shared-system safety. This file adds what is specific to
this app and **overrides the shared data-layer section** where noted.

The app handles incidents (tickets) for installed solar systems: health
facilities raise them, vendors resolve them, and state/tech teams review
them. It also has the RMS-pause flow for facilities.

## Scope: `core` and `im` only

Only two modules matter here, plus the shared packages they're built on:

| Package | Path | What it is |
|---|---|---|
| `im` | `packages/modules/im` | The product module. Its package name is still `@selco/digit-ui-module-pgr` (a fork of eGov PGR), its Redux key is `pgr`, and it is registered as `IM`. |
| `core` | `packages/modules/core` | Shell: routing, login/session, top bar/sidebar, home. |
| `libraries` | `packages/libraries` | **Local fork** of `@egovernments/digit-ui-libraries`. `im`'s hooks and services live here. |
| `react-components` | `packages/react-components` | **Local fork**, published as `@selco/digit-ui-react-components`. `im`'s UI components come from here. |
| `css` | `packages/css` | `@selco/selco-css`, shared styles. |

- The other modules on disk (`dss`, `hrms`, `engagement`, `workbench`,
  `utilities`, `common`, `bills`, `receipts`, `templates`) are out of scope.
  Don't change them, and don't treat them as examples to follow.
- Despite the names `pgr`/`PGR*` in code, this is the IM product. Keep the
  existing names; don't rename them as part of unrelated work.

## Layering

```
im, core  ──may import──▶  libraries, react-components, css
libraries ──must not import──▶  im, core
im        ──must not import──▶  core (and the reverse)
```

`im` reaches `libraries` at runtime through the `Digit` global
(`Digit.Hooks.im.*`, `Digit.Hooks.pgr.*`, `Digit.PGRService`,
`Digit.RMSService`, `Digit.FacilityService`, …), not through direct imports.

## Data layer (overrides the shared section)

`im`'s services and hooks live in `libraries`, not in `im` (it has no
`hooks/` folder, and its `services/` folder is legacy — see below). Follow the
existing layout:

| Piece | Where it goes | How it's registered |
|---|---|---|
| Endpoint URL | `libraries/src/services/atoms/urls.js` (for example `im_search: "/im-services/v2/request/_search"`) | — |
| Service | `libraries/src/services/elements/IM/<Entity>.js` (IM-specific) or `elements/PGR.js` (ticket CRUD) | `setupLibraries("<Entity>Service", <Entity>Service)` in `libraries/src/index.js` → `Digit.<Entity>Service` |
| Hook | `libraries/src/hooks/im/use<Thing>.js` (IM) or `hooks/pgr/` (ticket) | add to the `im` (or `pgr`) object in `libraries/src/hooks/index.js` → `Digit.Hooks.im.use<Thing>` |
| Pure helpers / mappers | `im/src/utilities/` (UI-side) or alongside the hook in `libraries` | — |

- `im/src/services/` exists but holds one legacy file, `Video.js`, which
  `ComplaintDetails.js` imports directly. Don't add to it. New services go in
  `libraries` as above.
- The shared service and hook rules still apply.
- **Workflow and ticket updates** go through Redux thunks in
  `im/src/redux/actions/` (for example `updateComplaints` → `Digit.PGRService.update`).
  Keep new ticket mutations in that pattern; don't call the service directly
  from a component.
- **Changing `libraries` or `react-components` means rebuilding them too.**
  `im` imports their built output, not their source.

## `im` module layout

```
src/
  Module.js            # IMModule (citizen vs employee app), componentsToRegister
  EmployeeApp.js       # employee routes
  pages/employee/      # Inbox, ComplaintDetails, CreateComplaint, PauseRMS, RMSPausedFacilities, Rating, Response…
  pages/citizen/       # not used, leave untouched
  components/          # inbox/ (Filter, Status, links), timelineInstances/, rmsPaused/, Desktop/Mobile inbox
  redux/               # actions/ (thunks), reducers/ (complaintReducer, pauseRMSReducer)
  selectors/           # complaint, processInstance
  constants/           # Routes, Localization, Employee, Citizen
  utilities/           # CommonUtils
```

- **Only the employee side is in use** (`defaultLanding="employee"`,
  `allowedUserTypes={["employee"]}`). `pages/citizen/` is **not used**.
  Don't change it, extend it, or copy patterns from it.
- Inbox, ticket and RMS views each have desktop and mobile versions
  (`DesktopInbox`/`MobileInbox`, `RMSPausedDesktop`/`RMSPausedMobile`). A
  change to one usually needs the same change in the other.

## Roles

- Role codes and what they mean can change, so they aren't listed here.
- Role checks are written inline against
  `Digit.UserService.getUser().info.roles`. Work out which roles a
  requirement involves from the existing checks (the inbox `Filter.js` is a
  good starting point) and match their conventions.
- Some inbox filters combine role and workflow status, and the status lists
  are hardcoded in several places. When adding a role- or status-dependent
  behaviour, find every one of them.

## PWA

The deployed app (`web/`) registers `public/pwa-assets/service-worker.js`
from `src/index.js`, but only when `PUBLIC_PATH` is set. After a deploy,
users may see a cached old version until the service worker updates. Keep
this in mind when a reported bug "is already fixed".
