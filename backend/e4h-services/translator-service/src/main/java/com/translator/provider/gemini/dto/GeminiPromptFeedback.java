package com.translator.provider.gemini.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiPromptFeedback(String blockReason) {
}
