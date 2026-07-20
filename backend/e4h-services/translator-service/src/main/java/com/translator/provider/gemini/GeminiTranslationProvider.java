package com.translator.provider.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.translator.config.GeminiProperties;
import com.translator.exception.GeminiResponseParsingException;
import com.translator.model.TranslationResult;
import com.translator.provider.TranslationProvider;
import com.translator.provider.TranslationProviderType;
import com.translator.provider.gemini.dto.GeminiCandidate;
import com.translator.provider.gemini.dto.GeminiContent;
import com.translator.provider.gemini.dto.GeminiGenerateContentRequest;
import com.translator.provider.gemini.dto.GeminiGenerateContentResponse;
import com.translator.provider.gemini.dto.GeminiGenerationConfig;
import com.translator.provider.gemini.dto.GeminiSchema;
import com.translator.provider.gemini.dto.GeminiTranslationItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Translates words using the Gemini API. Responsible ONLY for mapping
 * between the application domain and the Gemini contract; HTTP concerns
 * belong to {@link GeminiClient}.
 */
@Component
public class GeminiTranslationProvider implements TranslationProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiTranslationProvider.class);
    private static final String JSON_MIME_TYPE = "application/json";

    private final GeminiClient geminiClient;
    private final GeminiProperties geminiProperties;
    private final ObjectMapper objectMapper;

    public GeminiTranslationProvider(
            GeminiClient geminiClient,
            GeminiProperties geminiProperties,
            ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.geminiProperties = geminiProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getProviderName() {
        return TranslationProviderType.GEMINI.name();
    }

    @Override
    public List<TranslationResult> translate(
            String sourceLanguage,
            String destinationLanguage,
            List<String> words) {

        long startTime = System.currentTimeMillis();
        log.info(
                "Gemini translating {} word(s) from '{}' to '{}' using model '{}'",
                words.size(), sourceLanguage, destinationLanguage, geminiProperties.model()
        );

        GeminiGenerateContentRequest request = buildRequest(sourceLanguage, destinationLanguage, words);
        GeminiGenerateContentResponse response = geminiClient.generateContent(request);
        List<TranslationResult> results = mapToResults(response, words);

        long elapsedMs = System.currentTimeMillis() - startTime;
        log.info("Gemini translation completed in {} ms for {} word(s)", elapsedMs, words.size());

        return results;
    }

    private GeminiGenerateContentRequest buildRequest(
            String sourceLanguage,
            String destinationLanguage,
            List<String> words) {

        String prompt = buildPrompt(sourceLanguage, destinationLanguage, words);
        GeminiContent content = GeminiContent.userText(prompt);
        GeminiGenerationConfig generationConfig = new GeminiGenerationConfig(JSON_MIME_TYPE, buildResponseSchema());

        return new GeminiGenerateContentRequest(List.of(content), generationConfig);
    }

    private String buildPrompt(String sourceLanguage, String destinationLanguage, List<String> words) {
        String wordsJson = writeJson(words);

        return """
                You are a translation engine.

                Translate the following words from %s to %s.

                Rules:
                - Return JSON only.
                - No markdown.
                - No explanations.
                - Preserve input order.
                - Translate each word independently.
                - If translation is unavailable, return the original word.

                Input:
                %s
                """.formatted(sourceLanguage, destinationLanguage, wordsJson);
    }

    private GeminiSchema buildResponseSchema() {
        Map<String, GeminiSchema> properties = Map.of(
                "source", GeminiSchema.string(),
                "translated", GeminiSchema.string()
        );
        return GeminiSchema.arrayOf(GeminiSchema.object(properties, List.of("source", "translated")));
    }

    private List<TranslationResult> mapToResults(GeminiGenerateContentResponse response, List<String> originalWords) {
        if (response == null) {
            throw new GeminiResponseParsingException("Gemini API returned an empty response");
        }
        if (response.promptFeedback() != null && response.promptFeedback().blockReason() != null) {
            throw new GeminiResponseParsingException(
                    "Gemini blocked the request: " + response.promptFeedback().blockReason());
        }
        if (response.candidates() == null || response.candidates().isEmpty()) {
            throw new GeminiResponseParsingException("Gemini API returned no candidates");
        }

        String text = extractText(response.candidates().get(0));
        List<GeminiTranslationItem> items = parseJson(text);

        if (items.isEmpty()) {
            throw new GeminiResponseParsingException("Gemini API returned an empty translation list");
        }
        if (items.size() != originalWords.size()) {
            log.warn(
                    "Gemini returned {} item(s) for {} input word(s)",
                    items.size(), originalWords.size()
            );
        }

        return items.stream()
                .map(item -> new TranslationResult(item.source(), item.translated()))
                .toList();
    }

    private String extractText(GeminiCandidate candidate) {
        boolean hasText = candidate.content() != null
                && candidate.content().parts() != null
                && !candidate.content().parts().isEmpty()
                && candidate.content().parts().get(0).text() != null;

        if (!hasText) {
            throw new GeminiResponseParsingException("Gemini candidate contained no text content");
        }

        return candidate.content().parts().get(0).text();
    }

    private List<GeminiTranslationItem> parseJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<GeminiTranslationItem>>() {
            });
        } catch (JsonProcessingException e) {
            throw new GeminiResponseParsingException("Failed to parse Gemini JSON response", e);
        }
    }

    private String writeJson(List<String> words) {
        try {
            return objectMapper.writeValueAsString(words);
        } catch (JsonProcessingException e) {
            throw new GeminiResponseParsingException("Failed to serialize words for Gemini prompt", e);
        }
    }
}
