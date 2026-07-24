package com.translator.provider;

import com.translator.model.TranslationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MockTranslationProviderTest {

    private MockTranslationProvider provider;

    @BeforeEach
    void setUp() {
        provider = new MockTranslationProvider();
    }

    @Test
    @DisplayName("should return provider name as mock")
    void shouldReturnProviderName() {
        assertThat(provider.getProviderName()).isEqualTo(MockTranslationProvider.PROVIDER_NAME);
    }

    @Test
    @DisplayName("should translate known English words to Hindi")
    void shouldTranslateKnownWords() {
        List<TranslationResult> results = provider.translate(
                "english",
                "hindi",
                List.of("potato", "tomato", "water", "pump", "tank", "valve")
        );

        assertThat(results).containsExactly(
                new TranslationResult("potato", "आलू"),
                new TranslationResult("tomato", "टमाटर"),
                new TranslationResult("water", "पानी"),
                new TranslationResult("pump", "पंप"),
                new TranslationResult("tank", "टैंक"),
                new TranslationResult("valve", "वाल्व")
        );
    }

    @Test
    @DisplayName("should return [NOT_FOUND] for unknown words")
    void shouldReturnNotFoundForUnknownWords() {
        List<TranslationResult> results = provider.translate(
                "english",
                "hindi",
                List.of("banana", "apple")
        );

        assertThat(results).containsExactly(
                new TranslationResult("banana", MockTranslationProvider.NOT_FOUND),
                new TranslationResult("apple", MockTranslationProvider.NOT_FOUND)
        );
    }

    @Test
    @DisplayName("should be case-insensitive for dictionary lookup")
    void shouldBeCaseInsensitive() {
        List<TranslationResult> results = provider.translate(
                "english",
                "hindi",
                List.of("Potato", "TOMATO", "WaTeR")
        );

        assertThat(results).containsExactly(
                new TranslationResult("Potato", "आलू"),
                new TranslationResult("TOMATO", "टमाटर"),
                new TranslationResult("WaTeR", "पानी")
        );
    }

    @Test
    @DisplayName("should preserve original source word in the result")
    void shouldPreserveOriginalSourceWord() {
        List<TranslationResult> results = provider.translate(
                "english",
                "hindi",
                List.of("  potato  ")
        );

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().source()).isEqualTo("  potato  ");
        assertThat(results.getFirst().translated()).isEqualTo("आलू");
    }
}
