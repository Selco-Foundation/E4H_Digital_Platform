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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoUtil videoUtil;
    private final FFmpegService fFmpegService;
    private final DirectoryUtil directoryUtil;
    private final StorageUtil storageUtil;
    private final VideoUploaderService uploaderService;


    private static final String OUTPUT_DIR = "output";

    public StorageResponse processVideo(File inputFile, ProcessingContext context) {
        log.trace("VideoService::processVideo method invoked");
        log.info("Starting video processing for videoId: {}", context.getVideoId());

        log.trace("Preparing output directory");
        Path outputPath = prepareOutputDirectory();

        try {
            // Get original video dimensions
            log.trace("Retrieving video dimensions");
            String[] originalDimensions = getVideoDimensions(inputFile);

            log.info("Original dimensions detected - Height: {} x Width: {}", originalDimensions[0], originalDimensions[1]);

            // Determine quality levels for the video
            log.trace("Determining quality levels for video");
            List<VideoQualitySettings> qualities = videoUtil.determineQualityLevels(originalDimensions);
            log.debug("Determined {} quality levels for video", qualities.size());

            // Create the master playlist
            log.trace("Creating master playlist");
            MultipartFile multipartFile = fFmpegService.createMasterPlaylist(qualities, context, outputPath);

            log.trace("Uploading processed file");
            StorageResponse response = uploaderService.uploadProcessedFile(context, List.of(multipartFile));
            log.info("Video processing completed successfully for videoId: {}", context.getVideoId());
            return response;

        } catch (Exception e) {
            log.error("Error processing video for videoId: {}", context.getVideoId(), e);
            cleanup(context, inputFile, outputPath);
            throw new CustomException("VIDEO_PROCESSING_ERROR", "Failed to process video: " + e.getMessage());
        }
    }

    private void cleanup(ProcessingContext context, File inputFile, Path outputPath) {
        log.trace("VideoService::cleanup method invoked");
        log.debug("Cleaning up temporary files for videoId: {}", context.getVideoId());
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

