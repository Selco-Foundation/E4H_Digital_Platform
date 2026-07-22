# Translation Service

Spring Boot 3 REST API that translates a list of words from a source language to a destination language, backed by a pluggable set of translation providers.

**Step 1** shipped an in-memory mock provider. **Step 2** adds a production Gemini-backed provider while keeping the controller and service fully provider-agnostic.

## Tech Stack

- Java 21
- Spring Boot 3.3.x
- Maven
- Spring WebFlux `WebClient` (HTTP client only — the app remains a Servlet/MVC app)
- Jakarta Bean Validation
- Apache POI (`.xlsx`/`.xls`) + Apache Commons CSV (`.csv`) for the spreadsheet upload endpoint
- JUnit 5 + Mockito
- SLF4J / Logback

## Architecture

```
controller  →  service  →  TranslationProviderFactory  →  TranslationProvider (interface)
                                                               │
                                                               ├── GeminiTranslationProvider   (implemented)
                                                               │        └── GeminiClient  →  Gemini REST API
                                                               ├── MockTranslationProvider      (Step 1, standalone)
                                                               ├── BhashiniTranslationProvider   (not implemented yet)
                                                               └── GoogleTranslationProvider     (not implemented yet)
```

| Layer | Responsibility |
|---|---|
| `controller` | HTTP mapping only; no business logic |
| `service` | Orchestrates translation; resolves the requested provider via `TranslationProviderFactory` |
| `provider` | `TranslationProvider` contract, `TranslationProviderType` enum, `TranslationProviderFactory` |
| `provider.gemini` | `GeminiTranslationProvider` (mapping) + `GeminiClient` (HTTP only) + Gemini-specific DTOs |
| `model` | Domain records (`TranslationResult`) |
| `dto` | Request / response / error payloads |
| `exception` | Custom exceptions + `@ControllerAdvice` global handler |
| `config` | `GeminiProperties` (`@ConfigurationProperties`) + Gemini `WebClient` bean with timeouts |

### SOLID / clean-architecture notes

- **S** — Controller, service, provider, and client each have a single responsibility (`GeminiClient` only does HTTP; `GeminiTranslationProvider` only maps).
- **O** — New providers (Bhashini, Google, ...) are added by implementing `TranslationProvider` and registering one entry in `TranslationProviderFactory`; no controller/service changes.
- **L** — Any `TranslationProvider` can substitute another via the factory.
- **I** — Narrow `TranslationProvider` contract (`translate(...)`, `getProviderName()`).
- **D** — `TranslationService` depends only on `TranslationProviderFactory` / `TranslationProvider`, never on a concrete provider like `GeminiTranslationProvider`.

## API

### `POST /translate`

**Request**

```json
{
  "sourceLanguage": "english",
  "destinationLanguage": "hindi",
  "provider": "gemini",
  "words": ["potato", "tomato", "water"]
}
```

`provider` is **optional**. Case-insensitive-by-contract values are `GEMINI`, `BHASHINI`, `GOOGLE` (deserialized directly into `TranslationProviderType`). If omitted, it defaults to `GEMINI`. An unrecognized value returns `400 Bad Request`.

**Response `200 OK`**

```json
{
  "sourceLanguage": "english",
  "destinationLanguage": "hindi",
  "translations": [
    { "source": "potato", "translated": "आलू" },
    { "source": "tomato", "translated": "टमाटर" },
    { "source": "water", "translated": "पानी" }
  ]
}
```

**Validation (`400 Bad Request`)**

- `sourceLanguage` must not be blank
- `destinationLanguage` must not be blank
- `words` must not be null or empty
- `provider`, if present, must be one of `GEMINI`, `BHASHINI`, `GOOGLE`

Example error body:

```json
{
  "timestamp": "2026-07-13T09:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "path": "/translate",
  "details": [
    "words: words must not be empty"
  ]
}
```

Requesting `BHASHINI` or `GOOGLE` today returns `501 Not Implemented` with message `"<name> provider not implemented yet"` until those providers are built. This applies to every endpoint (`/translate`, `/translate/rows`, `/translate/excel`) — all of them resolve the provider through the same `TranslationProviderFactory`, and `GlobalExceptionHandler` maps its `UnsupportedOperationException` to `501` in one place.

### `POST /translate/excel`

Uploads a spreadsheet and fills in every destination-language column it finds, returning the updated file for download. Multipart form fields:

| Field | Required | Description |
|---|---|---|
| `file` | yes | `.xlsx`, `.xls`, or `.csv` |
| `sourceLanguage` | yes | e.g. `english` |
| `destinationLanguage` | no | e.g. `hindi` — see below |
| `provider` | no | Same enum/defaulting rules as `/translate` |

