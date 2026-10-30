# E4H Field Assist mobile

Flutter application for installation, AMC, and assessment reports. Android application ID: `org.e4h.asset`.

This guide covers local development, APK builds, and signed Android App Bundles (AABs) for Google Play. **Run every command from this directory (`mobile/`)**, unless a command explicitly changes directory. Shell examples use Bash on macOS/Linux; replace example paths and values before running them.

## 1. Toolchain

| Component | Project baseline |
| --- | --- |
| Flutter | **3.22.2** |
| Dart | **3.4.3**, bundled with Flutter 3.22.2 |
| Java used by Gradle | **JDK 17** |
| Android compile/target SDK | **36** |
| Android NDK | **27.0.12077973** |
| Gradle | **8.11.1**, selected by the committed wrapper properties |
| Android Gradle Plugin | **8.9.1** |
| Minimum Android device | Android 7.0 / API **24** |

Flutter/Dart versions above are the locally recorded toolchain; the repository has no FVM pin. Keep `pubspec.lock` when installing dependencies.

## 2. Required local files

Obtain environment/Firebase configuration and the existing release signing material from the project maintainer or the team's secret store. Do not reconstruct production signing files from a different app.

### Files you supply

Paths below are relative to `mobile/`. The Flutter root `.gitignore` and `android/.gitignore` apply at different directory levels.

| File/location | Ignored by | What to supply |
| --- | --- | --- |
| `.env` | `.gitignore`: `.env` | Active environment configuration; bundled as a Flutter asset. Required before running, testing, or building. |
| `.env.dev`, `.env.uat`, `.env.prod`, or other `.env.*` | `.gitignore`: `.env.*` | Optional local environment copies. The app does **not** select these automatically. Copy the chosen file to `.env`. |
| `android/app/google-services.json` | `.gitignore` | Firebase Android configuration matching `org.e4h.asset` and the intended Firebase project. Used by the Google Services/Crashlytics plugins. |
| `android/key.properties` | `android/.gitignore`: `key.properties` | Four signing properties shown below. Required by the current Gradle configuration **even for debug builds**, because release signing properties are read during configuration. Debug APKs still use Android debug signing. |
| `android/signing/upload-keystore.jks` | `android/.gitignore`: `/signing/`, `**/*.jks` | Recommended location for the **existing** upload keystore. Its actual filename may differ; reference it correctly in `key.properties`. Other `.jks`/`.keystore` paths under Android are also ignored. |
| `android/local.properties` | `android/.gitignore`: `/local.properties` | Machine-specific SDK paths. Flutter normally creates/updates this; use paths for your machine. |

### Environment configuration

Create `.env` using the team's actual environment values. This is a structural example, **not a working production configuration**:

```dotenv
ENV_NAME=DEV
BASE_URL=https://your-environment.example.org/
TENANT_ID=in
MDMS_API_PATH=egov-mdms-service/v2/_search
ACTIONS_API_PATH=access/v1/actions/mdms/_get
MOBILE_APP_GLOBAL=https://your-environment.example.org/REPLACE_WITH_APP_CONFIG_PATH
PRIVACY_POLICY_URL=https://your-site.example.org/privacy
TERMS_AND_CONDITIONS_URL=https://your-site.example.org/terms
DUMP_ERROR_PATH=error-handler/handle-error
CONNECT_TIMEOUT=1200000
RECEIVE_TIMEOUT=1200000
SEND_TIMEOUT=1200000
SYNC_DOWN_RETRY_COUNT=3
RETRY_TIME_INTERVAL=5
```

The loader in `lib/utils/envConfig.dart` reads `.env`. `--dart-define` does not replace this configuration. Stop/rebuild the app after changing the environment. `.env` is packaged into the application, so it must not contain private signing credentials, service-account keys, or other server secrets. `CHECK_BANDWIDTH_API` is not read by the current environment loader.

### Signing properties and path resolution

Create `android/key.properties`:

```properties
storePassword=REPLACE_WITH_EXISTING_STORE_PASSWORD
keyPassword=REPLACE_WITH_EXISTING_KEY_PASSWORD
keyAlias=REPLACE_WITH_EXISTING_ALIAS
storeFile=../signing/upload-keystore.jks
```

`storeFile` is resolved relative to **`android/app/`**, not relative to `key.properties`. Thus `../signing/upload-keystore.jks` points to `android/signing/upload-keystore.jks`. An absolute path also works. Use forward slashes or properly escaped Java-properties paths on Windows. Passwords containing Java-properties escape characters must be encoded correctly; restoring an approved complete properties file avoids shell-escaping mistakes.

A manually supplied `android/local.properties` needs these entries:

