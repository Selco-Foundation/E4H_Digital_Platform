package com.translator.controller;

import com.translator.exception.GlobalExceptionHandler;
import com.translator.provider.StringToTranslationProviderTypeConverter;
import com.translator.provider.TranslationProviderType;
import com.translator.service.ExcelTranslationService;
import com.translator.service.TranslatedSpreadsheet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExcelTranslationController.class)
@Import({GlobalExceptionHandler.class, StringToTranslationProviderTypeConverter.class})
class ExcelTranslationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExcelTranslationService excelTranslationService;

    @Test
    @DisplayName("POST /translate/excel should return the translated workbook as a downloadable file")
    void shouldReturnTranslatedWorkbook() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "words.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "fake-workbook-bytes".getBytes());

        TranslatedSpreadsheet translated = new TranslatedSpreadsheet(
                "translated-workbook-bytes".getBytes(),
                "words-translated.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        when(excelTranslationService.translateWorkbook(any(), eq("english"), eq("hindi"), eq(TranslationProviderType.GEMINI)))
                .thenReturn(translated);

        mockMvc.perform(multipart("/translate/excel")
                        .file(file)
                        .param("sourceLanguage", "english")
                        .param("destinationLanguage", "hindi")
                        .param("provider", "gemini"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"words-translated.xlsx\""));

        verify(excelTranslationService).translateWorkbook(any(), eq("english"), eq("hindi"), eq(TranslationProviderType.GEMINI));
    }

    @Test
    @DisplayName("POST /translate/excel should work without an explicit provider")
    void shouldAcceptRequestWithoutProvider() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "words.csv", "text/csv", "fake-csv-bytes".getBytes());

        TranslatedSpreadsheet translated = new TranslatedSpreadsheet(
                "translated".getBytes(), "words-translated.csv", "text/csv");

        when(excelTranslationService.translateWorkbook(any(), eq("english"), eq("hindi"), isNull()))
                .thenReturn(translated);

        mockMvc.perform(multipart("/translate/excel")
                        .file(file)
                        .param("sourceLanguage", "english")
                        .param("destinationLanguage", "hindi"))
                .andExpect(status().isOk());

        verify(excelTranslationService).translateWorkbook(any(), eq("english"), eq("hindi"), isNull());
    }

    @Test
    @DisplayName("POST /translate/excel should work without destinationLanguage, filling only existing destination columns")
    void shouldAcceptRequestWithoutDestinationLanguage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "words.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "fake-workbook-bytes".getBytes());

        TranslatedSpreadsheet translated = new TranslatedSpreadsheet(
                "translated-workbook-bytes".getBytes(), "words-translated.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        when(excelTranslationService.translateWorkbook(any(), eq("english"), isNull(), isNull()))
                .thenReturn(translated);

        mockMvc.perform(multipart("/translate/excel")
                        .file(file)
                        .param("sourceLanguage", "english"))
                .andExpect(status().isOk());

        verify(excelTranslationService).translateWorkbook(any(), eq("english"), isNull(), isNull());
    }

    @Test
    @DisplayName("POST /translate/excel should return 400 when sourceLanguage is blank")
    void shouldRejectBlankSourceLanguage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "words.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "fake-workbook-bytes".getBytes());

        mockMvc.perform(multipart("/translate/excel")
                        .file(file)
                        .param("sourceLanguage", " ")
                        .param("destinationLanguage", "hindi"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /translate/excel should return 501 with a clear message when the provider isn't implemented yet")
    void shouldReturnNotImplementedForUnsupportedProvider() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "words.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "fake-workbook-bytes".getBytes());

        when(excelTranslationService.translateWorkbook(any(), eq("english"), eq("hindi"), eq(TranslationProviderType.BHASHINI)))
                .thenThrow(new UnsupportedOperationException("Bhashini provider not implemented yet"));

        mockMvc.perform(multipart("/translate/excel")
                        .file(file)
                        .param("sourceLanguage", "english")
                        .param("destinationLanguage", "hindi")
                        .param("provider", "bhashini"))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.status").value(501))
                .andExpect(jsonPath("$.message").value("Bhashini provider not implemented yet"));
    }

    @Test
    @DisplayName("POST /translate/excel should return 400 when provider is an unknown value")
    void shouldRejectUnknownProvider() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "words.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "fake-workbook-bytes".getBytes());

        mockMvc.perform(multipart("/translate/excel")
                        .file(file)
                        .param("sourceLanguage", "english")
                        .param("destinationLanguage", "hindi")
                        .param("provider", "not-a-provider"))
                .andExpect(status().isBadRequest());
    }
}
