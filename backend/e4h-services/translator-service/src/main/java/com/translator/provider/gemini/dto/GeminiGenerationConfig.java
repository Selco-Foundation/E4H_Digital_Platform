package com.translator.provider.gemini.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiGenerationConfig(
        String responseMimeType,
        GeminiSchema responseSchema
) {
}
