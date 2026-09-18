# Mobile overview

The mobile application is a Flutter app under `mobile`.

The current README is the default Flutter starter README, so this GitBook page acts as the platform-level map for the app.

## Required versions

Flutter `3.22.2` and Dart `3.4.3` are necessary to run the application.

## Main capabilities visible from source structure

- Authentication and role selection.
- App initialization and localization.
- Asset submission and asset summary.
- Installation image capture and upload flows.
- Scheduled visits and scheduled visit submission.
- AMC home, inbox, OTP, media upload, dynamic form, report, and rejection flows.
- Offline/cache-oriented flows for assets, media, installation images, specifications, sync records, and completion reports.
- MDMS-backed model and widget support.
- PDF, image, and video viewer flows.

## Main source areas

- `mobile/lib/blocs`: BLoC state-management modules.
- `mobile/lib/repositories`: data access and service integration.
- `mobile/lib/model`: request, response, domain, and master-data models.
- `mobile/lib/pages`: application screens.
- `mobile/lib/router`: routing.
- `mobile/lib/widgets`: reusable UI widgets.
- `mobile/lib/data`: local and secure storage helpers.

## Main repositories

- `auth_repo.dart`
- `app_init_repo.dart`
- `asset_repo.dart`
- `activity_facility_repo.dart`
- `activity_facility_workflow_repo.dart`
- `dynamic_form_repo.dart`
- `installation_images_repo.dart`
- `localization_repo.dart`
- `operation_progress_repo.dart`
- `scheduled_visit_repo.dart`
- `user_repository.dart`

## Testing

Use Flutter test tooling from the `mobile` directory and validate workflows on a device or emulator when changes affect field-user flows.
