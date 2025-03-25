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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoUtil videoUtil;
    private final FFmpegService fFmpegService;
    private final DirectoryUtil directoryUtil;
    private final StorageUtil storageUtil;
    private final VideoQualityProcessor videoQualityProcessor;
    private final VideoUploaderService uploaderService;


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

            return uploaderService.uploadProcessedFile(context, List.of(multipartFile));

        } catch (Exception e) {
            log.error("Error processing video for videoId: {}", context.getVideoId(), e);
            cleanup(context, inputFile, outputPath);
            throw new CustomException("VIDEO_PROCESSING_ERROR", "Failed to process video: " + e.getMessage());
        }
    }

    @Async
    public void processVideoAsync(File inputFile, ProcessingContext context) {
        log.info("Starting async processing for videoId: {}", context.getVideoId());

        Path outputPath = prepareOutputDirectory();
        String[] dimensions = getVideoDimensions(inputFile);

        List<VideoQualitySettings> qualities = videoUtil.determineQualityLevels(dimensions);

        try {
            for (VideoQualitySettings qualitySettings : qualities) {
                List<MultipartFile> multipartFiles =
                        videoQualityProcessor.processQuality(context, inputFile, outputPath, qualitySettings);

                log.info("Finished processing qualities for videoId: {}", context.getVideoId());

                uploaderService.uploadProcessedFile(context, multipartFiles);

                log.info("Processed all chunk qualities for videoId: {}", context.getVideoId());
            }
            // Cleanup after processing
            cleanup(context, inputFile, outputPath);
        } catch (Exception ex) {
            log.error("Error during video processing for videoId: {}", context.getVideoId(), ex);
            handleProcessingError(context, inputFile, outputPath, ex);
        }
    }

    private void cleanup(ProcessingContext context, File inputFile, Path outputPath) {
        log.info("start cleaning...");
        storageUtil.cleanupTemporaryFiles(context.getVideoId(), inputFile, outputPath);
    }

    private void handleProcessingError(ProcessingContext context, File inputFile, Path outputPath, Throwable ex) {
        log.error("Error processing video asynchronously for videoId: {}", context.getVideoId(), ex);
        storageUtil.cleanupTemporaryFiles(context.getVideoId(), inputFile, outputPath);
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

