package com.translator.provider.gemini;

import com.translator.config.GeminiProperties;
import com.translator.exception.GeminiApiException;
import com.translator.provider.gemini.dto.GeminiContent;
import com.translator.provider.gemini.dto.GeminiGenerateContentRequest;
import com.translator.provider.gemini.dto.GeminiGenerateContentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeminiClientTest {

    private final GeminiProperties geminiProperties = new GeminiProperties(
            "test-api-key", "gemini-2.5-flash", "https://example.com",
            Duration.ofSeconds(1), Duration.ofSeconds(1)
    );

    @Test
    void shouldReturnParsedResponseOnSuccess() {
        String body = """
                {"candidates":[{"content":{"parts":[{"text":"[]"}],"role":"model"},"finishReason":"STOP"}]}
                """;

        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .body(body)
                        .build()))
                .build();

        GeminiClient geminiClient = new GeminiClient(webClient, geminiProperties);

        GeminiGenerateContentResponse response = geminiClient.generateContent(
                new GeminiGenerateContentRequest(List.of(GeminiContent.userText("hi")), null));

        assertThat(response.candidates()).hasSize(1);
    }

    @Test
    void shouldThrowGeminiApiExceptionOnHttpErrorStatus() {
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .body("{\"error\":\"boom\"}")
                        .build()))
                .build();

        GeminiClient geminiClient = new GeminiClient(webClient, geminiProperties);

        assertThatThrownBy(() -> geminiClient.generateContent(
                new GeminiGenerateContentRequest(List.of(GeminiContent.userText("hi")), null)))
                .isInstanceOf(GeminiApiException.class);
    }
}