```properties
sdk.dir=/absolute/path/to/android-sdk
flutter.sdk=/absolute/path/to/flutter
```

Flutter maintains `flutter.buildMode`, `flutter.versionName`, and `flutter.versionCode` entries during builds. Set release versions through `pubspec.yaml` or the build flags below instead of editing those generated entries.

### Generated and other ignored files

These do not need to be restored as release secrets:

| Ignore-file location | Ignored paths/patterns | Handling |
| --- | --- | --- |
| Flutter root `.gitignore` | `.dart_tool/`, `.flutter-plugins`, `.flutter-plugins-dependencies`, `.pub-cache/`, `.pub/` | Generated by dependency/tool commands. The default global Pub cache is outside the project. |
| Flutter root `.gitignore` | `/build/`, `/android/app/debug`, `/android/app/profile`, `/android/app/release` | Build outputs; regenerate. Archive release artifacts separately. |
| Flutter root `.gitignore` | `**/doc/api/`, `**/ios/Flutter/.last_build_id`, `app.*.symbols`, `app.*.map.json` | Generated docs, build markers, and optional debugging/obfuscation output. Retain matching symbol files if using obfuscated builds. |
| Flutter root `.gitignore` | `*.iml`, `*.ipr`, `*.iws`, `.idea/`, `.atom/`, `.history`, `.claude/`, `.kilo/` | Local IDE/editor/tool state; not build prerequisites. `.vscode/` is **not** actively ignored by this file. |
| Flutter root `.gitignore` | `*.class`, `*.log`, `*.pyc`, `*.swp`, `.DS_Store`, `.build/`, `.buildlog/`, `.svn/`, `.swiftpm/`, `migrate_working_dir/` | Temporary, platform, or local migration files; do not restore. |
| `android/.gitignore` | `gradle-wrapper.jar`, `/gradlew`, `/gradlew.bat` | Flutter injects missing Gradle wrapper files when preparing an Android build. Keep the committed `android/gradle/wrapper/gradle-wrapper.properties`; it selects the Gradle version. Run a Flutter build before using `./android/gradlew` on a fresh checkout. |
| `android/.gitignore` | `/.gradle`, `/captures/`, `GeneratedPluginRegistrant.java`, `.cxx/` | Gradle/native caches, captures, and plugin registration; generated. |
| `android/.gitignore` | `key.properties`, `**/*.keystore`, `**/*.jks`, `/signing/`, `/local.properties` | Supplied signing material and machine configuration described above. |

Generated Dart source files such as `*.g.dart`, `*.freezed.dart`, `*.mapper.dart`, and `app_router.gr.dart` are **not** globally ignored here. Generation can change tracked files; inspect the diff and include intentional generated changes with their source changes. Keep `pubspec.lock` committed.

## 3. Install dependencies and generate code

After supplying configuration:

```sh
flutter pub get
dart run build_runner build --delete-conflicting-outputs
```

This generates Freezed/JSON models, Dart mappers, AutoRoute routes, and database code configured by the dependencies. Do not edit generated source manually.

For continuous generation while developing:

```sh
dart run build_runner watch --delete-conflicting-outputs
```

If generation has stale state, stop watch mode and run:

```sh
dart run build_runner clean
dart run build_runner build --delete-conflicting-outputs
```

For a genuinely stale Flutter build, use `flutter clean`, then repeat `flutter pub get` and code generation. Cleaning is not required for every build. Do not delete `pubspec.lock` or use `flutter pub upgrade` to work around a release build failure.

## 4. Run locally and build APKs

Enable USB debugging on an Android device and authorize the computer, or start an Android emulator.

```sh
flutter devices
flutter run -d DEVICE_ID
```

Use the ID printed by `flutter devices`. To run a release build on a physical device:

```sh
flutter run --release -d DEVICE_ID
```

Build/install a debug APK:

```sh
flutter build apk --debug
adb -s DEVICE_ID install -r build/app/outputs/flutter-apk/app-debug.apk
```

Build a signed release APK, or smaller APKs split by ABI:

```sh
flutter build apk --release
flutter build apk --release --split-per-abi
```

| Build | Output under `mobile/` |
| --- | --- |
| Debug APK | `build/app/outputs/flutter-apk/app-debug.apk` |
| Universal release APK | `build/app/outputs/flutter-apk/app-release.apk` |
| Split release APKs | `build/app/outputs/flutter-apk/app-armeabi-v7a-release.apk`, `app-arm64-v8a-release.apk`, `app-x86_64-release.apk` |
| Play Store AAB | `build/app/outputs/bundle/release/app-release.aab` |

`adb install -r` preserves data only when Android accepts the update's package/signature/version. A locally upload-key-signed APK may not update a Play-installed app signed by Google's different app-signing key. Prefer Play internal testing for that case; uninstalling deletes local drafts/data. An AAB cannot be installed directly with `adb install`.

