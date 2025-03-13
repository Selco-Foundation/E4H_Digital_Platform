package org.egov.im.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FFmpegCommandGenerator {

    private static final String BASE_COMMAND =
            "ffmpeg -i %s -hls_time 10 -hls_list_size 0 %s";

    private static final String OPTIMIZED_COMMAND =
            "ffmpeg -i %s -max_muxing_queue_size 2048 -c:v libx264 -preset %s " +
                    "-crf %d -s %s -c:a aac -b:a %s -maxrate 1500K " +
                    "-bufsize 1024K -hls_time 10 -hls_list_size 0 " +
                    "-hls_flags split_by_time -f hls %s";

    /**
     * Generates an FFmpeg command for HLS conversion (original quality, no resizing).
     *
     * @param inputFilePath  The path of the input video file.
     * @param outputFilePath The output path for the HLS playlist.
     * @return The formatted FFmpeg command string.
     */
    public String getBaseCommand(String inputFilePath, String outputFilePath) {
        return String.format(BASE_COMMAND, inputFilePath, outputFilePath);
    }

    /**
     * Generates an optimized FFmpeg command for HLS conversion with specified quality.
     *
     * @param inputFilePath  The path of the input video file.
     * @param preset         The FFmpeg preset (e.g., "slow", "medium", "fast").
     * @param crf            The Constant Rate Factor (CRF) for quality control.
     * @param resolution     The desired video resolution (e.g., "1280x720").
     * @param outputFilePath The output path for the HLS playlist.
     * @return The formatted FFmpeg command string.
     */
    public String getOptimizedCommand(String inputFilePath,
                                      String preset,
                                      int crf,
                                      String resolution,
                                      String audioBitRate,
                                      String outputFilePath) {
        return String.format(OPTIMIZED_COMMAND, inputFilePath, preset, crf, resolution, audioBitRate, outputFilePath);
    }
}
