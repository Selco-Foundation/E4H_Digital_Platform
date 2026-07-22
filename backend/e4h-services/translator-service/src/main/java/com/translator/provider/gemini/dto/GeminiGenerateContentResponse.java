package com.translator.provider.gemini.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiGenerateContentResponse(
        List<GeminiCandidate> candidates,
        GeminiPromptFeedback promptFeedback
) {
}
