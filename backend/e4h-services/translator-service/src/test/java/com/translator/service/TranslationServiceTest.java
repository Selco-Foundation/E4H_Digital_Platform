package com.translator.service;

import com.translator.dto.TranslationItemDto;
import com.translator.dto.TranslationRequest;
import com.translator.dto.TranslationResponse;
import com.translator.model.TranslationResult;
import com.translator.provider.TranslationProvider;
import com.translator.provider.TranslationProviderFactory;
import com.translator.provider.TranslationProviderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranslationServiceTest {

    @Mock
    private TranslationProviderFactory translationProviderFactory;

    @Mock
    private TranslationProvider translationProvider;

    private TranslationService translationService;

    @BeforeEach
    void setUp() {
        translationService = new TranslationService(translationProviderFactory);
    }

    @Test
    @DisplayName("should resolve provider via factory using the requested provider type")
    void shouldTranslateUsingSelectedProvider() {
        TranslationRequest request = new TranslationRequest(
                "english",
                "hindi",
                TranslationProviderType.GEMINI,
                List.of("potato", "banana")
        );

        when(translationProviderFactory.getProvider(TranslationProviderType.GEMINI)).thenReturn(translationProvider);
        when(translationProvider.getProviderName()).thenReturn("GEMINI");
        when(translationProvider.translate("english", "hindi", List.of("potato", "banana")))
                .thenReturn(List.of(
                        new TranslationResult("potato", "आलू"),
                        new TranslationResult("banana", "[NOT_FOUND]")
                ));

        TranslationResponse response = translationService.translate(request);

        assertThat(response.sourceLanguage()).isEqualTo("english");
        assertThat(response.destinationLanguage()).isEqualTo("hindi");
        assertThat(response.translations()).containsExactly(
                new TranslationItemDto("potato", "आलू"),
                new TranslationItemDto("banana", "[NOT_FOUND]")
        );

        verify(translationProviderFactory).getProvider(TranslationProviderType.GEMINI);
        verify(translationProvider).translate(
                eq("english"),
                eq("hindi"),
                eq(List.of("potato", "banana"))
        );
    }

    @Test
    @DisplayName("should default to GEMINI when request provider is null")
    void shouldDefaultToGeminiWhenProviderIsNull() {
        TranslationRequest request = new TranslationRequest(
                "english",
                "hindi",
                null,
                List.of("water")
        );

        when(translationProviderFactory.getProvider(TranslationProviderType.GEMINI)).thenReturn(translationProvider);
        when(translationProvider.getProviderName()).thenReturn("GEMINI");
        when(translationProvider.translate(anyString(), anyString(), anyList()))
                .thenReturn(List.of(new TranslationResult("water", "पानी")));

        TranslationResponse response = translationService.translate(request);

        assertThat(response.translations()).hasSize(1);
        assertThat(response.translations().getFirst().translated()).isEqualTo("पानी");
        verify(translationProviderFactory).getProvider(TranslationProviderType.GEMINI);
    }

    @Test
    @DisplayName("should depend only on TranslationProvider interface")
    void shouldDependOnProviderInterface() {
        TranslationRequest request = new TranslationRequest(
                "english",
                "hindi",
                TranslationProviderType.GEMINI,
                List.of("water")
        );

        when(translationProviderFactory.getProvider(eq(TranslationProviderType.GEMINI))).thenReturn(translationProvider);
        when(translationProvider.getProviderName()).thenReturn("GEMINI");
        when(translationProvider.translate(anyString(), anyString(), anyList()))
                .thenReturn(List.of(new TranslationResult("water", "पानी")));

        TranslationResponse response = translationService.translate(request);

        assertThat(response.translations()).hasSize(1);
        assertThat(response.translations().getFirst().translated()).isEqualTo("पानी");
    }
}
