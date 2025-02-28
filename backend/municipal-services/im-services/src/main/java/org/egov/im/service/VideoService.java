package org.egov.im.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.settings.VideoQualityConfig;
import org.egov.im.util.DirectoryUtil;
import org.egov.im.util.VideoUtil;
import org.egov.im.web.models.ProcessingContext;

import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoUtil videoUtil;
    private final FFmpegService fFmpegService;
    private final DirectoryUtil directoryUtil;

    private static final String OUTPUT_DIR = "output";
    private Path outputPath = Paths.get(System.getProperty("user.dir"), OUTPUT_DIR);

    @PostConstruct
    private void init() {
        outputPath = directoryUtil.createDirectory(outputPath.toAbsolutePath().toString());
        log.info("Created output directory at: {}", outputPath);
    }

    public void processVideo(File inputFile, ProcessingContext context) {
        log.info("Starting video processing for videoId: {}", context.getVideoId());

        try {
            // Get original video dimensions
            String[] originalDimensions = videoUtil.getVideoDimensions(inputFile.getAbsolutePath());

            if (originalDimensions.length < 2) {
                throw new CustomException("INVALID_DIMENSIONS", "Unable to retrieve video dimensions");
            }

            log.info("Original dimensions detected - Height: {} x Width: {}", originalDimensions[0], originalDimensions[1]);

            // Determine quality levels for the video
            List<VideoQualityConfig> qualities = videoUtil.determineQualityLevels(originalDimensions);

            List<CompletableFuture<Void>> processingFutures = qualities.stream()
                    .map(videoQuality ->
                            fFmpegService.processQuality(context, inputFile.getAbsolutePath(), outputPath, videoQuality))
                    .toList();

            CompletableFuture.allOf(processingFutures.toArray(new CompletableFuture[0]))
                    .exceptionally(ex -> {
                        log.error("Error processing HLS chunks for videoId: {}", context.getVideoId(), ex);
                        throw new CustomException("Failed to process video qualities", ex.getMessage());
                    }).join();

            // Create the master playlist
            fFmpegService.createMasterPlaylist(qualities, context, outputPath);

            // Clean up temporary files
            cleanupTemporaryFiles(context.getVideoId());

        } catch (Exception e) {
            log.error("Error processing video for videoId: {}", context.getVideoId(), e);
            cleanupTemporaryFiles(context.getVideoId()); // Ensure cleanup happens even on error
            throw new CustomException("VIDEO_PROCESSING_ERROR", "Failed to process video: " + e.getMessage());
        }
    }

    /**
     * Clean up temporary files after processing
     *
     * @param videoId the ID of the processed video
     */
    private void cleanupTemporaryFiles(String videoId) {
        log.info("Cleaning up temporary files for videoId: {}", videoId);
        try {
            // Delete transcoded mp4 files
            Path videoDirectory = outputPath.resolve(videoId);
            if (Files.exists(videoDirectory)) {
                Files.walk(videoDirectory)
                        .filter(path -> !Files.isDirectory(path))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                                log.debug("Deleted temporary file: {}", path);
                            } catch (IOException e) {
                                log.warn("Failed to delete temporary file: {}", path, e);
                            }
                        });

                // Delete directories (bottom-up)
                Files.walk(videoDirectory)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.warn("Failed to delete directory: {}", path, e);
                            }
                        });
            }

            // Delete master playlist
            Path masterPlaylist = outputPath.resolve(videoId + "_master.m3u8");
            if (Files.exists(masterPlaylist)) {
                Files.delete(masterPlaylist);
            }

            log.info("Cleanup completed for videoId: {}", videoId);
        } catch (IOException e) {
            log.error("Error during cleanup for videoId: {}", videoId, e);
        }
    }


}

