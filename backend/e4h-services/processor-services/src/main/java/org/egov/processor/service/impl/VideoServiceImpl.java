package org.egov.processor.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.processor.models.ProcessingContext;
import org.egov.processor.models.storage.StorageResponse;
import org.egov.processor.service.FFMpegService;
import org.egov.processor.service.VideoQualityProcessor;
import org.egov.processor.service.VideoService;
import org.egov.processor.service.VideoUploaderService;
import org.egov.processor.settings.VideoQualitySettings;
import org.egov.processor.utils.DirectoryUtil;
import org.egov.processor.utils.StorageUtil;
import org.egov.processor.utils.VideoUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoUtil videoUtil;
    private final DirectoryUtil directoryUtil;
    private final StorageUtil storageUtil;
    private final VideoQualityProcessor videoQualityProcessor;
    private final VideoUploaderService uploaderService;


    private static final String OUTPUT_DIR = "output";

    public void processVideoAsync(File inputFile, ProcessingContext context) {
        log.trace("Method invoked: processVideoAsync, videoId: {}", context.getVideoId());
        log.info("Starting async video processing for videoId: {}", context.getVideoId());

        log.trace("Preparing output directory");
        Path outputPath = prepareOutputDirectory();
        log.debug("Output directory prepared: {}", outputPath);
        
        log.trace("Retrieving video dimensions");
        String[] dimensions = getVideoDimensions(inputFile);
        log.debug("Video dimensions retrieved: {}x{}", dimensions.length > 0 ? dimensions[0] : "unknown", dimensions.length > 1 ? dimensions[1] : "unknown");

        log.trace("Determining quality levels");
        List<VideoQualitySettings> qualities = videoUtil.determineQualityLevels(dimensions);
        log.debug("Quality levels determined: {}", qualities.size());

        try {
            for (VideoQualitySettings qualitySettings : qualities) {
                log.info("Processing quality level: {} for videoId: {}", qualitySettings.getLabel(), context.getVideoId());
                List<MultipartFile> multipartFiles =
                        videoQualityProcessor.processQuality(context, inputFile, outputPath, qualitySettings);

                log.info("Finished processing quality: {} for videoId: {}", qualitySettings.getLabel(), context.getVideoId());

                log.trace("Uploading processed files");
                uploaderService.uploadProcessedFile(context, multipartFiles);
                log.debug("Uploaded {} files for quality: {}", multipartFiles.size(), qualitySettings.getLabel());
            }
            log.info("Completed processing all quality levels for videoId: {}", context.getVideoId());
            // Cleanup after processing
            cleanup(context, inputFile, outputPath);
        } catch (Exception ex) {
            log.error("Error during video processing for videoId: {}", context.getVideoId(), ex);
            handleProcessingError(context, inputFile, outputPath, ex);
        }
    }

    private void cleanup(ProcessingContext context, File inputFile, Path outputPath) {
        log.trace("Method invoked: cleanup, videoId: {}", context.getVideoId());
        log.info("Starting cleanup for videoId: {}", context.getVideoId());
        storageUtil.cleanupTemporaryFiles(context.getVideoId(), inputFile, outputPath);
    }

    private void handleProcessingError(ProcessingContext context, File inputFile, Path outputPath, Throwable ex) {
        log.trace("Method invoked: handleProcessingError, videoId: {}", context.getVideoId());
        log.error("Error processing video asynchronously for videoId: {}", context.getVideoId(), ex);
        storageUtil.cleanupTemporaryFiles(context.getVideoId(), inputFile, outputPath);
    }

    /**
     * Prepares the output directory.
     */
    private Path prepareOutputDirectory() {
        log.trace("Method invoked: prepareOutputDirectory");
        Path outputPath = Paths.get(System.getProperty("user.dir"), OUTPUT_DIR);
        return directoryUtil.createDirectory(outputPath.toAbsolutePath().toString());
    }

    /**
     * Retrieves video dimensions.
     */
    private String[] getVideoDimensions(File inputFile) {
        log.trace("Method invoked: getVideoDimensions, inputFile: {}", inputFile.getName());
        String[] dimensions = videoUtil.getVideoDimensions(inputFile.getAbsolutePath());
        if (dimensions.length < 2) {
            log.error("Invalid video dimensions retrieved, length: {}", dimensions.length);
            throw new CustomException("INVALID_DIMENSIONS", "Unable to retrieve video dimensions");
        }
        return dimensions;
    }
}


