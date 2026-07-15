package com.translator.provider;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Supported translation backends. New backends are added here and wired
 * into {@link TranslationProviderFactory} without touching the controller
 * or service layers.
 */
public enum TranslationProviderType {
    GEMINI,
    BHASHINI,
    GOOGLE;

    @JsonCreator
    public static TranslationProviderType fromValue(String value) {
        return TranslationProviderType.valueOf(value.trim().toUpperCase());
    }
}
