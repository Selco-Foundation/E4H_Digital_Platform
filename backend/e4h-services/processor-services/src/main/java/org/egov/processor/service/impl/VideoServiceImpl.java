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


