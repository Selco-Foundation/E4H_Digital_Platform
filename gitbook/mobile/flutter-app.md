# Flutter app

The Flutter app is located in `mobile`.

## Required versions

Use Flutter `3.22.2` with Dart `3.4.3`. Other versions may fail dependency resolution, code generation, or runtime behavior.

## Project structure

| Path | Purpose |
| --- | --- |
| `mobile/pubspec.yaml` | Flutter package metadata and dependencies. |
| `mobile/lib/blocs` | State-management modules. |
| `mobile/lib/repositories` | Data access and integration layer. |
| `mobile/lib/model` | Domain, request, response, and master-data models. |
| `mobile/lib/pages` | User-facing screens. |
| `mobile/lib/router` | Navigation and route definitions. |
| `mobile/lib/widgets` | Shared UI components. |
| `mobile/lib/data` | Local storage and secure storage helpers. |
| `mobile/test` | Automated tests. |
| `mobile/android` | Android platform project. |
| `mobile/ios` | iOS platform project. |

## Major dependencies

The app uses:

- `flutter_bloc` for BLoC state management.
- `dio` and `pretty_dio_logger` for HTTP clients and request logging.
- `drift`, `isar`, and `isar_flutter_libs` for local persistence.
- `flutter_secure_storage` and `shared_preferences` for local app state and secure data.
- `connectivity_plus` and `internet_connection_checker_plus` for network state.
- `location`, `permission_handler`, and `disable_battery_optimization` for device capabilities.
- `image_picker`, `video_player`, `open_file`, `flutter_pdfview`, and `cached_network_image` for media and document handling.
- `firebase_core` and `firebase_analytics` for Firebase integration.
- `digit_ui_components`, `digit_data_model`, `digit_dss`, `digit_scanner`, and `digit_forms_engine` for DIGIT/SELCO app capabilities.
- `flutter_local_notifications` for local notifications.
- `auto_route`, `build_runner`, `freezed`, `json_serializable`, and mapper generators for routing and generated code.

## Development guidance

- Keep workflow behavior aligned with backend APIs and schemas.
- Update models and repositories together when API contracts change.
- Validate cache-backed flows carefully because field users may rely on offline or delayed-sync behavior.
- Update this page when new major mobile modules are added.

## Related docs

- [Mobile setup](../getting-started/mobile-setup.md)
- [API reference](../backend/api-reference.md)
- [Schemas](../data-and-integrations/schemas.md)
- [Mobile workflows](workflows.md)
