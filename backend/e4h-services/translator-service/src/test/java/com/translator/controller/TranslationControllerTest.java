package com.translator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.translator.dto.TranslationItemDto;
import com.translator.dto.TranslationRequest;
import com.translator.dto.TranslationResponse;
import com.translator.dto.TranslationRowRequest;
import com.translator.dto.TranslationRowResponse;
import com.translator.exception.GlobalExceptionHandler;
import com.translator.service.RowTranslationService;
import com.translator.service.TranslationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TranslationController.class)
@Import(GlobalExceptionHandler.class)
class TranslationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TranslationService translationService;

    @MockBean
    private RowTranslationService rowTranslationService;

    @Test
    @DisplayName("POST /translate should return 200 with translations")
    void shouldReturnTranslations() throws Exception {
        TranslationRequest request = new TranslationRequest(
                "english",
                "hindi",
                null,
                List.of("potato", "tomato", "water")
        );

        TranslationResponse response = new TranslationResponse(
                "english",
                "hindi",
                List.of(
                        new TranslationItemDto("potato", "आलू"),
                        new TranslationItemDto("tomato", "टमाटर"),
                        new TranslationItemDto("water", "पानी")
                )
        );

        when(translationService.translate(any(TranslationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceLanguage").value("english"))
                .andExpect(jsonPath("$.destinationLanguage").value("hindi"))
                .andExpect(jsonPath("$.translations[0].source").value("potato"))
                .andExpect(jsonPath("$.translations[0].translated").value("आलू"))
                .andExpect(jsonPath("$.translations[1].source").value("tomato"))
                .andExpect(jsonPath("$.translations[1].translated").value("टमाटर"))
                .andExpect(jsonPath("$.translations[2].source").value("water"))
                .andExpect(jsonPath("$.translations[2].translated").value("पानी"));

        verify(translationService).translate(any(TranslationRequest.class));
    }

    @Test
    @DisplayName("POST /translate should return 400 when sourceLanguage is blank")
    void shouldRejectBlankSourceLanguage() throws Exception {
        String payload = """
                {
                  "sourceLanguage": " ",
                  "destinationLanguage": "hindi",
                  "words": ["potato"]
                }
                """;

        mockMvc.perform(post("/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.details").isArray());

        verify(translationService, never()).translate(any());
    }

    @Test
    @DisplayName("POST /translate should return 400 when destinationLanguage is blank")
    void shouldRejectBlankDestinationLanguage() throws Exception {
        String payload = """
                {
                  "sourceLanguage": "english",
                  "destinationLanguage": "",
                  "words": ["potato"]
                }
                """;

        mockMvc.perform(post("/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(translationService, never()).translate(any());
    }

    @Test
    @DisplayName("POST /translate should return 400 when words list is empty")
    void shouldRejectEmptyWords() throws Exception {
        TranslationRequest request = new TranslationRequest(
                "english",
                "hindi",
                null,
                Collections.emptyList()
        );

        mockMvc.perform(post("/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed"));

        verify(translationService, never()).translate(any());
    }

    @Test
    @DisplayName("POST /translate should return 400 when words is null")
    void shouldRejectNullWords() throws Exception {
        String payload = """
                {
                  "sourceLanguage": "english",
                  "destinationLanguage": "hindi",
                  "words": null
                }
                """;

        mockMvc.perform(post("/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(translationService, never()).translate(any());
    }

    @Test
    @DisplayName("POST /translate should accept a lowercase provider value, per the API contract")
    void shouldAcceptLowercaseProviderValue() throws Exception {
        String payload = """
                {
                  "sourceLanguage": "english",
                  "destinationLanguage": "hindi",
                  "provider": "gemini",
                  "words": ["potato"]
                }
                """;

        TranslationResponse response = new TranslationResponse(
                "english",
                "hindi",
                List.of(new TranslationItemDto("potato", "आलू"))
        );

        when(translationService.translate(any(TranslationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        verify(translationService).translate(any(TranslationRequest.class));
    }

    @Test
    @DisplayName("POST /translate should return 400 when provider is an unknown value")
    void shouldRejectUnknownProvider() throws Exception {
        String payload = """
                {
                  "sourceLanguage": "english",
                  "destinationLanguage": "hindi",
                  "provider": "UNKNOWN_PROVIDER",
                  "words": ["potato"]
                }
                """;

        mockMvc.perform(post("/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(translationService, never()).translate(any());
    }

    @Test
    @DisplayName("POST /translate should default provider to GEMINI when omitted")
    void shouldAcceptRequestWithoutProvider() throws Exception {
        String payload = """
                {
                  "sourceLanguage": "english",
                  "destinationLanguage": "hindi",
                  "words": ["potato"]
                }
                """;

        TranslationResponse response = new TranslationResponse(
                "english",
                "hindi",
                List.of(new TranslationItemDto("potato", "आलू"))
        );

        when(translationService.translate(any(TranslationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        verify(translationService).translate(any(TranslationRequest.class));
    }

    @Test
    @DisplayName("POST /translate should return 501 with a clear message when the provider isn't implemented yet")
    void shouldReturnNotImplementedForUnsupportedProvider() throws Exception {
        String payload = """
                {
                  "sourceLanguage": "english",
                  "destinationLanguage": "hindi",
                  "provider": "bhashini",
                  "words": ["potato"]
                }
                """;

        when(translationService.translate(any(TranslationRequest.class)))
                .thenThrow(new UnsupportedOperationException("Bhashini provider not implemented yet"));

        mockMvc.perform(post("/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.status").value(501))
                .andExpect(jsonPath("$.message").value("Bhashini provider not implemented yet"));
    }

    @Test
    @DisplayName("POST /translate/rows should return 200 with one row per word and a field per destination language")
    void shouldReturnTranslatedRows() throws Exception {
        TranslationRowRequest request = new TranslationRowRequest(
                "english", List.of("hindi", "french"), null, List.of("Potato", "Tomato"));

        Map<String, String> resultRow1 = new LinkedHashMap<>();
        resultRow1.put("English", "Potato");
        resultRow1.put("Hindi", "आलू");
        resultRow1.put("French", "Pomme de terre");
        Map<String, String> resultRow2 = new LinkedHashMap<>();
        resultRow2.put("English", "Tomato");
        resultRow2.put("Hindi", "टमाटर");
        resultRow2.put("French", "Tomate");

        TranslationRowResponse response = new TranslationRowResponse(List.of(resultRow1, resultRow2));

        when(rowTranslationService.translateRows(any(TranslationRowRequest.class))).thenReturn(response);

        mockMvc.perform(post("/translate/rows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].English").value("Potato"))
                .andExpect(jsonPath("$.rows[0].Hindi").value("आलू"))
                .andExpect(jsonPath("$.rows[0].French").value("Pomme de terre"))
                .andExpect(jsonPath("$.rows[1].English").value("Tomato"))
                .andExpect(jsonPath("$.rows[1].Hindi").value("टमाटर"))
                .andExpect(jsonPath("$.rows[1].French").value("Tomate"));

        verify(rowTranslationService).translateRows(any(TranslationRowRequest.class));
    }

    @Test
    @DisplayName("POST /translate/rows should return 400 when words is empty")
    void shouldRejectEmptyWordsForRows() throws Exception {
        String payload = """
                {
                  "sourceLanguage": "english",
                  "destinationLanguage": ["hindi"],
                  "words": []
                }
                """;

        mockMvc.perform(post("/translate/rows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(rowTranslationService, never()).translateRows(any());
    }

    @Test
    @DisplayName("POST /translate/rows should return 400 when destinationLanguage is empty")
    void shouldRejectEmptyDestinationLanguageForRows() throws Exception {
        String payload = """
                {
                  "sourceLanguage": "english",
                  "destinationLanguage": [],
                  "words": ["Potato"]
                }
                """;

        mockMvc.perform(post("/translate/rows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(rowTranslationService, never()).translateRows(any());
    }

    @Test
    @DisplayName("POST /translate/rows should return 400 when a destinationLanguage entry is blank")
    void shouldRejectBlankDestinationLanguageEntryForRows() throws Exception {
        String payload = """
                {
                  "sourceLanguage": "english",
                  "destinationLanguage": ["hindi", " "],
                  "words": ["Potato"]
                }
                """;

        mockMvc.perform(post("/translate/rows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(rowTranslationService, never()).translateRows(any());
    }

    @Test
    @DisplayName("POST /translate/rows should return 400 when sourceLanguage is blank")
    void shouldRejectBlankSourceLanguageForRows() throws Exception {
        String payload = """
                {
                  "sourceLanguage": " ",
                  "destinationLanguage": ["hindi"],
                  "words": ["Potato"]
                }
                """;

        mockMvc.perform(post("/translate/rows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(rowTranslationService, never()).translateRows(any());
    }
}
