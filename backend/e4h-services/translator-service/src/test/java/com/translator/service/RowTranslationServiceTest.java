package com.translator.service;

import com.translator.dto.TranslationItemDto;
import com.translator.dto.TranslationRequest;
import com.translator.dto.TranslationResponse;
import com.translator.dto.TranslationRowRequest;
import com.translator.dto.TranslationRowResponse;
import com.translator.exception.RowTranslationException;
import com.translator.provider.TranslationProviderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RowTranslationServiceTest {

    @Mock
    private TranslationService translationService;

    private RowTranslationService rowTranslationService;

    @BeforeEach
    void setUp() {
        rowTranslationService = new RowTranslationService(translationService);
    }

    @Test
    @DisplayName("should build one row per word with a field per destination language")
    void shouldTranslateWordsIntoMultipleDestinationLanguages() {
        TranslationRowRequest request = new TranslationRowRequest(
                "english", List.of("hindi", "french"), null, List.of("Potato", "Tomato"));

        when(translationService.translate(argThat(req -> req != null && "hindi".equals(req.destinationLanguage()))))
                .thenReturn(new TranslationResponse("english", "hindi", List.of(
                        new TranslationItemDto("Potato", "आलू"),
                        new TranslationItemDto("Tomato", "टमाटर")
                )));
        when(translationService.translate(argThat(req -> req != null && "french".equals(req.destinationLanguage()))))
                .thenReturn(new TranslationResponse("english", "french", List.of(
                        new TranslationItemDto("Potato", "Pomme de terre"),
                        new TranslationItemDto("Tomato", "Tomate")
                )));

        TranslationRowResponse response = rowTranslationService.translateRows(request);

        assertThat(response.rows()).hasSize(2);
        assertThat(response.rows().get(0))
                .containsEntry("English", "Potato")
                .containsEntry("Hindi", "आलू")
                .containsEntry("French", "Pomme de terre");
        assertThat(response.rows().get(1))
                .containsEntry("English", "Tomato")
                .containsEntry("Hindi", "टमाटर")
                .containsEntry("French", "Tomate");
    }

    @Test
    @DisplayName("should work with a single destination language")
    void shouldTranslateWordsIntoSingleDestinationLanguage() {
        TranslationRowRequest request = new TranslationRowRequest(
                "english", List.of("hindi"), TranslationProviderType.GEMINI, List.of("Potato"));

        when(translationService.translate(any(TranslationRequest.class))).thenReturn(
                new TranslationResponse("english", "hindi", List.of(new TranslationItemDto("Potato", "आलू"))));

        TranslationRowResponse response = rowTranslationService.translateRows(request);

        assertThat(response.rows()).hasSize(1);
        assertThat(response.rows().get(0)).containsEntry("English", "Potato").containsEntry("Hindi", "आलू");
    }

    @Test
    @DisplayName("should pass the requested provider through to every destination-language translation call")
    void shouldPassProviderToEveryTranslationCall() {
        TranslationRowRequest request = new TranslationRowRequest(
                "english", List.of("hindi", "french"), TranslationProviderType.GEMINI, List.of("Potato"));

        when(translationService.translate(any(TranslationRequest.class))).thenReturn(
                new TranslationResponse("english", "x", List.of(new TranslationItemDto("Potato", "x"))));

        rowTranslationService.translateRows(request);

        verify(translationService).translate(argThat(req ->
                "hindi".equals(req.destinationLanguage()) && req.provider() == TranslationProviderType.GEMINI));
        verify(translationService).translate(argThat(req ->
                "french".equals(req.destinationLanguage()) && req.provider() == TranslationProviderType.GEMINI));
    }

    @Test
    @DisplayName("should throw when a destination language's translation result count doesn't match the word count")
    void shouldThrowWhenTranslationCountMismatches() {
        TranslationRowRequest request = new TranslationRowRequest(
                "english", List.of("hindi"), null, List.of("Potato", "Tomato"));

        when(translationService.translate(any(TranslationRequest.class))).thenReturn(
                new TranslationResponse("english", "hindi", List.of(new TranslationItemDto("Potato", "आलू"))));

        assertThatThrownBy(() -> rowTranslationService.translateRows(request))
                .isInstanceOf(RowTranslationException.class)
                .hasMessageContaining("did not match");
    }
}
