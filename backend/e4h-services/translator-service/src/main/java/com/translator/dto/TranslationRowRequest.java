package com.translator.dto;

import com.translator.provider.TranslationProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Same enrichment model as the spreadsheet upload endpoint, but for plain
 * JSON: a flat list of source words, translated into every language in
 * {@code destinationLanguage}. The response has one row per word, with a
 * field per destination language (see {@code TranslationRowResponse}).
 */
public record TranslationRowRequest(
        @NotBlank(message = "sourceLanguage must not be blank")
        String sourceLanguage,

        @NotNull(message = "destinationLanguage must not be null")
        @NotEmpty(message = "destinationLanguage must not be empty")
        List<@NotBlank(message = "destinationLanguage entries must not be blank") String> destinationLanguage,

        TranslationProviderType provider,

        @NotNull(message = "words must not be null")
        @NotEmpty(message = "words must not be empty")
        List<String> words
) {
}
