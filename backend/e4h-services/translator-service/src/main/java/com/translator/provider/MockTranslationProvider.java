package com.translator.provider;

import com.translator.model.TranslationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * In-memory dictionary-based translation provider for POC purposes.
 * Replace with GoogleTranslationProvider / BhashiniTranslationProvider in Step 2.
 */
@Component
public class MockTranslationProvider implements TranslationProvider {

    public static final String PROVIDER_NAME = "mock";
    public static final String NOT_FOUND = "[NOT_FOUND]";

    private static final Logger log = LoggerFactory.getLogger(MockTranslationProvider.class);

    private static final Map<String, String> ENGLISH_TO_HINDI = Map.of(
            "potato", "आलू",
            "tomato", "टमाटर",
            "water", "पानी",
            "pump", "पंप",
            "tank", "टैंक",
            "valve", "वाल्व"
    );

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public List<TranslationResult> translate(
            String sourceLanguage,
            String destinationLanguage,
            List<String> words) {

        log.debug(
                "MockTranslationProvider translating {} words from '{}' to '{}'",
                words.size(),
                sourceLanguage,
                destinationLanguage
        );

        return words.stream()
                .map(this::translateWord)
                .toList();
    }

    private TranslationResult translateWord(String word) {
        String normalized = word == null ? "" : word.trim().toLowerCase();
        String translated = ENGLISH_TO_HINDI.getOrDefault(normalized, NOT_FOUND);
        return new TranslationResult(word, translated);
    }
}
