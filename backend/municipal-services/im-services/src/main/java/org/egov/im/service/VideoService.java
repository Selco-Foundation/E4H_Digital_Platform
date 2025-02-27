package org.egov.im.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.util.StorageUtil;
import org.egov.im.util.VideoUtil;
import org.egov.im.web.models.ProcessingContext;
import org.egov.im.web.models.ProcessingTimes;

import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

    private final StorageUtil storageUtil;
    private final VideoUtil videoUtil;
    private final FFMpegExecutor ffMpegExecutor;

    private static final String OUTPUT_DIR = "output";
    private final Path outputPath = Paths.get(System.getProperty("user.dir"), OUTPUT_DIR);

    @PostConstruct
    private void init() {
        try {
            Files.createDirectories(outputPath);
            log.info("Created output directory at: {}", outputPath);
        } catch (IOException e) {
            log.error("Failed to create output directory", e);
            throw new CustomException("Failed to create output directory", e.getMessage());
        }
    }

    public void processVideo(File inputFile, ProcessingContext context) {

        log.info("Starting video processing for videoId: {}", context.getVideoId());

        try {
            Map<String, ProcessingTimes> qualityTimings = new HashMap<>();
            List<String> qualities = getApplicableQualities(inputFile.getAbsolutePath());

            for (String quality : qualities) {
                log.debug("Processing quality: {} for videoId: {}", quality, context.getVideoId());
                qualityTimings.put(quality, new ProcessingTimes());

                String hlsOutputPath = outputPath.resolve(Paths.get(context.getVideoId(), "hls", quality)).toString();

                if ("original".equals(quality)) {
                    processOriginalQuality(inputFile, hlsOutputPath, qualityTimings, context);
                } else {
                    processTranscodedQuality(inputFile, quality, hlsOutputPath, qualityTimings, context);
                }
            }

            // Create master playlist with all qualities including original
            createMasterPlaylist(qualities, context);
            // Clean up temporary files
            cleanupTemporaryFiles(context.getVideoId());
        } catch (Exception e) {
            log.error("Error processing video for videoId: {}", context.getVideoId(), e);
            cleanupTemporaryFiles(context.getVideoId()); // Ensure cleanup happens even on error
            throw new CustomException("VIDEO_PROCESSING_ERROR", "Failed to process video: " + e.getMessage());
        }
    }

    private void processOriginalQuality(File inputFile,
                                        String hlsOutputPath,
                                        Map<String, ProcessingTimes> qualityTimings,
                                        ProcessingContext context) {
        log.info("Processing original quality for videoId: {}", context.getVideoId());

        Instant hlsStart = Instant.now();
        createHlsChunks(inputFile.getAbsolutePath(), hlsOutputPath);
        qualityTimings.get("original").setHlsTime(Duration.between(hlsStart, Instant.now()).toMillis());

        Instant uploadStart = Instant.now();
        uploadHlsChunksToMinio(
                hlsOutputPath, context.getVideoId() + "/hls/original", context);
        qualityTimings.get("original").setUploadTime(Duration.between(uploadStart, Instant.now()).toMillis());
    }

    private void processTranscodedQuality(File inputFile, String quality,
                                          String hlsOutputPath,
                                          Map<String, ProcessingTimes> qualityTimings,
                                          ProcessingContext context) {
        log.info("Processing transcoded quality: {} for videoId: {}", quality, context.getVideoId());

        String outputFilename = outputPath.resolve(context.getVideoId() + "_" + quality + ".mp4").toString();

        Instant transcodeStart = Instant.now();
        transcodeVideo(inputFile.getAbsolutePath(), outputFilename, quality);
        qualityTimings.get(quality).setTranscodeTime(Duration.between(transcodeStart, Instant.now()).toMillis());

        Instant hlsStart = Instant.now();
        createHlsChunks(outputFilename, hlsOutputPath);
        qualityTimings.get(quality).setHlsTime(Duration.between(hlsStart, Instant.now()).toMillis());

        Instant uploadStart = Instant.now();

        // Upload MP4 file
        File outputFile = new File(outputFilename);
        try {
            storageUtil.uploadToFileStorage(List.of(videoUtil.convertFileToMultipartFile(outputFile)), context);
        } catch (IOException e) {
            log.error("Error uploading transcoded video file for quality: {}", quality, e);
            throw new CustomException("Failed to upload transcoded video", e.getMessage());
        }

        // Upload HLS chunks
        uploadHlsChunksToMinio(hlsOutputPath, context.getVideoId() + "/hls/" + quality, context);

        qualityTimings.get(quality).setUploadTime(Duration.between(uploadStart, Instant.now()).toMillis());
    }


    private void transcodeVideo(String inputPath, String outputPath, String quality) {
        // Use FFmpeg to transcode video with optimization flags
        String command = String.format("ffmpeg -i %s -vf scale=%s -c:v libx264 -preset ultrafast -crf 35 -c:a copy %s",
                inputPath, videoUtil.getScaleForQuality(quality), outputPath);
        ffMpegExecutor.executeCommand(command);
    }

    private void createHlsChunks(String inputPath, String outputPath) {
        // Create output directory if it doesn't exist
        File outputDir = new File(outputPath);
        if (!outputDir.exists()) {
            log.debug("Creating output directory: {}", outputPath);
            if (!outputDir.mkdirs()) {
                log.error("Failed to create output directory: {}", outputPath);
                throw new CustomException("Failed to create output directory: ", outputPath);
            }
        }

        // Use FFmpeg to create HLS chunks
        String command = String.format("ffmpeg -i %s -hls_time 10 -hls_list_size 0 %s/playlist.m3u8",
                inputPath, outputPath);
        ffMpegExecutor.executeCommand(command);
    }

    private void createMasterPlaylist(List<String> qualities, ProcessingContext context) {
        log.info("Creating master playlist for videoId: {}", context.getVideoId());

        StringBuilder masterPlaylist = new StringBuilder("#EXTM3U\n");

        for (String quality : qualities) {
            log.debug("Adding quality {} to master playlist", quality);
            String playlistPath = quality + "/playlist.m3u8";

            if ("original".equals(quality)) {
                // Get actual resolution and bandwidth for original quality
                String originalPlaylistPath = outputPath.resolve(context.getVideoId() + "/hls/original/playlist.m3u8").toString();
                String[] dimensions = getVideoDimensions(originalPlaylistPath);

                if (dimensions != null) {
                    int width = Integer.parseInt(dimensions[0]);
                    int height = Integer.parseInt(dimensions[1]);
                    masterPlaylist.append(String.format("#EXT-X-STREAM-INF:BANDWIDTH=%d,RESOLUTION=%dx%d\n",
                            videoUtil.getBandwidthForResolution(width, height), width, height));
                } else {
                    // Default fallback resolution
                    masterPlaylist.append("#EXT-X-STREAM-INF:BANDWIDTH=8000000,RESOLUTION=1920x1080\n");
                }
            } else {
                masterPlaylist.append(String.format("#EXT-X-STREAM-INF:BANDWIDTH=%d,RESOLUTION=%s\n",
                        videoUtil.getBandwidthForQuality(quality), videoUtil.getResolutionForQuality(quality)));
            }

            masterPlaylist.append(playlistPath).append("\n");
        }

        // Write the master playlist to a file
        Path masterPlaylistPath = outputPath.resolve(context.getVideoId() + "_master.m3u8");
        try {
            Files.write(masterPlaylistPath, masterPlaylist.toString().getBytes());

            // Upload the master playlist to storage
            File masterPlaylistFile = masterPlaylistPath.toFile();
            storageUtil.uploadToFileStorage(
                    List.of(videoUtil.convertFileToMultipartFile(masterPlaylistFile)), context);

            log.info("Successfully created and uploaded master playlist: {}", masterPlaylistPath);
        } catch (IOException e) {
            log.error("Error creating master playlist for videoId: {}", context.getVideoId(), e);
            throw new CustomException("Error creating master playlist", e.getMessage());
        }
    }

    private void uploadHlsChunksToMinio(String hlsOutputPath, String minioPath, ProcessingContext context) {
        log.debug("Starting upload of HLS chunks from {} to Minio path {}", hlsOutputPath, minioPath);

        File hlsDir = new File(hlsOutputPath);
        File[] files = hlsDir.listFiles();

        if (files == null || files.length == 0) {
            log.warn("No HLS chunks found in directory: {}", hlsOutputPath);
            return;
        }

        log.info("Found {} HLS chunks to upload", files.length);

        List<MultipartFile> multipartFiles = Arrays.stream(files)
                .peek(p -> log.debug("Processing HLS chunk: {}", p.getName()))
                .map(videoUtil::convertFileToMultipartFile)
                .toList();

        try {
            storageUtil.uploadToFileStorage(multipartFiles, context);
        } catch (IOException e) {
            throw new CustomException("Failed to upload HLS chunks to MinIO", e.getMessage());
        }

        log.info("Completed uploading {} HLS chunks to {}", files.length, minioPath);
    }

    // Remove static QUALITIES list as we'll generate it dynamically
    private List<String> getApplicableQualities(String inputPath) {
        // Get input video dimensions
        String[] dimensions = getVideoDimensions(inputPath);
        if (dimensions == null) {
            log.error("Could not determine input video dimensions");
            return Arrays.asList("240p", "480p"); // fallback to safe defaults
        }

        int width = Integer.parseInt(dimensions[0]);
        int height = Integer.parseInt(dimensions[1]);
        List<String> applicableQualities = new ArrayList<>();

        // Add original quality first
        applicableQualities.add("original");

        // Add lower qualities based on input resolution
        if (height > 720) applicableQualities.add("720p");
        if (height > 480) applicableQualities.add("480p");
        if (height > 240) applicableQualities.add("240p");

        log.info("Determined applicable qualities for input video ({}x{}): {}", width, height, applicableQualities);
        return applicableQualities;
    }

    private String[] getVideoDimensions(String videoPath) {
        try {
            Process process = Runtime.getRuntime().exec(String.format(
                    "ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0 %s",
                    videoPath
            ));

            String dimensions;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                dimensions = reader.readLine();
                process.waitFor();
            }

            if (dimensions != null && !dimensions.isEmpty()) {
                return dimensions.split("x");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Preserve interruption status
            log.error("Thread was interrupted while getting video dimensions for path: {}", videoPath, e);
            throw new CustomException("Thread was interrupted while getting video dimensions", e.getMessage());
        } catch (Exception e) {
            log.error("Error getting video dimensions for path: {}", videoPath, e);
            throw new CustomException("Error getting video dimensions", e.getMessage());
        }
        return null;
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

