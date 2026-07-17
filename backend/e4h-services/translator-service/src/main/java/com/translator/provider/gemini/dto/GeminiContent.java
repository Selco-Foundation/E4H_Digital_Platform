package com.translator.provider.gemini.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiContent(List<GeminiPart> parts, String role) {

    public static GeminiContent userText(String text) {
        return new GeminiContent(List.of(new GeminiPart(text)), "user");
    }
}
