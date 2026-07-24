package com.translator.dto;

import com.translator.provider.TranslationProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TranslationRequest(
        @NotBlank(message = "sourceLanguage must not be blank")
        String sourceLanguage,

        @NotBlank(message = "destinationLanguage must not be blank")
        String destinationLanguage,

        TranslationProviderType provider,

        @NotNull(message = "words must not be null")
        @NotEmpty(message = "words must not be empty")
        List<String> words
) {
}
