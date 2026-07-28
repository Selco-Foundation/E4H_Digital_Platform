# Licenses and dependencies

This page inventories the third-party libraries and platform components used across the E4H Digital Platform, flags which are permissive open source, copyleft/source-available, or commercial/managed services, and marks whether each is free to use or has an associated cost. It is derived from the manifests in the repository (`package.json`, `pom.xml`, `pubspec.yaml`, `requirements.txt`) plus the infrastructure list in [Deployment](deployment.md). Versions, licenses, and pricing can drift as dependencies are upgraded — treat this as a snapshot, and re-check the manifest/vendor pricing page before relying on a specific status for compliance or budgeting purposes.

The platform's own code (`backend`, `frontend`, `mobile`) is MIT-licensed — see the repository [`LICENSE`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/LICENSE).

**"Free" here means free to use as consumed in this codebase** (self-hosted open-source software with no license fee). It does not mean zero infrastructure cost — self-hosting still consumes compute/storage that AWS bills for. **"Paid"** means the dependency itself is a metered API, a managed cloud service, or a licensed edition with a vendor fee.

## Summary by layer

| Layer | Predominant license model | Cost | Notable exceptions |
| --- | --- | --- | --- |
| Backend (Java/Maven) | Apache License 2.0, MIT | Free — all self-contained libraries, no license fees | `h2` (EPL 1.0/MPL 2.0 dual-license, test-only) |
| Frontend (React) | MIT, ISC, BSD | Free, except the Google Maps API call | Google Maps JS API loader is Apache-2.0 code but the underlying Google Maps service is a metered/commercial API |
| Mobile (Flutter/Dart) | BSD-3-Clause, MIT | Free, except Firebase at scale | Firebase SDKs are Apache-2.0, but Firebase itself is a commercial Google Cloud service (free tier, paid beyond it) |
| Infrastructure/backbone | Mixed | Mixed — self-hosted OSS is free; AWS-managed pieces are paid | MinIO server is AGPL-3.0 (free, self-hosted); Terraform is BUSL 1.1 (source-available, not OSI-approved, still free for this use); AWS EKS/RDS/S3 are paid managed services |
| Platform's own code | MIT | Free | eGov/DIGIT and Selco modules are open source but authored/maintained by this project's ecosystem, not independent third parties |

---

## Backend (Java / Maven)

Extracted from the 21 `pom.xml` files under `backend/core-services` and `backend/e4h-services`. All of these are free, self-hosted libraries with no runtime license fee — cost only enters via the infrastructure they run on (see the Infrastructure section below).

### Open source — permissive (Apache 2.0 / MIT / BSD)

