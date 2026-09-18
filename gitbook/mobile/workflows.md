# Mobile workflows

The mobile app is organized around field-user workflows. This page maps visible source files to the user journeys they support.

## Authentication and session

Relevant pages and modules:

- `login.dart`
- `enter_otp.dart`
- `forgot_password.dart`
- `setup_new_password.dart`
- `role_selection.dart`
- `authenticated.dart`
- `unauthenticated.dart`
- `mobile/lib/blocs/auth`
- `mobile/lib/repositories/auth_repo.dart`
- `mobile/lib/repositories/user_repository.dart`

## Facility and asset setup

Relevant pages and modules:

- `select_health_facility.dart`
- `select_asset_type.dart`
- `asset_count.dart`
- `asset_type_detail.dart`
- `specification.dart`
- `add_new_asset.dart`
- `asset_summary.dart`
- `overall_asset_summary.dart`
- `inbox_asset_summary.dart`
- `mobile/lib/blocs/asset_submission`
- `mobile/lib/blocs/asset_summary`
- `mobile/lib/blocs/asset_type`
- `mobile/lib/blocs/specification`
- `mobile/lib/repositories/asset_repo.dart`

## Installation and media capture

Relevant pages and modules:

- `installation_images.dart`
- `media_upload.dart`
- `image_viewer.dart`
- `video_player.dart`
- `pdf_viewer.dart`
- `submit_for_approval.dart`
- `submitted_save_success.dart`
- `data_save_success.dart`
- `mobile/lib/blocs/installation_images`
- `mobile/lib/blocs/cache_installation_image`
- `mobile/lib/blocs/cache_media_upload`
- `mobile/lib/repositories/installation_images_repo.dart`

## Scheduled visits and activities

Relevant pages and modules:

- `home.dart`
- `inbox.dart`
- `dynamic_form.dart`
- `draft.dart`
- `sync_loading.dart`
- `mobile/lib/blocs/scheduled_visit`
- `mobile/lib/blocs/scheduled_visit_submission`
- `mobile/lib/blocs/activity_facility`
- `mobile/lib/blocs/activity_facility_bom`
- `mobile/lib/blocs/selected_activity_facility`
- `mobile/lib/blocs/selected_scheduled_visit`
- `mobile/lib/repositories/scheduled_visit_repo.dart`
- `mobile/lib/repositories/activity_facility_repo.dart`
- `mobile/lib/repositories/activity_facility_workflow_repo.dart`

## AMC workflows

Relevant pages and modules:

- `amc_home.dart`
- `amc_inbox.dart`
- `amc_draft.dart`
- `amc_dynamic_form.dart`
- `amc_media_upload.dart`
- `amc_otp.dart`
- `amc_report_home.dart`
- `amc_rejection_reasons.dart`
- `amc_select_facility.dart`
- `mobile/lib/blocs/amc_otp`
- `mobile/lib/blocs/cache_amc_media_upload`
- `mobile/lib/blocs/selected_amc_origin`

## Cache and offline-oriented flows

Relevant BLoC areas include:

- `cache_activity_facility_asset`
- `cache_add_new_asset`
- `cache_asset`
- `cache_asset_count`
- `cache_asset_detail`
- `cache_completion_report`
- `cache_specification`
- `cache_sync_record`

These areas should be tested carefully because field users may depend on drafts, delayed sync, or interrupted network recovery.

## Supporting app areas

- Localization: `mobile/lib/blocs/localization`, `mobile/lib/repositories/localization_repo.dart`.
- App initialization: `mobile/lib/blocs/app_init`, `mobile/lib/repositories/app_init_repo.dart`.
- Routing: `mobile/lib/router`.
- Shared widgets: `mobile/lib/widgets`.
- Local data and secure storage: `mobile/lib/data`.
