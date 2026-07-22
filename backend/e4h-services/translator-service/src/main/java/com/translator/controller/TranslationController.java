package com.translator.controller;

import com.translator.dto.TranslationRequest;
import com.translator.dto.TranslationResponse;
import com.translator.dto.TranslationRowRequest;
import com.translator.dto.TranslationRowResponse;
import com.translator.service.RowTranslationService;
import com.translator.service.TranslationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class TranslationController {

    private final TranslationService translationService;
    private final RowTranslationService rowTranslationService;

    public TranslationController(TranslationService translationService, RowTranslationService rowTranslationService) {
        this.translationService = translationService;
        this.rowTranslationService = rowTranslationService;
    }

    @PostMapping("/translate")
    public ResponseEntity<TranslationResponse> translate(@Valid @RequestBody TranslationRequest request) {
        TranslationResponse response = translationService.translate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/translate/rows")
    public ResponseEntity<TranslationRowResponse> translateRows(@Valid @RequestBody TranslationRowRequest request) {
        TranslationRowResponse response = rowTranslationService.translateRows(request);
        return ResponseEntity.ok(response);
    }
}
