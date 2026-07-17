package com.translator.service;

import com.translator.dto.TranslationItemDto;
import com.translator.dto.TranslationRequest;
import com.translator.dto.TranslationResponse;
import com.translator.model.TranslationResult;
import com.translator.provider.TranslationProvider;
import com.translator.provider.TranslationProviderFactory;
import com.translator.provider.TranslationProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TranslationService {

    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);
    private static final TranslationProviderType DEFAULT_PROVIDER_TYPE = TranslationProviderType.GEMINI;

    private final TranslationProviderFactory translationProviderFactory;

    public TranslationService(TranslationProviderFactory translationProviderFactory) {
        this.translationProviderFactory = translationProviderFactory;
    }

    public TranslationResponse translate(TranslationRequest request) {
        long startTime = System.currentTimeMillis();
        TranslationProviderType providerType = Objects.requireNonNullElse(request.provider(), DEFAULT_PROVIDER_TYPE);

        log.info(
                "Incoming translation request: provider={}, sourceLanguage='{}', destinationLanguage='{}', wordCount={}",
                providerType,
                request.sourceLanguage(),
                request.destinationLanguage(),
                request.words().size()
        );

        TranslationProvider provider = translationProviderFactory.getProvider(providerType);
        log.info("Provider selected: {}", provider.getProviderName());

        List<TranslationResult> results = provider.translate(
                request.sourceLanguage(),
                request.destinationLanguage(),
                request.words()
        );

        TranslationResponse response = toResponse(request, results);

        long elapsedMs = System.currentTimeMillis() - startTime;
        log.info(
                "Translation completed: provider={}, wordCount={}, executionTimeMs={}",
                provider.getProviderName(),
                request.words().size(),
                elapsedMs
        );

        return response;
    }

    private TranslationResponse toResponse(TranslationRequest request, List<TranslationResult> results) {
        List<TranslationItemDto> translations = results.stream()
                .map(result -> new TranslationItemDto(result.source(), result.translated()))
                .toList();

        return new TranslationResponse(
                request.sourceLanguage(),
                request.destinationLanguage(),
                translations
        );
    }
}