Behavior:

- The **first column** is always treated as the source words, regardless of its header text (e.g. a column headed `English` works the same as one headed `Language A`).
- **Every other column with a non-blank header is treated as a destination-language column**, named after the actual language it holds (e.g. headers `Hindi`, `French`, `German`) — a sheet can carry any number of these at once.
- For each destination column, **only empty cells are filled in**; cells that already contain a value (a pre-existing translation) are left untouched.
- Columns with a blank header are ignored — they're not treated as a language column.
- `destinationLanguage` only matters if **no column for that language exists yet**: in that case a new column is appended and named after it (capitalized), then filled like any other. If a column for it already exists, or if it's omitted entirely, existing destination columns are processed as-is.
- If the sheet has no destination columns at all and `destinationLanguage` is omitted, there's nothing to translate — the endpoint returns `200` with the file unchanged (not an error).
- Output format matches input format — upload a `.csv`, get a `.csv` back; upload `.xlsx`/`.xls`, get an `.xlsx` back.
- The response is the file itself (`Content-Disposition: attachment`), not JSON.

Sample curl, filling in whatever destination columns `words.csv` already has:

```bash
curl -s -X POST http://localhost:8080/translate/excel \
  -F "file=@words.csv;type=text/csv" \
  -F "sourceLanguage=english" \
  -o words-translated.csv
```

Given `words.csv` with two destination columns, one partially filled and one empty:

```csv
English,Hindi,French
Potato,,Pomme de terre
Tomato,टमाटर,
```

the response fills in only the blanks:

```csv
English,Hindi,French
Potato,आलू,Pomme de terre
Tomato,टमाटर,Tomate
```

Note `Pomme de terre` and `टमाटर` were left exactly as uploaded — only the two empty cells were translated (via two separate calls to `TranslationService`, one per destination language actually needing a fill).

`ExcelTranslationService` reads/writes both formats into a common `List<List<String>>` grid, discovers destination columns from non-blank headers, and delegates one translation call per column-needing-fill to the same `TranslationService` used by `/translate` — provider selection stays exactly as provider-agnostic. Errors (empty file, unsupported extension, translation/word count mismatch) throw `ExcelProcessingException`, mapped to `400 Bad Request`.

### `POST /translate/rows`

The JSON counterpart to `/translate/excel` — same multi-destination-language model, no file involved. Takes a flat list of source words and a list of destination languages; the response has one row per word, with a field per destination language.

**Request**

```json
{
  "sourceLanguage": "english",
  "destinationLanguage": ["hindi", "french"],
  "provider": "gemini",
  "words": ["Potato", "Tomato"]
}
```

**Response `200 OK`**

```json
{
  "rows": [
    { "English": "Potato", "Hindi": "आलू", "French": "Pomme de terre" },
    { "English": "Tomato", "Hindi": "टमाटर", "French": "Tomate" }
  ]
}
```

Behavior notes:

- `provider` is optional, same defaulting/case-insensitivity as `/translate`, and applies to every destination language's translation call.
- `words` must not be null or empty. `destinationLanguage` must not be null or empty, and none of its entries may be blank.
- Each row's source field is keyed by the capitalized `sourceLanguage` (e.g. `English`); each destination language gets its own capitalized field (e.g. `Hindi`, `French`).
- One `TranslationService.translate(...)` call is made per destination language (translating the full word list each time) — different destination languages can differ, so they can't be batched into a single call.
- Errors (translation/word count mismatch for a given destination language) throw `RowTranslationException`, mapped to `400 Bad Request`.
- `RowTranslationService` and `ExcelTranslationService` share a small `LanguageNames.capitalize(...)` helper so both name generated destination fields/columns the same way, and both delegate the actual translation call to `TranslationService`.

## Provider selection

| `provider` value | Behavior |
|---|---|
| `GEMINI` | Routed to `GeminiTranslationProvider` |
| `BHASHINI` | `TranslationProviderFactory` throws `UnsupportedOperationException("Bhashini provider not implemented yet")` -> `501 Not Implemented` |
| `GOOGLE` | `TranslationProviderFactory` throws `UnsupportedOperationException("Google provider not implemented yet")` -> `501 Not Implemented` |
| *(omitted / null)* | Defaults to `GEMINI` |

All of this selection logic lives inside `TranslationProviderFactory` — the service just asks for a provider by `TranslationProviderType` and calls `translate(...)`. No `switch`/`if-else` provider dispatch exists in the service layer. `GlobalExceptionHandler` maps any `UnsupportedOperationException` to `501 Not Implemented` with the original message, so every endpoint responds consistently as new providers are added or requested before they're built.

