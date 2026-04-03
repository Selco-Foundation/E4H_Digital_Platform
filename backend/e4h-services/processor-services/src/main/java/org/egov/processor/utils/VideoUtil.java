package org.egov.processor.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.processor.config.ProcessorConfiguration;
import org.egov.processor.models.ProcessingContext;
import org.egov.processor.settings.VideoQualityFactory;
import org.egov.processor.settings.VideoQualitySettings;
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
@Component
@Slf4j
public class VideoUtil {

    private final VideoQualityFactory videoQualityFactory;
    private final ProcessorConfiguration config;

    public List<MultipartFile> convertToMultipartFiles(ProcessingContext context, Path outputPath, String outputFilePath){
        log.trace("Method invoked: convertToMultipartFiles, videoId: {}, outputFilePath: {}", context.getVideoId(), outputFilePath);

        Path directoryPath = outputPath.resolve(String.format("%s%s", outputPath, outputFilePath));
        List<Path> files;
        try (Stream<Path> fileStream = Files.list(directoryPath)) {
            files = fileStream.toList();
            log.debug("Found {} files in directory: {}", files.size(), directoryPath);

            // Convert files to MultipartFile and upload
            return files.stream()
                    .map(file -> {
                        String resolvedPath =
                                String.format("%s/%s", context.getVideoId(), pathExtractor(file.toString(), "output"));
                        return convertFileToMultipartFile(file.toFile(), resolvedPath);
                    })
                    .toList();

        } catch (IOException e) {
            log.error("Error converting files to multipart file for videoId: {}", context.getVideoId(), e);
            throw new CustomException("Error converting files to multipart file", e.getMessage());
        }
    }

    public List<VideoQualitySettings> determineQualityLevels(String[] dimensions) {
        log.trace("Method invoked: determineQualityLevels");
        if (dimensions == null || dimensions.length < 2) {
            log.error("Could not determine input video dimensions, dimensions array is null or invalid");
            return List.of();
        }

        int width;
        int height;

        try {
            width = Integer.parseInt(dimensions[0]);
            height = Integer.parseInt(dimensions[1]);
            log.debug("Parsed video dimensions: {}x{}", width, height);
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

        log.info("Determined {} quality levels for input video ({}x{})", qualityLevels.size(), width, height);
        log.debug("Quality levels: {}", qualityLevels.stream().map(VideoQualitySettings::getLabel).toList());
        return qualityLevels;
    }

    public String[] getVideoDimensions(String videoPath) {
        log.trace("Method invoked: getVideoDimensions, videoPath: {}", videoPath);
        final String FFPROBE_PATH = config.getFfprobePath();

        List<String> command = List.of(
                FFPROBE_PATH, "-v", "error", "-select_streams", "v:0",
                "-show_entries", "stream=width,height", "-of", "csv=s=x:p=0",
                videoPath
        );

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            log.debug("Executing ffprobe command to get video dimensions");
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String dimensions = reader.readLine();
                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    log.error("ffprobe failed for video: {}, exit code: {}", videoPath, exitCode);
                    throw new CustomException("FFprobe execution failed", "Exit code: " + exitCode);
                }

                if (dimensions != null && !dimensions.isEmpty()) {
                    log.debug("Retrieved video dimensions: {}", dimensions);
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

        log.warn("No video dimensions found for video: {}", videoPath);
        return new String[]{"0", "0"}; // Default return value
    }

    public MultipartFile convertFileToMultipartFile(File file, String path) {
        log.trace("Method invoked: convertFileToMultipartFile, filename: {}", file.getName());
        byte[] fileContent;
        try {
            log.debug("Reading file content, file size: {} bytes", file.length());
            fileContent = Files.readAllBytes(file.toPath());

            // Set MIME type manually if it's a .ts file
            String contentType = Files.probeContentType(file.toPath());
            if (file.getName().endsWith(".ts")) {
                contentType = "video/mp2t"; // Correct MIME type for HLS .ts files
            }
            log.debug("Content type determined: {}", contentType);

            return ByteArrayMultipartFile.builder()
                    .content(fileContent)
                    .name(String.format("%s/%s", path, file.getName()))
                    .originalFilename(String.format("%s/%s", path, file.getName()))
                    .contentType(contentType)
                    .build();
        } catch (IOException e) {
            log.error("Error converting file to multipart file: {}", file.getName(), e);
            throw new CustomException("ERROR_CONVERTING_TO_MULTIPARTFILE", e.getMessage());
        }
    }

    public String pathExtractor(String fullPath, String indexPath) {
        log.trace("Method invoked: pathExtractor, fullPath: {}, indexPath: {}", fullPath, indexPath);
        Path path = Paths.get(fullPath);
        int outputIndex = path.toString().indexOf(indexPath);

        if (outputIndex != -1) {
            String extractedPath = String.format("%s",path.subpath(path.getNameCount() - 3, path.getNameCount() - 1));
            log.debug("Extracted path: {}", extractedPath);
            return extractedPath;
        }
        log.error("Invalid path, index '{}' not found in: {}", indexPath, fullPath);
        throw new IllegalArgumentException("Invalid path: 'output/' not found");
    }

}
