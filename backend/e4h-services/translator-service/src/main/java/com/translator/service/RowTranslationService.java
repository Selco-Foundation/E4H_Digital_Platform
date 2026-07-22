package com.translator.service;

import com.translator.dto.TranslationRequest;
import com.translator.dto.TranslationResponse;
import com.translator.dto.TranslationRowRequest;
import com.translator.dto.TranslationRowResponse;
import com.translator.exception.RowTranslationException;
import com.translator.provider.TranslationProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON counterpart to {@link ExcelTranslationService}: translates a flat
 * list of source words into every language in {@code destinationLanguage}.
 * The response has one row per word, keyed by the capitalized source
 * language plus one field per destination language. Delegates each
 * language's actual translation call to {@link TranslationService}.
 */
@Service
public class RowTranslationService {

    private static final Logger log = LoggerFactory.getLogger(RowTranslationService.class);

    private final TranslationService translationService;

    public RowTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

    public TranslationRowResponse translateRows(TranslationRowRequest request) {
        List<String> words = request.words();
        String sourceKey = LanguageNames.capitalize(request.sourceLanguage());

        List<Map<String, String>> rows = new ArrayList<>();
        for (String word : words) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put(sourceKey, word);
            rows.add(row);
        }

        for (String destinationLanguage : request.destinationLanguage()) {
            fillDestinationField(rows, words, request.sourceLanguage(), destinationLanguage, request.provider());
        }

        return new TranslationRowResponse(rows);
    }

    private void fillDestinationField(
            List<Map<String, String>> rows,
            List<String> words,
            String sourceLanguage,
            String destinationLanguage,
            TranslationProviderType provider) {

        log.info(
                "Translating {} word(s): '{}' -> '{}'",
                words.size(), sourceLanguage, destinationLanguage
        );

        TranslationRequest translationRequest = new TranslationRequest(sourceLanguage, destinationLanguage, provider, words);
        TranslationResponse response = translationService.translate(translationRequest);

        if (response.translations().size() != words.size()) {
            throw new RowTranslationException(
                    "Translation result count (%d) did not match the number of words (%d) for destination language '%s'"
                            .formatted(response.translations().size(), words.size(), destinationLanguage));
        }

        String destinationKey = LanguageNames.capitalize(destinationLanguage);

        for (int i = 0; i < words.size(); i++) {
            rows.get(i).put(destinationKey, response.translations().get(i).translated());
        }
    }
}