## Gemini integration

```
GeminiTranslationProvider  →  GeminiClient  →  POST /v1beta/models/{model}:generateContent
```

- `GeminiClient` is responsible **only** for HTTP communication (auth header, request/response (de)serialization, error/timeout handling).
- `GeminiTranslationProvider` is responsible **only** for mapping: building the prompt + structured-output schema, and turning the Gemini response back into `List<TranslationResult>`.
- Authentication uses the `x-goog-api-key` header.
- The request asks Gemini for **structured JSON output** (`generationConfig.responseMimeType = application/json` + a `responseSchema` describing `[{ "source": string, "translated": string }]`), so the response is deserialized directly into DTOs — no markdown or free-text parsing.
- The prompt is generated dynamically from the request's `sourceLanguage`/`destinationLanguage`/`words` — nothing is hardcoded.

### Configuration

`src/main/resources/application.yml`:

```yaml
gemini:
  apiKey: ${GEMINI_API_KEY}
  model: gemini-flash-latest
  baseUrl: https://generativelanguage.googleapis.com
  connectTimeout: 5s
  readTimeout: 20s
```

Set the `GEMINI_API_KEY` environment variable before starting the service. The API key is never logged.

For local development, you can instead drop a `.env` file (git-ignored — see `.env.example`) next to `pom.xml`:

```
GEMINI_API_KEY=your-gemini-api-key-here
```

`DotenvEnvironmentPostProcessor` (`config` package) loads it into the Spring `Environment` at startup, registered via `META-INF/spring.factories`. Real OS environment variables still take precedence over `.env`.

### Error handling

`GeminiClient` and `GeminiTranslationProvider` translate every failure mode into a specific, meaningful exception (never `null`):

| Exception | Raised when |
|---|---|
| `GeminiApiException` | Non-2xx HTTP status, network failure, or request timeout |
| `GeminiResponseParsingException` | Missing/empty candidates, blocked response (`promptFeedback.blockReason`), no text content, or invalid/empty JSON payload |

Both extend `TranslationException`, so they're already handled by the existing `@ControllerAdvice` (`500` with a descriptive message).

### Logging

Per request, the service and Gemini provider log:

- Provider selected
- Source / destination language
- Number of words
- Gemini model used
- Execution time (ms)

API keys are never logged.

## Project structure

```
src/main/java/com/translator/
├── TranslationServiceApplication.java
├── config/
│   ├── DotenvEnvironmentPostProcessor.java  (loads local .env, see Gemini config above)
│   ├── GeminiClientConfig.java              (Gemini WebClient bean + timeouts)
│   └── GeminiProperties.java                (@ConfigurationProperties "gemini")
├── controller/
│   ├── ExcelTranslationController.java      (POST /translate/excel, multipart)
│   └── TranslationController.java
├── dto/
│   ├── ErrorResponse.java
│   ├── TranslationItemDto.java
│   ├── TranslationRequest.java        (carries optional `provider`)
│   ├── TranslationResponse.java
│   ├── TranslationRowRequest.java     (row-object input for /translate/rows)
│   └── TranslationRowResponse.java
├── exception/
│   ├── ExcelProcessingException.java
│   ├── GeminiApiException.java
│   ├── GeminiResponseParsingException.java
│   ├── GlobalExceptionHandler.java
│   ├── ProviderNotFoundException.java
│   ├── RowTranslationException.java
│   └── TranslationException.java
├── model/
│   └── TranslationResult.java
├── provider/
│   ├── MockTranslationProvider.java
│   ├── StringToTranslationProviderTypeConverter.java  (case-insensitive @RequestParam binding)
│   ├── TranslationProvider.java
│   ├── TranslationProviderFactory.java
│   ├── TranslationProviderType.java   (GEMINI / BHASHINI / GOOGLE)
│   └── gemini/
│       ├── GeminiClient.java          (HTTP only)
│       ├── GeminiTranslationProvider.java (mapping only)
│       └── dto/                       (Gemini-only request/response DTOs)
└── service/
    ├── ExcelTranslationService.java   (spreadsheet read/write, delegates to TranslationService)
    ├── LanguageNames.java             (shared "hindi" -> "Hindi" helper)
    ├── RowTranslationService.java     (multi-destination-language word list, delegates to TranslationService)
    ├── TranslatedSpreadsheet.java     (bytes + filename + content type)
    └── TranslationService.java

src/test/java/com/translator/
├── controller/
│   ├── ExcelTranslationControllerTest.java
│   └── TranslationControllerTest.java  (covers /translate and /translate/rows)
├── provider/
│   ├── MockTranslationProviderTest.java
│   ├── TranslationProviderFactoryTest.java
│   └── gemini/
│       ├── GeminiClientTest.java
│       └── GeminiTranslationProviderTest.java
└── service/
    ├── ExcelTranslationServiceTest.java
    ├── RowTranslationServiceTest.java
    └── TranslationServiceTest.java
```

