package org.egov.im.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VideoQualityConfig {
    LOWEST_144P("256x144", "144p", 30, "64k", false),
    LOW_240P("426x240", "240p", 28, "96k", false),
    SD_480P("854x480", "480p", 26, "128k", false),
    HD_720P("1280x720", "720p", 23, "160k", false),
    FHD_1080P("1920x1080", "1080p", 20, "192k", false),
    ORIGINAL("original", "original", 0, "192k", true); // No resolution needed

    private final String resolution;
    private final String label;
    private final int crf;
    private final String audioBitRate;
    private final boolean original;
    private int bitRate;

    static {
        for (VideoQualityConfig quality : values()) {
            quality.bitRate = calculateBitRate(quality.resolution);
        }
    }

    public int getMaxBitrate() {
        return (int) (bitRate * 1.2);
    }

    public int getBufferSize() {
        return bitRate * 2;
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
        if ("original".equals(resolution)) return 0; // Handle ORIGINAL case
        String[] parts = resolution.split("x");
        try {
            return Integer.parseInt(parts[0]) * Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid resolution format: " + resolution);
        }
    }
}