## 5. Play Store signing and release

### Use the existing upload key

For updates to the existing Play application, restore the **registered upload key**, alias, and passwords. Do not generate a new key for each release. Gradle already signs release builds using `android/key.properties`; no separate `jarsigner` signing step is needed.

Inspect the existing certificate and export its public certificate if needed. These commands prompt for the keystore password:

```sh
keytool -list -v -keystore android/signing/upload-keystore.jks -alias YOUR_EXISTING_ALIAS
keytool -exportcert -rfc -keystore android/signing/upload-keystore.jks -alias YOUR_EXISTING_ALIAS -file /tmp/e4h-upload-certificate.pem
```

Compare the fingerprint with the **upload certificate** in Play Console's app integrity/signing settings. The public certificate is not the private keystore. With Play App Signing, the upload key authenticates uploaded bundles; Google uses the app-signing key for distributed APKs. See [Android app signing](https://developer.android.com/studio/publish/app-signing).

### Generate a key only for initial setup or an approved replacement

Skip this for normal updates. For an existing Play app, a different upload key requires the Play upload-key reset process before it can be used.

```sh
mkdir -p android/signing
keytool -genkeypair -v -keystore android/signing/NEW-upload-keystore.jks -storetype JKS -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

The command prompts for passwords and certificate details. Back up the key securely, then set the matching filename/alias/passwords in `key.properties`. Do not overwrite the current production keystore.

### Version and build the bundle

`pubspec.yaml` uses `version: NAME+CODE`. Every new Play upload needs an unused version code greater than previous uploads. Obtain the next code from Play Console; do not assume the repository's current code is the next available one.

Either update `pubspec.yaml` intentionally or override the version for a build:

```sh
flutter build appbundle --release --build-name=1.0.1 --build-number=YOUR_NEXT_UNUSED_INTEGER
jarsigner -verify -verbose -certs build/app/outputs/bundle/release/app-release.aab
keytool -printcert -jarfile build/app/outputs/bundle/release/app-release.aab
```

Replace both version examples with the intended release values. Verify the bundle is signed, and compare the signer fingerprint with the expected upload certificate. A self-signed upload certificate can cause certificate-chain warnings; an unsigned bundle or wrong fingerprint must not be uploaded. Flutter's official [Android release guide](https://docs.flutter.dev/deployment/android) covers AABs and signing.

### Test and publish

1. Confirm `.env` contains the intended production configuration and Firebase configuration belongs to the correct app/project.
2. Upload `app-release.aab` to the existing `org.e4h.asset` application's internal testing track, with release notes.
3. Check Play's bundle processing, signing/version acceptance, and reported compatibility issues.
4. Install through Play internal testing and verify login, MDMS loading/Retry, report submission, camera, and return-to-home navigation.
5. Promote the tested release using the team's Play Console release process. Monitor Crashlytics after rollout.

## 6. Troubleshooting

| Symptom | Check/action |
| --- | --- |
| Missing `.env` asset, wrong environment, or unexpected fallback URLs | Restore `.env` before commands, check deployment values, then stop/rebuild the app. |
| Google Services configuration missing or package mismatch | Restore `android/app/google-services.json` with an Android client for `org.e4h.asset`. |
| Signing-property cast/null error, including during debug builds | Supply all four keys in `android/key.properties`; the current Gradle script configures release signing unconditionally. |
| Keystore not found / wrong alias / password failure | Remember `storeFile` is relative to `android/app/`; inspect using `keytool -list` and restore the correct properties. |
| Java/Gradle compatibility failure | Check `flutter doctor -v` for the actual Java runtime and use JDK 17. Do not change the bytecode target to fix a runtime mismatch. |
| SDK, licenses, or NDK missing | Run Android license acceptance and install SDK 36/NDK `27.0.12077973`; check SDK paths. |
| `gradlew` missing on a fresh checkout | Use `flutter build apk --debug` after prerequisites; Flutter prepares the ignored wrapper files. Do not replace the committed Gradle version. |
| Build-runner conflicting/stale output | Stop watch mode, use `build_runner clean`, then regenerate with `--delete-conflicting-outputs`; review resulting tracked diffs. |
| Dependency or SDK constraint failure | Verify Flutter/Dart against the baseline and keep the lockfile. Resolve GitHub access for the Git dependency rather than upgrading packages. |
| Play rejects version code or certificate | Use an unused increasing version code and the registered upload key. New-key generation does not bypass Play's key-reset process. |
| Debug APK cannot update a release/Play install | Different signing certificates prevent an in-place update; use a suitable test device or the Play testing track without deleting unsynced user data. |
