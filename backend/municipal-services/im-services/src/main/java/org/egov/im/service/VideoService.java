package org.egov.im.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.settings.VideoQualitySettings;
import org.egov.im.util.DirectoryUtil;
import org.egov.im.util.VideoUtil;
import org.egov.im.web.models.ProcessingContext;

import org.egov.im.web.models.storage.StorageResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.scheduling.annotation.Async;
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

    public StorageResponse processVideo(File inputFile, ProcessingContext context) {
        log.info("Starting video processing for videoId: {}", context.getVideoId());

        Path outputPath = prepareOutputDirectory();

        try {
            // Get original video dimensions
            String[] originalDimensions = getVideoDimensions(inputFile);

            log.info("Original dimensions detected - Height: {} x Width: {}", originalDimensions[0], originalDimensions[1]);

            // Determine quality levels for the video
            List<VideoQualitySettings> qualities = videoUtil.determineQualityLevels(originalDimensions);

            // Create the master playlist
            StorageResponse storageResponse = fFmpegService.createMasterPlaylist(qualities, context, outputPath);
            log.info("Successfully created and uploaded master playlist: {}", storageResponse);

            return storageResponse; // Returns immediately without waiting for async processing

        } catch (Exception e) {
            log.error("Error processing video for videoId: {}", context.getVideoId(), e);
            //cleanupTemporaryFiles(context.getVideoId(), inputFile, outputPath);
            throw new CustomException("VIDEO_PROCESSING_ERROR", "Failed to process video: " + e.getMessage());
        } finally {
            log.info("Successfully processed all master files for videoId: {}", context.getVideoId());
            //cleanupTemporaryFiles(context.getVideoId(), inputFile,  outputPath);
        }
    }

    @Async
    public CompletableFuture<Void> processVideoAsync(File inputFile, ProcessingContext context) {
        log.info("Starting async processing for videoId: {}", context.getVideoId());

        Path outputPath = prepareOutputDirectory();

        return CompletableFuture.supplyAsync(() -> getVideoDimensions(inputFile))
                .thenApply(videoUtil::determineQualityLevels)
                .thenCompose(qualities ->
                        CompletableFuture.allOf(qualities.stream()
                                        .map(quality -> fFmpegService.processQuality(context,
                                                inputFile.getAbsolutePath(), outputPath, quality))
                                        .toArray(CompletableFuture[]::new)
                        )
                )
                .thenRun(() -> {
                    log.info("Successfully processed all chunks qualities for videoId: {}", context.getVideoId());
                   // cleanupTemporaryFiles(context.getVideoId(), inputFile, outputPath);
                })
                .exceptionally(ex -> {
                    log.error("Error processing video asynchronously for videoId: {}", context.getVideoId(), ex);
                  //  cleanupTemporaryFiles(context.getVideoId(), inputFile,  outputPath);
                    return null;
                });
    }


    /**
     * Prepares the output directory.
     */
    private Path prepareOutputDirectory() {
        Path outputPath = Paths.get(System.getProperty("user.dir"), OUTPUT_DIR);
        return directoryUtil.createDirectory(outputPath.toAbsolutePath().toString());
    }

    /**
     * Retrieves video dimensions.
     */
    private String[] getVideoDimensions(File inputFile) {
        String[] dimensions = videoUtil.getVideoDimensions(inputFile.getAbsolutePath());
        if (dimensions.length < 2) {
            throw new CustomException("INVALID_DIMENSIONS", "Unable to retrieve video dimensions");
        }
        return dimensions;
    }

    /**
     * Cleans up temporary files after processing.
     */
    private void cleanupTemporaryFiles(String videoId, File tempFile,  Path outputPath) {
        log.info("deleting temporary files");
        if(tempFile.exists()) {
            boolean deleted = tempFile.delete();
            log.info("temp file: {} deleted: {}", tempFile.getName(), deleted);
        }

        log.info("Cleaning up temporary files for videoId: {}", videoId);
        Path videoDirectory = outputPath.resolve(videoId);

        try {
            if (Files.exists(videoDirectory)) {
                Files.walk(videoDirectory)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                                log.debug("Deleted: {}", path);
                            } catch (IOException e) {
                                log.warn("Failed to delete: {}", path, e);
                            }
                        });
            }

            // Delete master playlist
            Path masterPlaylist = outputPath.resolve(videoId + "_master.m3u8");
            if (Files.exists(masterPlaylist)) {
                Files.delete(masterPlaylist);
                log.debug("Deleted master playlist: {}", masterPlaylist);
            }

            log.info("Cleanup completed for videoId: {}", videoId);
        } catch (IOException e) {
            log.error("Error during cleanup for videoId: {}", videoId, e);
        }
    }
}

