package org.egov.im.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.config.IMConfiguration;
import org.egov.im.settings.VideoQualityFactory;
import org.egov.im.settings.VideoQualitySettings;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
@Component
public class VideoUtil {

    private final VideoQualityFactory videoQualityFactory;
    private final IMConfiguration config;

    public int getBandwidthForResolution(int width, int height) {
        // Estimate bandwidth based on resolution
        int pixels = width * height;
        if (pixels <= 256 * 144) return 300000;
        if (pixels <= 426 * 240) return 700000;
        if (pixels <= 854 * 480) return 1500000;
        if (pixels <= 1280 * 720) return 3000000;
        if (pixels <= 1920 * 1080) return 6000000;
        return 8000000;
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


    public List<MultipartFile> convertToMultipartFiles(ProcessingContext context, Path outputPath, String outputFilePath){

        Path directoryPath = outputPath.resolve(String.format("%s%s", outputPath, outputFilePath));
        List<Path> files;
        try (Stream<Path> fileStream = Files.list(directoryPath)) {
            files = fileStream.toList();

        // Convert files to MultipartFile and upload
            return files.stream()
                .map(file -> {
                    String resolvedPath =
                            String.format("%s/%s", context.getVideoId(), pathExtractor(file.toString(), "output"));
                    return convertFileToMultipartFile(file.toFile(), resolvedPath);
                })
                .toList();

        } catch (IOException e) {
            throw new CustomException("Error converting files to multipart file", e.getMessage());
        }
    }

    public String[] getVideoDimensions(String videoPath) {
        final String FFPROBE_PATH = config.getFfprobePath();

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
    public List<VideoQualitySettings> determineQualityLevels(String[] dimensions) {
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

        List<VideoQualitySettings> qualityLevels = new ArrayList<>(5);

        if (height >= 1080) qualityLevels.add(videoQualityFactory.getQualitySettings("FHD_1080P"));
        if (height >= 720)  qualityLevels.add(videoQualityFactory.getQualitySettings("HD_720P"));
        if (height >= 480)  qualityLevels.add(videoQualityFactory.getQualitySettings("SD_480P"));
        if (height >= 240)  qualityLevels.add(videoQualityFactory.getQualitySettings("LOW_240P"));
        if (height >= 144)  qualityLevels.add(videoQualityFactory.getQualitySettings("LOWEST_144P"));

        //set original video quality
        qualityLevels.add(VideoQualitySettings.of(String.format("%sx%s", width, height),
                "original", 0, "192k", true));

        log.info("Determined quality levels for input video ({}x{}): {}", width, height, qualityLevels);
        return qualityLevels;
    }

    /**
     * Generates the #EXT-X-STREAM-INF entry for the original quality.
     */
    public String getOriginalStreamInfo(String[] dimensions) {

        if (dimensions != null && dimensions.length == 2) {
            int width = Integer.parseInt(dimensions[0]);
            int height = Integer.parseInt(dimensions[1]);
            int bandwidth = getBandwidthForResolution(width, height);
            return String.format("#EXT-X-STREAM-INF:BANDWIDTH=%d,RESOLUTION=%dx%d%n%s", bandwidth, width, height,
                    String.format("original/%s","playlist.m3u8"));
        }

        // Default fallback for unknown resolution
        return "#EXT-X-STREAM-INF:BANDWIDTH=8000000,RESOLUTION=1920x1080%n";
    }

    public String pathExtractor(String fullPath, String indexPath) {
            Path path = Paths.get(fullPath);
            int outputIndex = path.toString().indexOf(indexPath);

            if (outputIndex != -1) {
                return String.format("%s",path.subpath(path.getNameCount() - 3, path.getNameCount() - 1));
            }
            throw new IllegalArgumentException("Invalid path: 'output/' not found");
    }

}
