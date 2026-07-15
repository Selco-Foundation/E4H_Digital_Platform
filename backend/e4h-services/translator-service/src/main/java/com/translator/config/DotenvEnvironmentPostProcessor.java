package com.translator.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Loads a local ".env" file (if present) into the Spring Environment so
 * placeholders such as ${GEMINI_API_KEY} resolve without requiring the
 * variable to be exported in the shell. Real OS environment variables
 * and system properties still take precedence over ".env" values.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String DOTENV_FILENAME = ".env";
    private static final String PROPERTY_SOURCE_NAME = "dotenvProperties";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path dotenvPath = Path.of(DOTENV_FILENAME);

        if (!Files.isReadable(dotenvPath)) {
            return;
        }

        Map<String, Object> dotenvProperties = readDotenv(dotenvPath);

        if (!dotenvProperties.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, dotenvProperties));
        }
    }

    private Map<String, Object> readDotenv(Path dotenvPath) {
        Map<String, Object> properties = new LinkedHashMap<>();

        try {
            for (String line : Files.readAllLines(dotenvPath)) {
                parseLine(line).ifPresent(entry -> properties.put(entry.getKey(), entry.getValue()));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read .env file", e);
        }

        return properties;
    }

    private Optional<Map.Entry<String, String>> parseLine(String line) {
        String trimmed = line.trim();

        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return Optional.empty();
        }

        int separatorIndex = trimmed.indexOf('=');
        if (separatorIndex <= 0) {
            return Optional.empty();
        }

        String key = trimmed.substring(0, separatorIndex).trim();
        String value = stripQuotes(trimmed.substring(separatorIndex + 1).trim());

        return Optional.of(Map.entry(key, value));
    }

    private String stripQuotes(String value) {
        boolean isQuoted = value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")));

        return isQuoted ? value.substring(1, value.length() - 1) : value;
    }
}
