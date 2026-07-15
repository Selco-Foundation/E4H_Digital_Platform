package com.translator.provider;

import com.translator.model.TranslationResult;

import java.util.List;

/**
 * Abstraction for translation backends.
 * Implementations (Mock, Google, Bhashini, Azure, etc.) plug in via this contract.
 */
public interface TranslationProvider {

    /**
     * Unique provider name used by {@link ProviderFactory} for selection.
     */
    String getProviderName();

    /**
     * Translates the given words from sourceLanguage to destinationLanguage.
     *
     * @param sourceLanguage      language of the input words
     * @param destinationLanguage target language for translations
     * @param words               words to translate
     * @return list of translation results in the same order as input words
     */
    List<TranslationResult> translate(
            String sourceLanguage,
            String destinationLanguage,
            List<String> words
    );
}
