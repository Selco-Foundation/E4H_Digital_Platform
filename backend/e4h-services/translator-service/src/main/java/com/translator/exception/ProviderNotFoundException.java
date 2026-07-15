package com.translator.exception;

public class ProviderNotFoundException extends TranslationException {

    public ProviderNotFoundException(String providerName) {
        super("Translation provider not found: " + providerName);
    }
}
