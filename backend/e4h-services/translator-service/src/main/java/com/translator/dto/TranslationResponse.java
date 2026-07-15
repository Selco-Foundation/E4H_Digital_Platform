package com.translator.dto;

import java.util.List;

public record TranslationResponse(
        String sourceLanguage,
        String destinationLanguage,
        List<TranslationItemDto> translations
) {
}
