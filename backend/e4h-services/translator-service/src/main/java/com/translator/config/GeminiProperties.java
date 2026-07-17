package com.translator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(
        String apiKey,
        String model,
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout
) {
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(20);

    public GeminiProperties {
        if (connectTimeout == null) {
            connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        }
        if (readTimeout == null) {
            readTimeout = DEFAULT_READ_TIMEOUT;
        }
    }
}
