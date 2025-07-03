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
}
