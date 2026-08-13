# Changelog

All notable changes to this module will be documented in this file.

## Unreleased - 2026-07-28
- Added a README with the full API endpoint list for the service.

## 2025-07-03
- V2.0.0 deployment: added video upload and streaming support, including FFMpeg-based video processing, a storage service, and a Kafka-based video consumer.

## 2025-06-25
- Minor fix to the application startup class.

## 2025-06-24
- Fixed video processing and streaming issues.

## 2025-06-20
- Removed the async processing path after Kafka was found to deliver messages out of order.
- Fixed the video upload issue by updating Kafka properties.
- Fixed the image upload issue affecting IM Services and processor-service.

## 2025-05-12
- Added telemetry support.

## 2025-04-22 - Initial version
- Created the service as part of restructuring the municipal services into e4h-services.
