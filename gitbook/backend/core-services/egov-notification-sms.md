# eGov Notification SMS

## Purpose

The notification SMS service consumes SMS messages from Kafka and sends them through a configured SMS provider.

## Source location

- Service path: [`backend/core-services/egov-notification-sms`](https://github.com/Selco-Foundation/E4H_Digital_Platform/tree/add-gitbook-docs/backend/core-services/egov-notification-sms)
- README: [`backend/core-services/egov-notification-sms/README.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/core-services/egov-notification-sms/README.md)
- Changelog: [`backend/core-services/egov-notification-sms/CHANGELOG.md`](https://github.com/Selco-Foundation/E4H_Digital_Platform/blob/add-gitbook-docs/backend/core-services/egov-notification-sms/CHANGELOG.md)

## Responsibilities

- Consumes notification messages from the configured Kafka topic.
- Sends SMS messages to the target mobile number.
- Supports multiple provider implementations.
- Allows response verification and success/failure handling through configuration.

## Async behavior

This service is a Kafka consumer and does not expose a REST layer for normal integrations. Producers publish JSON messages to the topic configured by `kafka.topics.notification.sms.name`.

## Provider implementations

The README documents these provider options:

- `Console`: prints mobile number and message to logs.
- `Generic`: configurable provider integration using GET or POST, query params, form data, or JSON body.
- `MSDG`: provider-specific implementation.

Provider selection is controlled by `sms.provider.class`.

## Configuration notes

Important configuration areas include:

- Provider URL and request method.
- Content type.
- Parameter mapping with `sms.config.map`.
- Category mapping with `sms.category.map`.
- Response verification settings such as `sms.verify.response`, `sms.success.codes`, and `sms.error.codes`.

## Operational notes

Use this service for SMS notifications emitted by workflows such as incident management and user-facing status changes. When notifications fail, check Kafka messages, provider credentials, response verification settings, and provider-specific response codes.
