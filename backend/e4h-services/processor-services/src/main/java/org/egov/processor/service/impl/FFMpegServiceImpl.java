package org.egov.processor.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.processor.models.ProcessingContext;
import org.egov.processor.service.FFMpegService;
import org.egov.processor.settings.VideoQualitySettings;
import org.egov.processor.utils.DirectoryUtil;
import org.egov.processor.utils.VideoUtil;
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
public class FFMpegServiceImpl implements FFMpegService {

    private final FFMpegExecutor ffMpegExecutor;
    private final FFmpegCommandGenerator fFmpegCommandGenerator;
    private final DirectoryUtil directoryUtil;

    public String processQuality(
            ProcessingContext context, String inputPath, Path outputPath, VideoQualitySettings videoQuality) {
        log.trace("Method invoked: processQuality, videoId: {}, quality: {}", context.getVideoId(), videoQuality.getLabel());

        Path outputDirectory = createOutputDirectory(context, outputPath, videoQuality);
        String playlistFile = buildPlaylistFilePath(outputDirectory);
        String command = buildFfmpegCommand(inputPath, playlistFile, videoQuality);

        executeFfmpegCommand(command, videoQuality);

        String baseFileName = extractBaseFileName(outputDirectory);
        log.info("Successfully processed quality level: {} for videoId: {}", videoQuality.getLabel(), context.getVideoId());

        return baseFileName;
    }

    private Path createOutputDirectory(ProcessingContext context, Path outputPath, VideoQualitySettings videoQuality) {
        log.trace("Creating output directory for quality level");
        Path path = directoryUtil.createDirectory(String.format("%s/%s/hls/%s",
                outputPath.toString(), context.getVideoId(), videoQuality.getLabel()));
        log.debug("Output directory created: {}", path);
        return path;
    }

    private String buildPlaylistFilePath(Path outputDirectory) {
        String file = String.format("%s/playlist.m3u8", outputDirectory);
        log.trace("Playlist file path generated: {}", file);
        return file;
    }

    private String buildFfmpegCommand(String inputPath, String playlistFile, VideoQualitySettings videoQuality) {
        log.trace("Generating FFmpeg command, isOriginal: {}", videoQuality.isOriginal());
        if (videoQuality.isOriginal()) {
            return fFmpegCommandGenerator.getBaseCommand(inputPath, playlistFile);
        }

        return fFmpegCommandGenerator.getOptimizedCommand(
                inputPath,
                "veryfast",
                videoQuality.getCrf(),
                videoQuality.getResolution(),
                videoQuality.getAudioBitRate(),
                playlistFile
        );
    }

    private void executeFfmpegCommand(String command, VideoQualitySettings videoQuality) {
        log.info("Executing FFmpeg command for quality: {}", videoQuality.getLabel());
        log.debug("FFmpeg command: {}", command);
        ffMpegExecutor.executeCommand(command);
    }

    private String extractBaseFileName(Path outputDirectory) {
        String baseFileName = outputDirectory.toString().split("output")[1];
        log.debug("Base filename extracted: {}", baseFileName);
        return baseFileName;
    }
}
