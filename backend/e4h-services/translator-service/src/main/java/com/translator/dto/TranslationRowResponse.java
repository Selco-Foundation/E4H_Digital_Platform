package com.translator.dto;

import java.util.List;
import java.util.Map;

public record TranslationRowResponse(List<Map<String, String>> rows) {
}
