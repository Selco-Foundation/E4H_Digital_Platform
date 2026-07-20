package com.translator.provider.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.translator.config.GeminiProperties;
import com.translator.exception.GeminiResponseParsingException;
import com.translator.model.TranslationResult;
import com.translator.provider.gemini.dto.GeminiCandidate;
import com.translator.provider.gemini.dto.GeminiContent;
import com.translator.provider.gemini.dto.GeminiGenerateContentRequest;
import com.translator.provider.gemini.dto.GeminiGenerateContentResponse;
import com.translator.provider.gemini.dto.GeminiPart;
import com.translator.provider.gemini.dto.GeminiPromptFeedback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeminiTranslationProviderTest {

    @Mock
    private GeminiClient geminiClient;

    private GeminiTranslationProvider geminiTranslationProvider;

    @BeforeEach
    void setUp() {
        GeminiProperties geminiProperties = new GeminiProperties(
                "test-api-key", "gemini-2.5-flash", "https://example.com",
                Duration.ofSeconds(5), Duration.ofSeconds(15)
        );
        geminiTranslationProvider = new GeminiTranslationProvider(geminiClient, geminiProperties, new ObjectMapper());
    }

    @Test
    @DisplayName("should report GEMINI as the provider name")
    void shouldReturnGeminiAsProviderName() {
        assertThat(geminiTranslationProvider.getProviderName()).isEqualTo("GEMINI");
    }

    @Test
    @DisplayName("should map Gemini's structured JSON response to translation results")
    void shouldMapStructuredJsonResponseToTranslationResults() {
        String json = "[{\"source\":\"potato\",\"translated\":\"आलू\"},{\"source\":\"tomato\",\"translated\":\"टमाटर\"}]";
        when(geminiClient.generateContent(any())).thenReturn(responseWithText(json));

        List<TranslationResult> results = geminiTranslationProvider.translate(
                "english", "hindi", List.of("potato", "tomato"));

        assertThat(results).containsExactly(
                new TranslationResult("potato", "आलू"),
                new TranslationResult("tomato", "टमाटर")
        );
    }

    @Test
    @DisplayName("should send a prompt containing source, destination and words, requesting JSON output")
    void shouldSendPromptContainingSourceDestinationAndWords() {
        when(geminiClient.generateContent(any())).thenReturn(responseWithText("[{\"source\":\"potato\",\"translated\":\"आलू\"}]"));

        geminiTranslationProvider.translate("english", "hindi", List.of("potato"));

        ArgumentCaptor<GeminiGenerateContentRequest> captor = ArgumentCaptor.forClass(GeminiGenerateContentRequest.class);
        verify(geminiClient).generateContent(captor.capture());

        GeminiGenerateContentRequest sentRequest = captor.getValue();
        String prompt = sentRequest.contents().get(0).parts().get(0).text();

        assertThat(prompt).contains("english").contains("hindi").contains("potato");
        assertThat(sentRequest.generationConfig().responseMimeType()).isEqualTo("application/json");
        assertThat(sentRequest.generationConfig().responseSchema().type()).isEqualTo("ARRAY");
    }

    @Test
    @DisplayName("should throw when Gemini returns no candidates")
    void shouldThrowWhenNoCandidatesReturned() {
        GeminiGenerateContentResponse response = new GeminiGenerateContentResponse(List.of(), null);
        when(geminiClient.generateContent(any())).thenReturn(response);

        assertThatThrownBy(() -> geminiTranslationProvider.translate("english", "hindi", List.of("potato")))
                .isInstanceOf(GeminiResponseParsingException.class);
    }

    @Test
    @DisplayName("should throw when Gemini blocks the response")
    void shouldThrowWhenResponseIsBlocked() {
        GeminiGenerateContentResponse response =
                new GeminiGenerateContentResponse(List.of(), new GeminiPromptFeedback("SAFETY"));
        when(geminiClient.generateContent(any())).thenReturn(response);

        assertThatThrownBy(() -> geminiTranslationProvider.translate("english", "hindi", List.of("potato")))
                .isInstanceOf(GeminiResponseParsingException.class)
                .hasMessageContaining("SAFETY");
    }

    @Test
    @DisplayName("should throw when Gemini's response text is not valid JSON")
    void shouldThrowWhenJsonIsInvalid() {
        when(geminiClient.generateContent(any())).thenReturn(responseWithText("not-json"));

        assertThatThrownBy(() -> geminiTranslationProvider.translate("english", "hindi", List.of("potato")))
                .isInstanceOf(GeminiResponseParsingException.class);
    }

    @Test
    @DisplayName("should throw when Gemini returns an empty translation list")
    void shouldThrowWhenTranslationListIsEmpty() {
        when(geminiClient.generateContent(any())).thenReturn(responseWithText("[]"));

        assertThatThrownBy(() -> geminiTranslationProvider.translate("english", "hindi", List.of("potato")))
                .isInstanceOf(GeminiResponseParsingException.class);
    }

    @Test
    @DisplayName("should throw when the candidate has no text content")
    void shouldThrowWhenCandidateHasNoText() {
        GeminiCandidate candidate = new GeminiCandidate(new GeminiContent(List.of(), "model"), "STOP");
        GeminiGenerateContentResponse response = new GeminiGenerateContentResponse(List.of(candidate), null);
        when(geminiClient.generateContent(any())).thenReturn(response);

        assertThatThrownBy(() -> geminiTranslationProvider.translate("english", "hindi", List.of("potato")))
                .isInstanceOf(GeminiResponseParsingException.class);
    }

    private GeminiGenerateContentResponse responseWithText(String text) {
        GeminiPart part = new GeminiPart(text);
        GeminiContent content = new GeminiContent(List.of(part), "model");
        GeminiCandidate candidate = new GeminiCandidate(content, "STOP");
        return new GeminiGenerateContentResponse(List.of(candidate), null);
    }
}
