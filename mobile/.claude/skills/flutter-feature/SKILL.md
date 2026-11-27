---
name: flutter-feature
description: Implement a new screen, flow or feature slice in the E4H Flutter app (model, repository, bloc, page, route, i18n, tests) by copying the project's existing patterns. Use when asked to build/add/implement a screen, page, list, form, API integration or feature from a ticket, plan or design image.
---

# Build a feature slice the project way

Work on ONE agreed task at a time (from `grill-me` if the task is not already clear). Copy patterns from the reference files; don't read other features "for context".

## Reference implementation (AMC scheduled visits) — read only the layer you're building
| Layer | Copy from |
|---|---|
| Model (freezed + json) | `lib/model/scheduled_visit/scheduled_visit.dart` |
| Endpoint path | `const` path inside the repo method (see `search()` in `scheduled_visit_repo.dart`); assessment endpoints go in `lib/repositories/assessment_api_paths.dart` |
| Remote repository (injectable `Dio`) | `ScheduledVisitRemoteRepository` in `lib/repositories/scheduled_visit_repo.dart` |
| Repository (remote + Isar fallback, `fromCache`) | `ScheduledVisitRepository` in the same file (grep for the class, read just it) |
| Isar cache | `lib/data/nosql/cache_scheduled_visit.dart` — also register the schema in `openIsar()` in `lib/utils/constants.dart` |
| Bloc (freezed events/states, `_requestGeneration` guard, pagination) | `lib/blocs/scheduled_visit/scheduled_visit.dart`; provide it in `lib/pages/authenticated.dart` if app-wide |
| Page (list) | `lib/pages/amc_inbox.dart`; with search/filter + pull-to-refresh: `lib/pages/assessment_select_facility.dart` |
| Route | `lib/router/routes.dart` + entry next to the related routes in `lib/router/app_router.dart` |
| i18n | group class in `lib/utils/i18_key_constants.dart` (see `AmcInbox`, `AmcSelectFacility`) |
| Test | `test/blocs/scheduled_visit_search_test.dart` (bloc), `test/repositories/offline_search_test.dart` (Isar offline, uses `test/support/offline_isar.dart`), `test/widgets/` for widgets |

## Order
1. Model → `dart run build_runner build --delete-conflicting-outputs`
2. Endpoint path + remote repository (backend missing → stop and raise a backend question)
3. Repository with Isar fallback (constructor-injectable)
4. Bloc (or `StatefulWidget` state if the screen is simple and local)
5. Page + route → run codegen again (`*.gr.dart`)
6. i18n keys
7. Test for logic you added (repository mapping, bloc states, helpers). Use fakes via constructor params.
8. `flutter analyze` and `flutter test test/<dir>/<file>_test.dart`

Follow the UI rules (`.claude/rules/flutter-ui.md`) for anything in `lib/pages/` or `lib/widgets/`.

## Finish with
- Files changed (one line each)
- New i18n keys with English text
- Backend questions
- Anything left for the next task
- If this task revealed a reusable pattern not covered here, propose a short addition to this skill or `CLAUDE.md` (don't write it without approval).
