package com.translator.provider.gemini.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Subset of the Gemini structured-output schema (OpenAPI-style) used to
 * force {@code generateContent} to return JSON matching a fixed shape.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiSchema(
        String type,
        Map<String, GeminiSchema> properties,
        GeminiSchema items,
        List<String> required,
        List<String> propertyOrdering
) {

    public static GeminiSchema string() {
        return new GeminiSchema("STRING", null, null, null, null);
    }

    public static GeminiSchema object(Map<String, GeminiSchema> properties, List<String> required) {
        return new GeminiSchema("OBJECT", properties, null, required, required);
    }

    public static GeminiSchema arrayOf(GeminiSchema items) {
        return new GeminiSchema("ARRAY", null, items, null, null);
    }
}
