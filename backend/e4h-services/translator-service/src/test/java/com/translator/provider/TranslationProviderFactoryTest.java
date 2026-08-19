package com.translator.provider;

import com.translator.provider.gemini.GeminiTranslationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class TranslationProviderFactoryTest {

    @Mock
    private GeminiTranslationProvider geminiTranslationProvider;

    private TranslationProviderFactory translationProviderFactory;

    @BeforeEach
    void setUp() {
        translationProviderFactory = new TranslationProviderFactory(geminiTranslationProvider);
    }

    @Test
    @DisplayName("should return GeminiTranslationProvider for GEMINI")
    void shouldReturnGeminiProviderForGeminiType() {
        assertThat(translationProviderFactory.getProvider(TranslationProviderType.GEMINI))
                .isSameAs(geminiTranslationProvider);
    }

    @Test
    @DisplayName("should default to GeminiTranslationProvider when provider type is null")
    void shouldDefaultToGeminiWhenProviderTypeIsNull() {
        assertThat(translationProviderFactory.getProvider(null))
                .isSameAs(geminiTranslationProvider);
    }

    @Test
    @DisplayName("should throw UnsupportedOperationException for BHASHINI")
    void shouldThrowUnsupportedOperationExceptionForBhashini() {
        assertThatThrownBy(() -> translationProviderFactory.getProvider(TranslationProviderType.BHASHINI))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Bhashini provider not implemented yet");
    }

    @Test
    @DisplayName("should throw UnsupportedOperationException for GOOGLE")
    void shouldThrowUnsupportedOperationExceptionForGoogle() {
        assertThatThrownBy(() -> translationProviderFactory.getProvider(TranslationProviderType.GOOGLE))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Google provider not implemented yet");
    }
}
