package com.translator.service;

/**
 * Formats a language name for use as a generated column/field header
 * (e.g. "hindi" -&gt; "Hindi"). Shared by {@link ExcelTranslationService}
 * and {@link RowTranslationService} so both name a newly-added destination
 * field the same way.
 */
final class LanguageNames {

    private LanguageNames() {
    }

    static String capitalize(String value) {
        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            return trimmed;
        }

        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }
}
