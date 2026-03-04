package org.egov.processor.settings;


import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class VideoQualitySettings {
    private final String resolution;
    private final String label;
    private final int crf;
    private final String audioBitRate;
    private final boolean isOriginal;
    private final int bitRate;

    public static VideoQualitySettings of(String resolution, String label, int crf, String audioBitRate, boolean isOriginal) {
        log.trace("Method invoked: of, resolution: {}, label: {}, crf: {}", resolution, label, crf);
        int bitRate = calculateBitRate(resolution);
        log.debug("Creating VideoQualitySettings: label={}, resolution={}, bitRate={}", label, resolution, bitRate);
        return new VideoQualitySettings(resolution, label, crf, audioBitRate, isOriginal, bitRate);
    }

    private static int calculateBitRate(String resolution) {
        log.trace("Method invoked: calculateBitRate, resolution: {}", resolution);
        int pixels = parsePixels(resolution);
        int bitRate;
        if (pixels == 0) {
            bitRate = 8_000_000; // Default for "original"
        } else if (pixels <= 256 * 144) {
            bitRate = 300_000;
        } else if (pixels <= 426 * 240) {
            bitRate = 700_000;
        } else if (pixels <= 854 * 480) {
            bitRate = 1_500_000;
        } else if (pixels <= 1280 * 720) {
            bitRate = 3_000_000;
        } else if (pixels <= 1920 * 1080) {
            bitRate = 6_000_000;
        } else {
            bitRate = 8_000_000;
        }
        log.debug("Calculated bitrate: {} for resolution: {} (pixels: {})", bitRate, resolution, pixels);
        return bitRate;
    }

    private static int parsePixels(String resolution) {
        log.trace("Method invoked: parsePixels, resolution: {}", resolution);
        if ("original".equalsIgnoreCase(resolution)) {
            log.debug("Resolution is 'original', returning 0 pixels");
            return 0; // Handle ORIGINAL case
        }
        String[] parts = resolution.split("x");

        try {
            int pixels = parts.length == 2 ? Optional.ofNullable(parts[0])
                    .map(Integer::parseInt)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid resolution format: " + resolution))
                    * Optional.ofNullable(parts[1])
                    .map(Integer::parseInt)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid resolution format: " + resolution))
                    : 0;
            log.debug("Parsed pixels: {} from resolution: {}", pixels, resolution);
            return pixels;
        } catch (IllegalArgumentException e) {
            log.error("Invalid resolution format: {}", resolution, e);
            throw e;
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%s, CRF: %d, Audio: %s, Bitrate: %d kbps, Original: %b)",
                label, resolution, crf, audioBitRate, bitRate / 1000, isOriginal);
    }
}

