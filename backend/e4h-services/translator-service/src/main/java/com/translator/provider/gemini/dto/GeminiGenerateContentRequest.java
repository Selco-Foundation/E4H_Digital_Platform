package com.translator.provider.gemini.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiGenerateContentRequest(
        List<GeminiContent> contents,
        GeminiGenerationConfig generationConfig
) {
}
