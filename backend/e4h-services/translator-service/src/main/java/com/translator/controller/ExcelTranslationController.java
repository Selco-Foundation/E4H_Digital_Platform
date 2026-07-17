package com.translator.controller;

import com.translator.provider.TranslationProviderType;
import com.translator.service.ExcelTranslationService;
import com.translator.service.TranslatedSpreadsheet;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/translate/excel")
public class ExcelTranslationController {

    private final ExcelTranslationService excelTranslationService;

    public ExcelTranslationController(ExcelTranslationService excelTranslationService) {
        this.excelTranslationService = excelTranslationService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> translateExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sourceLanguage") @NotBlank(message = "sourceLanguage must not be blank") String sourceLanguage,
            @RequestParam(value = "destinationLanguage", required = false) String destinationLanguage,
            @RequestParam(value = "provider", required = false) TranslationProviderType provider) {

        TranslatedSpreadsheet translated = excelTranslationService.translateWorkbook(
                file, sourceLanguage, destinationLanguage, provider);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(translated.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + translated.filename() + "\"")
                .body(translated.content());
    }
}