| Library | Example version(s) | License | Paid or free |
| --- | --- | --- | --- |
| Spring Framework / Spring Boot / Spring Data / Spring Kafka | 5.x / Boot 3.x / 2.4.5 | Apache 2.0 | Free |
| Apache Commons (`commons-lang`, `commons-lang3`, `commons-io`, `commons-collections4`) | various | Apache 2.0 | Free |
| Apache HttpClient / HttpClient5 | 4.5.3 | Apache 2.0 | Free |
| Apache POI / POI-OOXML | 3.10–3.17 | Apache 2.0 | Free |
| Apache Tika (`tika-core`) | 2.9.1 | Apache 2.0 | Free |
| Apache Lucene-based **Elasticsearch client** (`elasticsearch`, `elasticsearch-rest-high-level-client`) | 6.5.4 | Apache 2.0 (this client version predates Elastic's 2021 license change — see infra note below) | Free |
| ODF Toolkit (`odfdom-java`) | 0.8.6 | Apache 2.0 | Free |
| Everit JSON Schema | 1.5.1 | Apache 2.0 | Free |
| Jackson (core, databind, datatype-jsr310, dataformat-yaml) | 2.7.4–2.9.8 | Apache 2.0 | Free |
| Gson | 2.8.0 | Apache 2.0 | Free |
| json-path, json-smart, json-flattener | various | Apache 2.0 | Free |
| Resilience4j (`resilience4j-retry`, `-spring-boot3`) | 2.1.0 | Apache 2.0 | Free |
| OpenTelemetry BOMs | 1.35.0 / 2.1.0-alpha | Apache 2.0 | Free (the SDK is free; an APM/observability backend to send telemetry to, if added later, may be paid) |
| Swagger / springdoc-openapi | 1.5.18 / 1.6.14 / 2.2.8 | Apache 2.0 | Free |
| cache2k-spring | 1.2.0.Final | Apache 2.0 | Free |
| Javers (`javers-core`) | 3.1.0 | Apache 2.0 | Free |
| Flyway Core | 6.4.3 / 9.22.3 | Apache 2.0 (Community edition) | Free — Community edition is used here; Flyway Teams/Enterprise (extra DB engines, undo, drift detection) is a paid Redgate tier, not referenced in these `pom.xml` files |
| AWS Java SDK (`aws-java-sdk-s3`) | 1.11.289 | Apache 2.0 | Free (the SDK itself); calls made through it hit paid AWS S3, billed by AWS — see Infrastructure section |
| Azure Storage SDK | 5.0.0 | MIT | Free (the SDK itself); calls made through it hit paid Azure Storage, billed by Microsoft if this integration is active |
| MinIO Java client | 7.1.4 / 8.4.3 | Apache 2.0 (client library — the MinIO **server**, deployed as backbone infra, is AGPL-3.0; see below) | Free |
| PostgreSQL JDBC driver | 42.7.x | BSD-2-Clause | Free |
| Redis client (Jedis) | — | MIT | Free |
| jsoup | 1.10.2 | MIT | Free |
| Project Lombok | 1.18.x | MIT | Free |
| Java-util (Cedar Software) | 1.63.0 | Apache 2.0 | Free |
| JUnit | 4.13.2 | EPL 1.0 | Free |
| AspectJ | 1.8.10 | EPL 1.0 | Free |
| Hibernate Validator | 6.0.16.Final | Apache 2.0 | Free |
| jakarta/javax validation API | 3.0.2 | EPL 2.0 / EDL | Free |

### Dual/weak-copyleft (test scope only)

| Library | License | Paid or free | Note |
| --- | --- | --- | --- |
| H2 Database (`h2`) | EPL 1.0 or MPL 2.0 (dual, pick either) | Free | Used as an in-memory test database, not shipped to production |

### eGov / DIGIT platform artifacts (open source, internal ecosystem)

`org.egov:enc-client`, `org.egov:mdms-client`, `org.egov.common:health-services-common`, `org.egov.common:health-services-models`, `org.egov.services:digit-models`, `org.egov.services:services-common`, `org.egov.services:tracer`, `org.egov.works:works-services-common`.

These are eGovernments Foundation DIGIT libraries (Apache 2.0 / MIT, consistent with the [DIGIT-core](https://github.com/egovernments/DIGIT-core) project) — **free**, no license fee. Several are pinned to `-SNAPSHOT` versions, meaning the exact code was pulled from an internal/fork build rather than a tagged release — confirm the resolved snapshot if a license audit needs exact provenance.

---

## Frontend (React / micro-ui, installation-ui)

Extracted from `frontend/micro-ui/web` and `frontend/installation-ui/web`, including their `micro-ui-internals` Yarn workspaces (see [Modules and dependencies](../frontend/modules-and-dependencies.md) for how those workspaces are wired).

### Open source — permissive (MIT / BSD / ISC) — all free

| Category | Packages | Paid or free |
| --- | --- | --- |
| Core framework | `react`, `react-dom` 17.0.2, `react-router-dom`, `react-redux`, `redux`, `redux-thunk` | Free |
| Forms | `react-hook-form`, `@hookform/resolvers`, `json-schema-to-yup` | Free |
| Data/state | `react-query`, `axios`, `jsonpath` | Free |
| i18n | `i18next`, `react-i18next`, `i18next-react-postprocessor` | Free |
| UI components | `react-select`, `react-datepicker`, `react-date-range`, `react-time-picker`, `react-tooltip`, `react-table`, `react-simple-maps`, `react-responsive`, `react-drag-drop-files`, `react-inlinesvg`, `rooks`, `recharts` | Free |
| Media/export | `xlsx` (SheetJS community edition, Apache 2.0), `pdfmake`, `html2canvas`, `dom-to-image`, `hls.js`, `react-player` | Free |
| Build tooling | `react-scripts`, `webpack`, `webpack-cli`, `babel-loader`, `css-loader`, `style-loader`, `postcss` + plugins, `sass`, `node-sass`, `tailwindcss`, `gulp` + plugins | Free |
| Dev/test tooling | `storybook` packages, `prettier`, `husky`, `lint-staged`, `eslint` (via `react-app` config) | Free |
| Date/util | `date-fns`, `lodash`, `lodash.merge` | Free |

All of the above use MIT, BSD, or ISC licenses, which is standard for the React/webpack ecosystem — no copyleft or commercial terms apply, and none require a paid tier at any usage volume.

### Third-party service dependency (not just a library)

| Package | License of the code | Paid or free | Service consideration |
| --- | --- | --- | --- |
| `@googlemaps/js-api-loader` | Apache 2.0 | **Paid** above free tier | The loader code is free/open source, but it loads the Google Maps JavaScript API, which requires a Google Cloud API key and is billed per usage (map loads, geocoding, etc.) once the monthly free credit is exceeded. |

### eGov / DIGIT and Selco platform packages (open source, internal ecosystem) — all free

`@egovernments/digit-ui-libraries`, `@egovernments/digit-ui-react-components`, `@egovernments/digit-ui-components`, `@egovernments/digit-ui-css`, `@egovernments/digit-ui-components-css`, `@egovernments/digit-ui-svg-components`, `@egovernments/digit-ui-module-*` (common, engagement, utilities, workbench, hrms, pgr, dss, open-payment, sandbox), `@upyog/digit-ui-react-components`.

All published as MIT (matching the [DIGIT-Frontend](https://github.com/egovernments/DIGIT-Frontend) project they're derived from) — free, no license fee.

`@selco/digit-ui-module-core`, `@selco/digit-ui-module-dss`, `@selco/digit-ui-module-hrms`, `@selco/digit-ui-module-pgr`, `@selco/digit-ui-react-components`, `@selco/installation-ui-css`, `@selco/selco-css`.

These are Selco's own modules (declared `"license": "MIT"` in their local `package.json`), built on top of the DIGIT UI framework — free, since they're this project's own code. They are published to the public npm registry but are authored and maintained as part of this project, not an independent third party.

---

## Mobile (Flutter / Dart)

Extracted from `mobile/pubspec.yaml` (Flutter `3.22.2`, Dart `3.4.3` per [Mobile setup](../getting-started/mobile-setup.md); the `mobile/` directory itself is not present in this checkout — verified against a full platform checkout).

### Open source — permissive (BSD-3-Clause / MIT / Apache 2.0) — all free

| Category | Packages | Paid or free |
| --- | --- | --- |
| State management | `flutter_bloc` | Free |
| Networking | `dio`, `pretty_dio_logger`, `connectivity_plus`, `internet_connection_checker_plus` | Free |
| Local persistence | `drift`, `drift_db_viewer`, `isar`, `isar_flutter_libs`, `shared_preferences`, `flutter_secure_storage` | Free |
| Device/media | `location`, `permission_handler`, `disable_battery_optimization`, `image_picker`, `video_player`, `open_file`, `flutter_pdfview`, `cached_network_image`, `qr_flutter` | Free |
| Code generation | `auto_route`/`auto_route_generator`, `build_runner`, `freezed`/`freezed_annotation`, `json_serializable`/`json_annotation`, `dart_mappable`/`dart_mappable_builder`, `isar_generator`, `drift_dev`, `recase` | Free |
| UI/misc | `badges`, `shimmer`, `fluttertoast`, `flutter_keyboard_visibility`, `flutter_local_notifications`, `flutter_background_service`, `reactive_forms`, `flutter_dotenv`, `package_info_plus`, `flutter_lints`, `patch_package` | Free |

These follow the standard pub.dev convention (BSD-3-Clause is the most common, with some MIT/Apache 2.0) — all permissive, no copyleft terms, and no paid tier for any of them.

### Third-party / commercial service dependency

| Package | License of the code | Paid or free | Service consideration |
| --- | --- | --- | --- |
| `firebase_core`, `firebase_analytics` | Apache 2.0 (SDK code) | Free tier, paid beyond it | The SDK is free. Firebase Analytics itself is unlimited and free on Google's Spark plan; other Firebase products (if adopted later, e.g. Cloud Functions, Crashlytics-adjacent hosting) bill under the Blaze pay-as-you-go plan. Usage is governed by Google's Firebase Terms of Service, and analytics data flows to Google infrastructure. |

### DIGIT/Selco Flutter packages (open source, internal ecosystem) — all free

`digit_ui_components`, `digit_data_model`, `digit_dss`, `digit_scanner`, `digit_forms_engine` — published from the eGovernments Foundation's [health-campaign-field-worker-app](https://github.com/egovernments/health-campaign-field-worker-app) monorepo (the dev dependency `dart_mappable_builder` is pulled directly from that repo's `master` branch via a git dependency, not a published pub.dev version — pin/verify the commit before relying on it for a production build). These are open source and free but part of the same DIGIT ecosystem this platform builds on, not independent third parties.

---

## Other (build scripts, infrastructure, and platform services)

### Small standalone scripts

`backend/docs/cron/requirements.txt` — `requests==2.23.0` (Apache 2.0) — **Free**.

### Infrastructure / backbone (per [Deployment](deployment.md))

These aren't code dependencies pulled into a build artifact — they're services the platform runs against, deployed via Helm/Helmfile per the release manifest. License model and cost both vary far more here than in the application code above. Where a row bundles the open-source engine with an AWS-managed equivalent, they're split so "paid or free" is unambiguous.

| Component | License | Paid or free | Consideration |
| --- | --- | --- | --- |
| PostgreSQL (engine) | PostgreSQL License (permissive, OSI-approved) | Free (self-hosted) | Open source database engine |
| AWS RDS for PostgreSQL | Proprietary (AWS managed service) | **Paid** | Billed by AWS per instance-hour, storage, and I/O if RDS is used instead of self-hosted Postgres |
| Apache Kafka | Apache 2.0 | Free (self-hosted via the Helm chart) | Would be paid if swapped for a managed offering (e.g. AWS MSK, Confluent Cloud) — not indicated in this repo |
| Redis | BSD-3-Clause for versions ≤ 7.2; Redis 7.4+ (released March 2024) moved to a dual RSALv2/SSPLv1 source-available license, not OSI-approved | Free (self-hosted) either way | Confirm the deployed Redis image/version; the BSD-licensed Valkey fork is a free alternative if license terms matter. Would be paid if swapped for AWS ElastiCache — not indicated here |
| Elasticsearch / Kibana | Apache 2.0 through v7.10; Elastic License 2.0/SSPL from v7.11 (2021 relicense), with an AGPL-3.0 option reintroduced in 2024 | Free (self-hosted via Helm) | The only version pinned in this repo is the Java **client** library (6.5.4, Apache 2.0) — confirm the deployed **server** version/image before asserting a license. Would be paid if swapped for Elastic Cloud — not indicated here |
| MinIO | **AGPL-3.0** (server) | Free (self-hosted, community edition) | Copyleft with network-use provisions — distinct from the Apache-2.0-licensed MinIO client SDK used in backend code above. A paid MinIO commercial/enterprise license exists but isn't referenced in this repo; AWS S3 (used elsewhere in the stack) is the paid managed alternative |
| ingress-nginx | Apache 2.0 | Free | Self-hosted |
| cert-manager | Apache 2.0 | Free | Self-hosted; issues certs via free ACME providers (e.g. Let's Encrypt) unless configured otherwise |
| pgAdmin | PostgreSQL License | Free | Self-hosted |
| Terraform | **BUSL 1.1** (Business Source License, HashiCorp relicense, Aug 2023) | Free for this use | Source-available, not OSI-approved open source; usage restrictions only apply to competing commercial offerings, so free for internal infra provisioning. Terraform Cloud/Enterprise (not referenced in this repo) is a paid tier. OpenTofu (MPL 2.0) is the open-source fork if this matters |
| Helm / Helmfile | Apache 2.0 | Free | Self-hosted tooling |
| Kubernetes (engine) | Apache 2.0 | Free (the software) | Open source orchestrator |
| AWS EKS (managed control plane) | Proprietary (AWS managed service) | **Paid** | Billed per cluster-hour plus the worker node compute (EC2/Fargate) |
| AWS (RDS, S3, IAM, VPC/NAT via Terraform) | Proprietary commercial cloud service | **Paid** | Pay-as-you-go; not open source |
| Docker | Apache 2.0 (Docker Engine/Moby) | Free | Docker Engine/CLI, as used in the CI build images under `build/`, is free. Docker Desktop has separate paid terms for larger organizations (>250 employees or >$10M revenue), which is a different product not referenced here |

## How to keep this current

- Re-run the extraction against `pom.xml`, `package.json`, and `pubspec.yaml` files whenever dependencies are upgraded, especially around major version bumps (Elasticsearch, Redis, Terraform, and MinIO have all changed licenses in recent years).
- For a full transitive-dependency audit (not just direct dependencies listed here), use `mvn license:aggregate-third-party-report` for backend modules and `npx license-checker` / `flutter pub deps` for frontend and mobile.
- Cross-check infra versions against `deploy-as-code/charts/product-release-charts/dependency_chart-selco-v3.11.35.yaml` (or the current release manifest) rather than assuming versions from this page.
- Re-verify "paid or free" status against current AWS/Google/Firebase/MinIO/HashiCorp pricing pages before using this page for budget or procurement decisions — pricing and free-tier limits change independently of the code.