## How to run

Prerequisites: JDK 21+, Maven 3.9+, a `GEMINI_API_KEY` environment variable.

```bash
cd Translator/Backend
export GEMINI_API_KEY=your-key-here
mvn spring-boot:run
```

Or build and run the jar:

```bash
mvn clean package
GEMINI_API_KEY=your-key-here java -jar target/translation-service-1.0.0-SNAPSHOT.jar
```

The service listens on `http://localhost:8080`.

### Sample curl

```bash
curl -s -X POST http://localhost:8080/translate \
  -H "Content-Type: application/json" \
  -d '{
    "sourceLanguage": "english",
    "destinationLanguage": "hindi",
    "provider": "gemini",
    "words": ["potato", "tomato", "water"]
  }'
```

Omitting `provider` behaves identically (defaults to `gemini`).

Sample curl for the multi-destination-language JSON endpoint:

```bash
curl -s -X POST http://localhost:8080/translate/rows \
  -H "Content-Type: application/json" \
  -d '{
    "sourceLanguage": "english",
    "destinationLanguage": ["hindi", "french"],
    "words": ["Potato", "Tomato"]
  }'
```

## How to test

Run all unit tests:

```bash
mvn test
```

Covered areas:

- `MockTranslationProvider` — dictionary hits, misses, case-insensitivity (Step 1, unchanged)
- `TranslationProviderFactory` — GEMINI resolution, null-defaulting to GEMINI, `UnsupportedOperationException` for BHASHINI/GOOGLE
- `GeminiTranslationProvider` — prompt construction, structured-JSON mapping, and every error path (no candidates, blocked, invalid JSON, empty list, no text) — `GeminiClient` is mocked, no real API calls are made
- `GeminiClient` — success and HTTP-error mapping against a stubbed `WebClient` exchange function, no real network calls
- `TranslationService` — factory resolution (explicit + default), DTO mapping (Mockito)
- `TranslationController` — happy path, validation `400`s, and unknown-provider `400` (`@WebMvcTest`)
- `ExcelTranslationService` — xlsx and csv translation, filling only empty cells across multiple destination columns while leaving pre-filled ones untouched, destination-column reuse vs. creation, blank-header columns ignored, first-column-by-position matching, no-op success when there's nothing to translate, and error paths (empty file, unsupported extension, count mismatch) — `TranslationService` is mocked
- `ExcelTranslationController` — multipart upload happy path, missing-provider default, missing-destinationLanguage default, blank-field and unknown-provider `400`s (`@WebMvcTest`)
- `RowTranslationService` — one row per word across multiple destination languages, provider passed through to every call, and error paths (count mismatch) — `TranslationService` is mocked
- `TranslationController` (rows tests) — happy path and validation `400`s for `/translate/rows` (`@WebMvcTest`)

## Adding a new provider (e.g. Bhashini or Google Translate)

No controller changes are required, and the service needs zero changes.

1. Implement `TranslationProvider`:

```java
@Component
public class BhashiniTranslationProvider implements TranslationProvider {

    @Override
    public String getProviderName() {
        return TranslationProviderType.BHASHINI.name();
    }

    @Override
    public List<TranslationResult> translate(
            String sourceLanguage,
            String destinationLanguage,
            List<String> words) {
        // Call the Bhashini API and map its response to List<TranslationResult>
    }
}
```

2. Inject it into `TranslationProviderFactory` and replace its `BHASHINI` entry:

```java
public TranslationProviderFactory(
        GeminiTranslationProvider geminiTranslationProvider,
        BhashiniTranslationProvider bhashiniTranslationProvider) {
    this.providerSuppliers = Map.of(
            TranslationProviderType.GEMINI, () -> geminiTranslationProvider,
            TranslationProviderType.BHASHINI, () -> bhashiniTranslationProvider,
            TranslationProviderType.GOOGLE, () -> {
                throw new UnsupportedOperationException("Google provider not implemented yet");
            }
    );
}
```

3. Clients call it the same way as always — just set `"provider": "bhashini"` in the request body.
