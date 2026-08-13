# Processor Services

## Purpose

Kafka-driven background worker that transcodes uploaded videos into multi-quality HLS (HTTP Live Streaming) renditions. It listens for video-processing requests, fetches the source file from the eGov file-store service, runs `ffprobe`/`ffmpeg` to inspect and transcode the video into 144p-1080p (plus original) HLS playlists/segments, and uploads the resulting `.m3u8`/`.ts` files back to the file-store's HLS endpoint. It has no REST API or database of its own — all durable state (source video, output segments) lives in the file-store service.

## Source location

- Service path: [`backend/e4h-services/processor-services`](https://github.com/Selco-Foundation/E4H_Digital_Platform/tree/add-gitbook-docs/backend/e4h-services/processor-services)
- README: [`backend/e4h-services/processor-services/README.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/processor-services/README.md)
- Build file: [`backend/e4h-services/processor-services/pom.xml`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/e4h-services/processor-services/pom.xml)

## Service dependencies

- **egov-filestore** — source video is fetched via `GET {filestore.host}/filestore/v1/files/id`; processed HLS output is uploaded via the file-store's HLS upload endpoint.
- **Kafka** — consumes video-processing requests; no results/events are published back to Kafka.
- **ffmpeg / ffprobe** — external CLI binaries invoked via `Runtime.exec`/`ProcessBuilder` (`FFMpegExecutor`, `VideoUtil`). Not bundled with the service — must be installed on the runtime host/image and reachable on `PATH` (`ffprobe` path is explicitly configurable; `ffmpeg` is invoked by bare command name).
- **org.egov.services:tracer** — shared eGov library providing `CustomException`, request tracing (`TracerConfiguration`), and OpenTelemetry Kafka instrumentation.
- Local filesystem — uses `input/` and `output/` working directories (under the process working directory) as scratch space for temp downloads and ffmpeg output; both are cleaned up after each video is processed.

Note: despite the method name `StorageUtil.getMultipartFileFromS3`, there is no AWS S3 (or other cloud storage) SDK dependency in this service — the method fetches files from egov-filestore over HTTP via `RestTemplate`/`ServiceRequestRepository`. Treat the name as legacy/misleading rather than indicating an actual S3 integration.

## Responsibilities

- Consumes video-processing requests from Kafka and resolves the referenced source file(s) from egov-filestore.
- Runs `ffprobe` to inspect source video and `ffmpeg` to transcode it into multiple HLS quality renditions (144p-1080p, plus the original).
- Uploads generated `.m3u8`/`.ts` HLS output back to egov-filestore's HLS upload endpoint.
- Skips non-video files (anything not ending in `.mp4`, `.avi`, `.mov`, `.wmv`) entirely.
- Cleans up local `input/`/`output/` scratch directories after each video is processed.

## API surface

None — this is a Kafka consumer worker with no REST surface. Spring Boot Actuator is on the classpath and exposed (`management.endpoints.web.exposure.include=*`, base path `/`), so operational endpoints such as `/health` and Kafka health checks (`management.health.kafka.enabled=true`) are available, but there are no application-defined REST controllers.

## Events

**Consumes:**

- Topic: `${im.kafka.process.video.topic}` (default `process-im-video-request`), consumer group `egov-im-services` (`VideoConsumer.listen`).
- Payload deserializes into `StorageProcessingContext`, containing a `StorageResponse` (list of `{tenantId, fileStoreId}` file references) and a `ProcessingContext` (`videoId`, `module`, `tag`, `tenantId`, `requestInfo`).
- Consumer is tuned for long-running work per message: `spring.kafka.consumer.max-poll-records=1`, `spring.kafka.consumer.max-poll-interval-ms=1800000` (30 min). Failures are logged and swallowed (not rethrown), relying on Kafka's own retry/offset-commit configuration rather than a dead-letter/produced error event.

**Produces:**

- No Kafka messages are published by this service (no `KafkaTemplate` usage found in source) — the only observable side effect after successful processing is the HTTP upload of transcoded HLS files to egov-filestore's HLS upload endpoint (`egov.filestore.hls.upload.endpoint`). Downstream consumers must poll/query file-store (or another service) for the result; there is no completion event on this service's side.

## Configuration

Defined in `src/main/resources/application.properties` and bound in `ProcessorConfiguration` (`@Value`-injected):

| Property | Purpose |
|---|---|
| `spring.application.name`, `server.port` (8099), `server.servlet.context-path` (`/processor-services`) | Basic app identity |
| `kafka.config.bootstrap_server_config` | Kafka bootstrap servers |
| `spring.kafka.consumer.*` | Deserializers (`HashMapDeserializer` for values), consumer group, resilience tuning for long video-processing jobs |
| `im.kafka.process.video.topic` | Kafka topic this service consumes |
| `egov.filestore.host`, `egov.filestore.upload.endpoint`, `egov.filestore.hls.upload.endpoint` | File-store base URL and endpoints for plain vs. HLS uploads/fetches |
| `ffprobe.path` | Absolute path to the `ffprobe` binary (`ffmpeg` itself is invoked by bare name, assumed on `PATH`) |
| `management.endpoints.web.exposure.include`, `management.endpoint.health.show-details`, `management.health.kafka.enabled` | Actuator/health exposure |
| `otel.*` | OpenTelemetry traces exporter (OTLP to `jaeger-collector.tracing`), service name, Kafka instrumentation |

No secrets are stored in `application.properties` in this service; all values shown are non-sensitive URLs/paths.

## Operational notes

- Requires Java 17, Maven, and `ffmpeg`/`ffprobe` installed and reachable on the host (or `ffprobe.path` set to the actual binary location) to run locally or in any deployment target.
- A local Kafka broker (`kafka.config.bootstrap_server_config`, default `localhost:9092`) and a reachable egov-filestore instance (`egov.filestore.host`, default `http://localhost:8083`) are required for the consumer to receive and process real video jobs.
- Consumed messages that fail processing are logged and swallowed rather than rethrown or dead-lettered — failure handling relies on Kafka's own retry/offset-commit behavior.
