package org.egov.im.settings;

import lombok.*;

import java.util.Optional;

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
        return new VideoQualitySettings(resolution, label, crf, audioBitRate, isOriginal, calculateBitRate(resolution));
    }

    private static int calculateBitRate(String resolution) {
        int pixels = parsePixels(resolution);
        if (pixels == 0) return 8_000_000; // Default for "original"

        if (pixels <= 256 * 144) return 300_000;
        if (pixels <= 426 * 240) return 700_000;
        if (pixels <= 854 * 480) return 1_500_000;
        if (pixels <= 1280 * 720) return 3_000_000;
        if (pixels <= 1920 * 1080) return 6_000_000;
        return 8_000_000;
    }

    private static int parsePixels(String resolution) {
        if ("original".equalsIgnoreCase(resolution)) return 0; // Handle ORIGINAL case
        String[] parts = resolution.split("x");

        return parts.length == 2 ? Optional.ofNullable(parts[0])
                .map(Integer::parseInt)
                .orElseThrow(() -> new IllegalArgumentException("Invalid resolution format: " + resolution))
                * Optional.ofNullable(parts[1])
                .map(Integer::parseInt)
                .orElseThrow(() -> new IllegalArgumentException("Invalid resolution format: " + resolution))
                : 0;
    }

    @Override
    public String toString() {
        return String.format("%s (%s, CRF: %d, Audio: %s, Bitrate: %d kbps, Original: %b)",
                label, resolution, crf, audioBitRate, bitRate / 1000, isOriginal);
    }
}
