package org.egov.im.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.im.settings.VideoQualityConfig;
import org.egov.im.web.models.ProcessingContext;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class VideoUtil {

    public int getBandwidthForResolution(int width, int height) {
        // Estimate bandwidth based on resolution
        int pixels = width * height;
        if (pixels <= 256 * 144) return 300000;
        if (pixels <= 426 * 240) return 700000;
        if (pixels <= 854 * 480) return 1500000;
        if (pixels <= 1280 * 720) return 3000000;
        if (pixels <= 1920 * 1080) return 6000000;
        return 8000000; // for higher resolutions
    }

    public MultipartFile convertFileToMultipartFile(File file, String path) {
        byte[] fileContent;
        try {
            fileContent = Files.readAllBytes(file.toPath());

            // Set MIME type manually if it's a .ts file
            String contentType = Files.probeContentType(file.toPath());
            if (file.getName().endsWith(".ts")) {
                contentType = "video/mp2t"; // Correct MIME type for HLS .ts files
            }

            return ByteArrayMultipartFile.builder()
                    .content(fileContent)
                    .name(String.format("%s/%s", path, file.getName()))
                    .originalFilename(String.format("%s/%s", path, file.getName()))
                    .contentType(contentType)
                    .build();
        } catch (IOException e) {
            throw new CustomException("ERROR_CONVERTING_TO_MULTIPARTFILE", e.getMessage());
        }
    }

    public String[] getVideoDimensions(String videoPath) {
        final String FFPROBE_PATH = "/usr/bin/ffprobe";

        List<String> command = List.of(
                FFPROBE_PATH, "-v", "error", "-select_streams", "v:0",
                "-show_entries", "stream=width,height", "-of", "csv=s=x:p=0",
                videoPath
        );

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String dimensions = reader.readLine();
                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    log.error("ffprobe failed for video: {}. Exit code: {}", videoPath, exitCode);
                    throw new CustomException("FFprobe execution failed", "Exit code: " + exitCode);
                }

                if (dimensions != null && !dimensions.isEmpty()) {
                    return dimensions.split("x");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted while getting video dimensions: {}", videoPath, e);
            throw new CustomException("Thread interrupted while getting video dimensions", e.getMessage());
        } catch (IOException e) {
            log.error("Error executing ffprobe for video: {}", videoPath, e);
            throw new CustomException("Error executing ffprobe", e.getMessage());
        }

        log.warn("No video dimensions found for {}", videoPath);
        return new String[]{"0", "0"}; // Default return value
    }



    // Determines which resolutions to create based on the original resolution
    public List<VideoQualityConfig> determineQualityLevels(String[] dimensions) {
        if (dimensions == null || dimensions.length < 2) {
            log.error("Could not determine input video dimensions");
            return List.of();
        }

        int width;
        int height;

        try {
            width = Integer.parseInt(dimensions[0]);
            height = Integer.parseInt(dimensions[1]);
        } catch (NumberFormatException e) {
            log.error("Invalid video dimensions format: {}", Arrays.toString(dimensions), e);
            return List.of();
        }

        List<VideoQualityConfig> qualityLevels = new ArrayList<>(5);

        if (height >= 1080) qualityLevels.add(VideoQualityConfig.FHD_1080P);
        if (height >= 720)  qualityLevels.add(VideoQualityConfig.HD_720P);
        if (height >= 480)  qualityLevels.add(VideoQualityConfig.SD_480P);
        if (height >= 240)  qualityLevels.add(VideoQualityConfig.LOW_240P);
        if (height >= 144)  qualityLevels.add(VideoQualityConfig.LOWEST_144P);

        qualityLevels.add(VideoQualityConfig.ORIGINAL); //adds the original

        log.info("Determined quality levels for input video ({}x{}): {}", width, height, qualityLevels);
        return qualityLevels;
    }

    /**
     * Generates the #EXT-X-STREAM-INF entry for the original quality.
     */
    public String getOriginalStreamInfo(ProcessingContext context, Path outputPath, String label) {
        String originalPlaylistPath
                = outputPath.resolve(String.format("%s/hls/%s/playlist.m3u8", context.getVideoId(), label))
                .toString();
        String[] dimensions = getVideoDimensions(originalPlaylistPath);

        if (dimensions != null && dimensions.length == 2) {
            int width = Integer.parseInt(dimensions[0]);
            int height = Integer.parseInt(dimensions[1]);
            int bandwidth = getBandwidthForResolution(width, height);
            return String.format("#EXT-X-STREAM-INF:BANDWIDTH=%d,RESOLUTION=%dx%d%n%s", bandwidth, width, height,
                    String.format("%s/%s",label, "playlist.m3u8"));
        }

        // Default fallback for unknown resolution
        return "#EXT-X-STREAM-INF:BANDWIDTH=8000000,RESOLUTION=1920x1080%n";
    }

}
