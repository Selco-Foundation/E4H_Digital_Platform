package org.egov.im.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.settings.VideoQualitySettings;
import org.egov.im.util.DirectoryUtil;
import org.egov.im.util.StorageUtil;
import org.egov.im.util.VideoUtil;
import org.egov.im.web.models.ProcessingContext;

import org.egov.im.web.models.storage.StorageResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoUtil videoUtil;
    private final FFmpegService fFmpegService;
    private final DirectoryUtil directoryUtil;
    private final StorageUtil storageUtil;

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
            MultipartFile multipartFile = fFmpegService.createMasterPlaylist(qualities, context, outputPath);

            // Upload master playlist to storage
            StorageResponse storageResponse = storageUtil.uploadToHLSFileStorage(List.of(multipartFile), context);
            log.info("Successfully created and uploaded master playlist: {}", storageResponse);

            return storageResponse; // Returns immediately without waiting for async processing

        } catch (Exception e) {
            log.error("Error processing video for videoId: {}", context.getVideoId(), e);
            cleanup(context, inputFile, outputPath);
            throw new CustomException("VIDEO_PROCESSING_ERROR", "Failed to process video: " + e.getMessage());
        }
    }

    @Async
    public CompletableFuture<Void> processVideoAsync(File inputFile, ProcessingContext context) {
        log.info("Starting async processing for videoId: {}", context.getVideoId());

        Path outputPath = prepareOutputDirectory();

        return CompletableFuture.supplyAsync(() -> getVideoDimensions(inputFile))
                .thenApply(videoUtil::determineQualityLevels)
                .thenCompose(qualities -> processQualitiesInParallel(context, inputFile, outputPath, qualities))
                .thenRun(() -> {
                    log.info("processed all chunk qualities for videoId: {}", context.getVideoId());
                    cleanup(context, inputFile, outputPath);
                })
                .exceptionally(ex -> handleProcessingError(context, inputFile, outputPath, ex));
    }

    private CompletableFuture<Void> processQualitiesInParallel(
            ProcessingContext context, File inputFile, Path outputPath, List<VideoQualitySettings> qualities) {
        log.info("Processing videoId: {} and qualities: {}", context.getVideoId(), qualities);

        List<CompletableFuture<Void>> uploadFutures = qualities.stream()
                .map(quality -> fFmpegService.processQuality(context, inputFile.getAbsolutePath(), outputPath, quality)
                        .thenCompose(outputFilePath ->
                                uploadProcessedFile(context, outputPath, outputFilePath)))
                .toList();

        return CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<Void> uploadProcessedFile(ProcessingContext context, Path outputPath, String outputFilePath) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Uploading processed file: {} for videoId: {}", outputFilePath, context.getVideoId());

                Path directoryPath = outputPath.resolve(String.format("%s%s", outputPath, outputFilePath));

                List<Path> files;
                try (Stream<Path> fileStream = Files.list(directoryPath)) {
                    files = fileStream.toList();
                }

                // Convert files to MultipartFile and upload
                List<MultipartFile> multipartFiles = files.stream()
                        .map(file -> {
                            String resolvedPath =
                                    String.format("%s/%s", context.getVideoId(), videoUtil.pathExtractor(file.toString(), OUTPUT_DIR));
                            return videoUtil.convertFileToMultipartFile(file.toFile(), resolvedPath);
                        })
                        .toList();

                // Upload files to HLS storage
                storageUtil.uploadToHLSFileStorage(multipartFiles, context);

            } catch (IOException e) {
                log.error("Error uploading processed files: {}", e.getMessage(), e);
                throw new CustomException("Error uploading files", e.getMessage());
            }
        });
    }


    private void cleanup(ProcessingContext context, File inputFile, Path outputPath) {
        storageUtil.cleanupTemporaryFiles(context.getVideoId(), inputFile, outputPath);
    }

    private Void handleProcessingError(ProcessingContext context, File inputFile, Path outputPath, Throwable ex) {
        log.error("Error processing video asynchronously for videoId: {}", context.getVideoId(), ex);
        storageUtil.cleanupTemporaryFiles(context.getVideoId(), inputFile, outputPath);
        return null;  // Ensure CompletableFuture chain is not broken
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
}

