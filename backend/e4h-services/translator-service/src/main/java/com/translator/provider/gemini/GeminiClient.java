package com.translator.provider.gemini;

import com.translator.config.GeminiProperties;
import com.translator.exception.GeminiApiException;
import com.translator.provider.gemini.dto.GeminiGenerateContentRequest;
import com.translator.provider.gemini.dto.GeminiGenerateContentResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.concurrent.TimeoutException;

/**
 * Responsible ONLY for HTTP communication with the Gemini REST API.
 * Prompt construction and response mapping live in {@link GeminiTranslationProvider}.
 */
@Component
public class GeminiClient {

    private static final String GENERATE_CONTENT_PATH = "/v1beta/models/{model}:generateContent";
    private static final String API_KEY_HEADER = "x-goog-api-key";

    private final WebClient geminiWebClient;
    private final GeminiProperties geminiProperties;

    public GeminiClient(@Qualifier("geminiWebClient") WebClient geminiWebClient, GeminiProperties geminiProperties) {
        this.geminiWebClient = geminiWebClient;
        this.geminiProperties = geminiProperties;
    }

    public GeminiGenerateContentResponse generateContent(GeminiGenerateContentRequest request) {
        try {
            return geminiWebClient.post()
                    .uri(GENERATE_CONTENT_PATH, geminiProperties.model())
                    .header(API_KEY_HEADER, geminiProperties.apiKey())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiGenerateContentResponse.class)
                    .timeout(geminiProperties.readTimeout())
                    .block();
        } catch (WebClientResponseException e) {
            throw new GeminiApiException(
                    "Gemini API returned HTTP %s: %s".formatted(e.getStatusCode(), e.getResponseBodyAsString()), e);
        } catch (WebClientRequestException e) {
            throw new GeminiApiException("Gemini API network error: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof TimeoutException) {
                throw new GeminiApiException(
                        "Gemini API request timed out after " + geminiProperties.readTimeout(), e);
            }
            throw new GeminiApiException("Unexpected error calling Gemini API", e);
        }
    }
}
