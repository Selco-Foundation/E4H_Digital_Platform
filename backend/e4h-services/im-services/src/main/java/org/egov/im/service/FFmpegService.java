package org.egov.im.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.settings.VideoQualitySettings;
import org.egov.im.util.DirectoryUtil;
import org.egov.im.util.VideoUtil;
import org.egov.im.web.models.ProcessingContext;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class FFmpegService {

    private final FFMpegExecutor ffMpegExecutor;
    private final FFmpegCommandGenerator fFmpegCommandGenerator;
    private final VideoUtil videoUtil;
    private final DirectoryUtil directoryUtil;

    public String processQuality(
            ProcessingContext context, String inputPath, Path outputPath, VideoQualitySettings videoQuality) {

        Path path = directoryUtil.createDirectory(String.format("%s/%s/hls/%s",
                outputPath.toString(), context.getVideoId(), videoQuality.getLabel()));

        String file = String.format("%s/playlist.m3u8", path);
        String command = videoQuality.isOriginal()
                ? fFmpegCommandGenerator.getBaseCommand(inputPath, file)
                : fFmpegCommandGenerator.getOptimizedCommand(inputPath,
                "veryfast", videoQuality.getCrf(), videoQuality.getResolution(), videoQuality.getAudioBitRate(), file);

        log.info("Executing FFmpeg command for {}: {}", videoQuality.getLabel(), command);
        ffMpegExecutor.executeCommand(command);

        String baseFileName = path.toString().split("output")[1];

        log.info("Successfully processed quality: {}", videoQuality.getLabel());

        return baseFileName;
    }


    public MultipartFile createMasterPlaylist(List<VideoQualitySettings> qualities,
                                                ProcessingContext context,
                                                Path outputPath) {

        directoryUtil.createDirectory(String.format("%s/%s/hls",
                outputPath.toString(), context.getVideoId()));

        log.info("Creating master playlist for videoId: {}", context.getVideoId());

        StringBuilder masterPlaylist = new StringBuilder("#EXTM3U\n");

        for (VideoQualitySettings quality : qualities) {
            String playlistPath = quality.getLabel() + "/playlist.m3u8";
                masterPlaylist.append(String.format("#EXT-X-STREAM-INF:BANDWIDTH=%d,RESOLUTION=%s%n%s%n",
                        quality.getBitRate(), quality.getResolution(), playlistPath));
            log.debug("Added quality {} ({}) to master playlist: {}",
                    quality.getLabel(), quality.getResolution(), playlistPath);
        }

        Path masterPlaylistPath = outputPath.resolve(context.getVideoId()).resolve("hls/master.m3u8");
        Path masterPath = directoryUtil.createFile(masterPlaylistPath);

        try {
            Files.writeString(masterPath, masterPlaylist.toString());
            File masterPlaylistFile = masterPath.toFile();

            log.info("master path: {}", masterPath);

            String resolveBasePath = videoUtil.pathExtractor(masterPlaylistPath.toString(), "output");

            //convert to multiPathFile
            return videoUtil.convertFileToMultipartFile(masterPlaylistFile, resolveBasePath);

        } catch (IOException e) {
            log.error("Error creating master playlist for videoId: {}", context.getVideoId(), e);
            throw new CustomException("MASTER_PLAYLIST_ERROR", "Failed to write master playlist");
        }
    }

}
