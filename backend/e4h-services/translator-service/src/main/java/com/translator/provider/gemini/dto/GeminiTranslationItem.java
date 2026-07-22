package com.translator.provider.gemini.dto;

/**
 * A single entry of the structured JSON array Gemini returns. Isolated
 * from the application's {@code TranslationResult} domain model.
 */
public record GeminiTranslationItem(String source, String translated) {
}
