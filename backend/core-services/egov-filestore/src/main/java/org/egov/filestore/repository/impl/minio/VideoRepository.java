package org.egov.filestore.repository.impl.minio;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class VideoRepository {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    private static final String OUTPUT_DIR = "output";
    private final Path outputPath;

    public VideoRepository() {
        try {
            // Initialize output directory in current working directory
            outputPath = Paths.get(System.getProperty("user.dir"), OUTPUT_DIR);
            Files.createDirectories(outputPath);
            log.info("Created output directory at: {}", outputPath);
        } catch (IOException e) {
            log.error("Failed to create output directory", e);
            throw new RuntimeException("Failed to create output directory", e);
        }
    }

    // Add timing data structures
    private static class ProcessingTimes {
        long transcodeTime;
        long hlsTime;
        long uploadTime;
        
        public long getTotal() {
            return transcodeTime + hlsTime + uploadTime;
        }
    }

    public String uploadAndProcess(MultipartFile file) throws IOException {

        Map<String, ProcessingTimes> qualityTimings = new HashMap<>();
        Instant processingStartTime;
        long totalProcessingTime;

        log.info("Starting upload and processing for file: {}", file.getOriginalFilename());
        processingStartTime = Instant.now();
        qualityTimings.clear();
        
        String videoId = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        log.debug("Generated video ID: {}", videoId);

        // Create output directory if it doesn't exist
        Files.createDirectories(outputPath);
        
        // Save original file
        Path tempFile = outputPath.resolve(videoId + extension);
        log.debug("logging all variables: {} {} {} {} {} {}", videoId, extension, outputPath, tempFile.toString(), file, originalFilename);
        file.transferTo(tempFile.toFile());
        log.debug("Saved original file to temporary location: {}", tempFile);

        // Upload original file to Minio
        uploadToMinio(tempFile.toFile(), videoId + "/original" + extension);
        log.info("Uploaded original file to Minio");

        // Process video with dynamic qualities
        processVideo(tempFile.toFile(), videoId, qualityTimings);
        
        // Calculate total processing time
        totalProcessingTime = Duration.between(processingStartTime, Instant.now()).toMillis();
        
        // Generate and log processing report
        generateProcessingReport(videoId, totalProcessingTime, qualityTimings);
        
        log.info("Completed video processing for videoId: {}", videoId);
        return videoId;
    }

    private void uploadToMinio(File file, String objectName) {
        log.debug("Uploading file to Minio: {}", objectName);
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(Files.newInputStream(file.toPath()), file.length(), -1)
                    .build());
            log.info("Successfully uploaded file to Minio: {}", objectName);
        } catch (Exception e) {
            log.error("Error uploading file to Minio: {}", objectName, e);
            throw new RuntimeException("Error uploading file to Minio", e);
        }
    }

    private void processVideo(File inputFile, String videoId, Map<String, ProcessingTimes> qualityTimings) {
        log.info("Starting video processing for videoId: {}", videoId);
        
        // Get applicable qualities based on input video
        List<String> qualities = getApplicableQualities(inputFile.getAbsolutePath());
        
        for (String quality : qualities) {
            log.debug("Processing quality: {} for videoId: {}", quality, videoId);
            qualityTimings.put(quality, new ProcessingTimes());
            
            if (quality.equals("original")) {
                // Skip transcoding for original quality
                String hlsOutputPath = outputPath.resolve(Paths.get(videoId, "hls", quality)).toString();
                
                // Create HLS chunks directly from original
                Instant hlsStart = Instant.now();
                createHlsChunks(inputFile.getAbsolutePath(), hlsOutputPath);
                qualityTimings.get(quality).hlsTime = Duration.between(hlsStart, Instant.now()).toMillis();
                
                // Upload HLS chunks
                Instant uploadStart = Instant.now();
                uploadHlsChunksToMinio(hlsOutputPath, videoId + "/hls/" + quality);
                qualityTimings.get(quality).uploadTime = Duration.between(uploadStart, Instant.now()).toMillis();
                
                continue;
            }

            String outputFilename = outputPath.resolve(videoId + "_" + quality + ".mp4").toString();
            String hlsOutputPath = outputPath.resolve(Paths.get(videoId, "hls", quality)).toString();

            // Rest of the existing processing logic
            Instant transcodeStart = Instant.now();
            transcodeVideo(inputFile.getAbsolutePath(), outputFilename, quality);
            qualityTimings.get(quality).transcodeTime = Duration.between(transcodeStart, Instant.now()).toMillis();

            Instant hlsStart = Instant.now();
            createHlsChunks(outputFilename, hlsOutputPath);
            qualityTimings.get(quality).hlsTime = Duration.between(hlsStart, Instant.now()).toMillis();

            Instant uploadStart = Instant.now();
            uploadToMinio(new File(outputFilename), videoId + "/" + new File(outputFilename).getName());
            uploadHlsChunksToMinio(hlsOutputPath, videoId + "/hls/" + quality);
            qualityTimings.get(quality).uploadTime = Duration.between(uploadStart, Instant.now()).toMillis();
        }

        // Create master playlist with all qualities including original
        createMasterPlaylist(videoId, qualities);
    }

    private void transcodeVideo(String inputPath, String outputPath, String quality) {
        // Use FFmpeg to transcode video with optimization flags
        String command = String.format("ffmpeg -i %s -vf scale=%s -c:v libx264 -preset ultrafast -crf 35 -c:a copy %s",
                inputPath, getScaleForQuality(quality), outputPath);
        executeCommand(command);
    }

    private void createHlsChunks(String inputPath, String outputPath) {
        // Create output directory if it doesn't exist
        File outputDir = new File(outputPath);
        if (!outputDir.exists()) {
            log.debug("Creating output directory: {}", outputPath);
            if (!outputDir.mkdirs()) {
                log.error("Failed to create output directory: {}", outputPath);
                throw new RuntimeException("Failed to create output directory: " + outputPath);
            }
        }

        // Use FFmpeg to create HLS chunks
        String command = String.format("ffmpeg -i %s -hls_time 10 -hls_list_size 0 %s/playlist.m3u8",
                inputPath, outputPath);
        executeCommand(command);
    }

    private void createMasterPlaylist(String videoId, List<String> qualities) {
        log.info("Creating master playlist for videoId: {}", videoId);
        StringBuilder masterPlaylist = new StringBuilder("#EXTM3U\n");
        
        for (String quality : qualities) {
            log.debug("Adding quality {} to master playlist", quality);
            if (quality.equals("original")) {
                // For original quality, we'll need to get its actual resolution and bandwidth
                String[] dimensions = getVideoDimensions(outputPath.resolve(videoId + "/hls/original/playlist.m3u8").toString());
                if (dimensions != null) {
                    int width = Integer.parseInt(dimensions[0]);
                    int height = Integer.parseInt(dimensions[1]);
                    masterPlaylist.append(String.format("#EXT-X-STREAM-INF:BANDWIDTH=%d,RESOLUTION=%dx%d\n",
                            getBandwidthForResolution(width, height), width, height));
                } else {
                    // Fallback if we can't get dimensions
                    masterPlaylist.append("#EXT-X-STREAM-INF:BANDWIDTH=8000000,RESOLUTION=1920x1080\n");
                }
            } else {
                masterPlaylist.append(String.format("#EXT-X-STREAM-INF:BANDWIDTH=%d,RESOLUTION=%s\n",
                        getBandwidthForQuality(quality), getResolutionForQuality(quality)));
            }
            masterPlaylist.append(quality + "/playlist.m3u8\n");
        }

        try {
            String masterPlaylistPath = outputPath.resolve(videoId + "_master.m3u8").toString();
            Files.write(Path.of(masterPlaylistPath), masterPlaylist.toString().getBytes());
            uploadToMinio(new File(masterPlaylistPath), videoId + "/master.m3u8");
        } catch (IOException e) {
            log.error("Error creating master playlist", e);
            throw new RuntimeException("Error creating master playlist", e);
        }
    }

    private void uploadHlsChunksToMinio(String hlsOutputPath, String minioPath) {
        log.debug("Starting upload of HLS chunks from {} to Minio path {}", hlsOutputPath, minioPath);
        File hlsDir = new File(hlsOutputPath);
        File[] files = hlsDir.listFiles();
        if (files == null || files.length == 0) {
            log.warn("No HLS chunks found in directory: {}", hlsOutputPath);
            return;
        }
        log.info("Found {} HLS chunks to upload", files.length);
        for (File file : files) {
            log.debug("Uploading HLS chunk: {}", file.getName());
            uploadToMinio(file, minioPath + "/" + file.getName());
        }
        log.info("Completed uploading {} HLS chunks to {}", files.length, minioPath);
    }

    private void executeCommand(String command) {
        log.debug("Executing command: {}", command);
        try {
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("Command failed with exit code: {}. Command: {}", exitCode, command);
                throw new RuntimeException("Command failed with exit code: " + exitCode);
            }
            log.debug("Command completed successfully with exit code: {}", exitCode);
        } catch (Exception e) {
            log.error("Error executing command: {}. Error: {}", command, e.getMessage(), e);
            throw new RuntimeException("Error executing command: " + command, e);
        }
    }

    private String getScaleForQuality(String quality) {
        log.trace("Getting scale for quality: {}", quality);
        try {
            switch (quality) {
                case "144p": return "256:144";
                case "240p": return "426:240";
                case "480p": return "854:480";
                case "720p": return "1280:720";
                case "1080p": return "1920:1080";
                default:
                    log.error("Invalid quality requested: {}", quality);
                    throw new IllegalArgumentException("Invalid quality: " + quality);
            }
        } catch (Exception e) {
            log.error("Error getting scale for quality: {}. Error: {}", quality, e.getMessage());
            throw e;
        }
    }

    private int getBandwidthForQuality(String quality) {
        switch (quality) {
            case "144p": return 300000;
            case "240p": return 700000;
            case "480p": return 1500000;
            case "720p": return 3000000;
            case "1080p": return 6000000;
            default: throw new IllegalArgumentException("Invalid quality: " + quality);
        }
    }

    private String getResolutionForQuality(String quality) {
        switch (quality) {
            case "144p": return "256x144";
            case "240p": return "426x240";
            case "480p": return "854x480";
            case "720p": return "1280x720";
            case "1080p": return "1920x1080";
            default: throw new IllegalArgumentException("Invalid quality: " + quality);
        }
    }

    private void generateProcessingReport(String videoId, long totalProcessingTime, Map<String, ProcessingTimes> qualityTimings ) {
        StringBuilder report = new StringBuilder();
        report.append("\n=== Video Processing Report ===\n");
        report.append("Video ID: ").append(videoId).append("\n");
        report.append("Total Processing Time: ").append(formatDuration(totalProcessingTime)).append("\n\n");
        report.append("Quality-wise Breakdown:\n");
        report.append(String.format("%-10s %-15s %-15s %-15s %-15s\n", 
                     "Quality", "Transcode", "HLS", "Upload", "Total"));
        report.append("----------------------------------------------------------\n");

        long totalTranscode = 0;
        long totalHls = 0;
        long totalUpload = 0;

        for (String quality : qualityTimings.keySet()) {
            ProcessingTimes times = qualityTimings.get(quality);
            if (times != null) {
                report.append(String.format("%-10s %-15s %-15s %-15s %-15s\n",
                    quality,
                    formatDuration(times.transcodeTime),
                    formatDuration(times.hlsTime),
                    formatDuration(times.uploadTime),
                    formatDuration(times.getTotal())
                ));
                totalTranscode += times.transcodeTime;
                totalHls += times.hlsTime;
                totalUpload += times.uploadTime;
            }
        }

        report.append("\nTotals by Process Type:\n");
        report.append(String.format("Transcoding: %s\n", formatDuration(totalTranscode)));
        report.append(String.format("HLS Creation: %s\n", formatDuration(totalHls)));
        report.append(String.format("Uploading: %s\n", formatDuration(totalUpload)));
        report.append(String.format("Total Processing Time: %s\n", formatDuration(totalProcessingTime)));

        log.info(report.toString());
    }

    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d.%03d", minutes, seconds, milliseconds % 1000);
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
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String dimensions = reader.readLine();
            process.waitFor();
            
            if (dimensions != null && !dimensions.isEmpty()) {
                return dimensions.split("x");
            }
        } catch (Exception e) {
            log.error("Error getting video dimensions", e);
        }
        return null;
    }

    private int getBandwidthForResolution(int width, int height) {
        // Estimate bandwidth based on resolution
        int pixels = width * height;
        if (pixels <= 256 * 144) return 300000;
        if (pixels <= 426 * 240) return 700000;
        if (pixels <= 854 * 480) return 1500000;
        if (pixels <= 1280 * 720) return 3000000;
        if (pixels <= 1920 * 1080) return 6000000;
        return 8000000; // for higher resolutions
    }
}

