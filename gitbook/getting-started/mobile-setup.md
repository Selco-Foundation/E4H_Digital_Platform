# Mobile setup

The mobile application is a Flutter project under `mobile`.

## Main locations

- `mobile/lib`: application code.
- `mobile/test`: tests.
- `mobile/android`: Android project.
- `mobile/ios`: iOS project.
- `mobile/pubspec.yaml`: Flutter dependencies and project metadata.

## Required versions

Use Flutter `3.22.2` and Dart `3.4.3`. These versions are necessary to run the application reliably.

## Typical setup flow

1. Install Flutter and platform-specific tooling.
2. Open the `mobile` directory.
3. Run dependency resolution with Flutter.
4. Configure any environment-specific app settings required by the target backend.
5. Run tests before shipping changes.
6. Run on a device or emulator for workflow validation.

## App structure

The app uses BLoC-oriented state management and separates models, repositories, pages, routing, utilities, and widgets.

See [Flutter app](../mobile/flutter-app.md) for the source map.
