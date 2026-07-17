package com.translator.provider;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Lets {@code @RequestParam}/{@code @PathVariable} binding accept the same
 * case-insensitive provider values as the JSON body contract
 * (see {@link TranslationProviderType#fromValue(String)}).
 */
@Component
public class StringToTranslationProviderTypeConverter implements Converter<String, TranslationProviderType> {

    @Override
    public TranslationProviderType convert(String source) {
        return TranslationProviderType.fromValue(source);
    }
}
