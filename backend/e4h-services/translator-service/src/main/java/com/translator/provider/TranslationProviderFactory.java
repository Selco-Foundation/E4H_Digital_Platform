package com.translator.provider;

import com.translator.exception.ProviderNotFoundException;
import com.translator.provider.gemini.GeminiTranslationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Resolves the active {@link TranslationProvider} by {@link TranslationProviderType}.
 * Adding a new backend means creating its provider implementation and adding a
 * single map entry here - no other layer changes.
 */
@Component
public class TranslationProviderFactory {

    private static final Logger log = LoggerFactory.getLogger(TranslationProviderFactory.class);
    private static final TranslationProviderType DEFAULT_PROVIDER_TYPE = TranslationProviderType.GEMINI;

    private final Map<TranslationProviderType, Supplier<TranslationProvider>> providerSuppliers;

    public TranslationProviderFactory(GeminiTranslationProvider geminiTranslationProvider) {
        this.providerSuppliers = Map.of(
                TranslationProviderType.GEMINI, () -> geminiTranslationProvider,
                TranslationProviderType.BHASHINI, () -> {
                    throw new UnsupportedOperationException("Bhashini provider not implemented yet");
                },
                TranslationProviderType.GOOGLE, () -> {
                    throw new UnsupportedOperationException("Google provider not implemented yet");
                }
        );
    }

    public TranslationProvider getProvider(TranslationProviderType providerType) {
        TranslationProviderType resolvedType = Objects.requireNonNullElse(providerType, DEFAULT_PROVIDER_TYPE);
        Supplier<TranslationProvider> supplier = providerSuppliers.get(resolvedType);

        if (supplier == null) {
            throw new ProviderNotFoundException(String.valueOf(resolvedType));
        }

        TranslationProvider provider = supplier.get();
        log.debug("Selected translation provider: {}", provider.getProviderName());
        return provider;
    }
}
