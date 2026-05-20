# Field Planner Activity

## Purpose

The field planner activity service supports activity-level execution for field planning workflows.

## Source location

- Service path: `backend/e4h-services/field-planner-activity`
- README: `backend/e4h-services/field-planner-activity/README.md`
- Local setup: `backend/e4h-services/field-planner-activity/LOCALSETUP.md`
- Changelog: `backend/e4h-services/field-planner-activity/CHANGELOG.md`

## Responsibilities

- Represents activity-level field planning behavior.
- Supports field execution workflows that are more granular than project-level planning.
- Provides backend support for mobile activity facility and scheduled visit flows.

## Related mobile source areas

- `mobile/lib/blocs/activity_facility`
- `mobile/lib/blocs/activity_facility_bom`
- `mobile/lib/blocs/selected_activity_facility`
- `mobile/lib/repositories/activity_facility_repo.dart`
- `mobile/lib/repositories/activity_facility_workflow_repo.dart`

## Operational notes

Use this service page as the activity-planning index. Keep detailed endpoint and workflow behavior in the service README and update this page when the service contract becomes clearer.
