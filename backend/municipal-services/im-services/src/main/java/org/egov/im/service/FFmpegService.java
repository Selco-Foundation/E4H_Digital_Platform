package org.egov.im.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.settings.VideoQualityConfig;
import org.egov.im.util.DirectoryUtil;
import org.egov.im.util.StorageUtil;
import org.egov.im.util.VideoUtil;
import org.egov.im.web.models.ProcessingContext;
import org.egov.tracer.model.CustomException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Component
public class FFmpegService {

    private final FFMpegExecutor ffMpegExecutor;
    private final FFmpegCommandGenerator fFmpegCommandGenerator;
    private final VideoUtil videoUtil;
    private final DirectoryUtil directoryUtil;
    private final StorageUtil storageUtil;

    @Async
    public CompletableFuture<Void> processQuality(
            ProcessingContext context, String inputPath, Path outputPath, VideoQualityConfig videoQuality) {
        try {
            Path path = directoryUtil.createDirectory(String.format("%s/%s/hls/%s",
                    outputPath.toString(), context.getVideoId(), videoQuality.getLabel()));

            String file = String.format("%s/playlist.m3u8", path);
            String command = videoQuality.isOriginal()
                    ? fFmpegCommandGenerator.getBaseCommand(inputPath, file)
                    : fFmpegCommandGenerator.getOptimizedCommand(inputPath,
                    "veryfast", videoQuality.getCrf(), videoQuality.getResolution(), file);

            log.info("Executing FFmpeg command for {}: {}", videoQuality.getLabel(), command);
            ffMpegExecutor.executeCommand(command);

            // Upload to MinIO
            storageUtil.uploadToFileStorage(
                    List.of(videoUtil.convertFileToMultipartFile(path.resolve(file).toFile())), context);

            log.info("Successfully processed and uploaded {} quality", videoQuality.getLabel());

            return CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            log.error("Error processing quality {}: {}", videoQuality.getLabel(), e.getMessage(), e);
            throw new CustomException("Error pushing to MinIO", e.getMessage());
        }
    }


    public void createMasterPlaylist(List<VideoQualityConfig> qualities,
                                     ProcessingContext context,
                                     Path outputPath) {
        log.info("Creating master playlist for videoId: {}", context.getVideoId());

        StringBuilder masterPlaylist = new StringBuilder("#EXTM3U\n");

        for (VideoQualityConfig quality : qualities) {
            String playlistPath = quality.getLabel() + "/playlist.m3u8";
            if (quality.isOriginal()) {
                // Fetch original stream info
                String streamInfo = videoUtil.getOriginalStreamInfo(context, outputPath, quality.getLabel());

                if (streamInfo == null || streamInfo.isBlank()) {
                    log.warn("No original stream info available for videoId: {}", context.getVideoId());
                    throw new CustomException("MASTER_PLAYLIST_ERROR", "Missing original stream info");
                }
                masterPlaylist.append(streamInfo);
            } else {
                masterPlaylist.append(String.format("#EXT-X-STREAM-INF:BANDWIDTH=%d,RESOLUTION=%s%n%s%n",
                        quality.getBitRate(), quality.getResolution(), playlistPath));
            }
            log.debug("Added quality {} ({}) to master playlist: {}",
                    quality.getLabel(), quality.getResolution(), playlistPath);
        }

        Path masterPlaylistPath = outputPath.resolve(context.getVideoId()).resolve("hls/master.m3u8");
        Path masterPath = directoryUtil.createFile(masterPlaylistPath);

        try {
            Files.writeString(masterPath, masterPlaylist.toString());
            File masterPlaylistFile = masterPath.toFile();

            // Upload master playlist to storage
            storageUtil.uploadToFileStorage(
                    List.of(videoUtil.convertFileToMultipartFile(masterPlaylistFile)), context);

            log.info("Successfully created and uploaded master playlist: {}", masterPlaylistPath);
        } catch (IOException e) {
            log.error("Error creating master playlist for videoId: {}", context.getVideoId(), e);
            throw new CustomException("MASTER_PLAYLIST_ERROR", "Failed to write master playlist");
        }
    }

}
